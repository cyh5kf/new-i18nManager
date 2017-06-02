package tcpupload;

import android.content.Context;
import android.text.TextUtils;

import com.azus.android.util.AESFileUtil;
import com.azus.android.util.AZusLog;
import com.azus.android.util.FileCacheStore;
import com.azus.android.util.FileUtil;
import com.instanza.cocovoice.FileUploadHelp;
import com.hime.messenger.activity.helper.SelfEccHelper;
import com.hime.messenger.activity.helper.SettingHelper;
import com.hime.messenger.consts.AppConstants;
import com.hime.messenger.dao.LoginedUserMgr;
import com.hime.messenger.dao.SomaConfigMgr;
import com.hime.messenger.dao.model.CurrentUser;
import com.hime.messenger.dao.model.SelfEccModel;
import com.hime.messenger.httpservice.CryptManager;
import com.messenger.javaserver.compjecc.Ecc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nuodundd on 16/4/26.
 */
public class TcpUploadFileBase {
    private static final String TAG = TcpUploadFileBase.class.getSimpleName();
    public static final int FAIL_TYPE_FAIL = 1000;
    public static final int FAIL_TYPE_FILEERROR = 1001;
    public static final int FAIL_TYPE_SERVERERROR = 1002;

    public static final String FILE_TYPE_IMAGE = "image";
    public static final String FILE_TYPE_VIDEO = "video";
    public static final String FILE_TYPE_AUDIO = "audio";
    public static final String FILE_TYPE_AVATAR = "avatar";
    private static final int MAX_RETRYCOUNT = 1;
    private static Map<Long, UploadFileConfigBean> uploadFileConfigMap = new HashMap<Long, UploadFileConfigBean>();
    private static String gSessionValidTag = null;
    private String sessionTag = null;
    protected Context mContext;
    private long rowId = -1;
    private int retryCount = 0;
    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(1000);
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;
    private UploaderBase uploader;
    private boolean aesEncrypt;
    private UploadFileConfigBean uploadFileConfigBean;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "TcpUploadFileBase" + mCount.getAndIncrement());
        }
    };
    /**
     * An {@link Executor} that can be used to execute tasks in parallel.
     */
    public static Executor THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);


    public TcpUploadFileBase(Context mContext) {
        this.mContext = mContext;
        CurrentUser loginUser = LoginedUserMgr.getCurrentLoginedUser();
        if (null != loginUser) {
            setSessionTag(loginUser.getSessionTag());
        }
    }
    public void aPostFile(String fileKey, String filePath) {
       aPostFile(fileKey, filePath, true);
    }
    public void aPostFile(String fileKey, String filePath, boolean aesEncrypt) {
        this.aesEncrypt = aesEncrypt;
        this.aesEncrypt = false;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile() || file.length() == 0) {
            AZusLog.e("HttpRequest", "aPostFile Failed, file not exist, fileKey=" + fileKey + ",filePath=" + filePath);
            processFailed(FAIL_TYPE_FILEERROR);
            return;
        }
        startUpload(filePath, fileKey);
    }

    public void aPostFile(String fileKey, String filePath, boolean aesEncrypt, long rowId) {
        this.rowId = rowId;
        aPostFile(fileKey, filePath, aesEncrypt);
    }

    private void startUpload(final String filePath, final String uploadType) {
        THREAD_POOL_EXECUTOR.execute(new Runnable() {

            @Override
            public void run() {
                uploadFile(filePath, uploadType);
            }
        });
    }

    private void uploadFile(String filePath, String fileKey) {
        CurrentUser user = LoginedUserMgr.getCurrentLoginedUser();
        String uploadFilePath;
        if (null == user) {
            return;
        }
        long uid = user.getUserId();
        SelfEccModel selfEcc = SelfEccHelper.getLastSelfEccModel();

        byte[] usrprikey = selfEcc.getPrivatekey();
        byte[] usrpubkey = selfEcc.getPublickey();
        byte[] srvpubkey = Ecc.fromHexString(SettingHelper.getServerUploadFilePubKey());
        uploadFileConfigBean = null;
        boolean isFirstConnect=false;
        synchronized (uploadFileConfigMap) {
            uploadFileConfigBean = uploadFileConfigMap.get(rowId);
            if (null == uploadFileConfigBean) {
                try {
                    isFirstConnect=true;
                    uploadFileConfigBean = generateUploadBean(filePath, fileKey);
                } catch (Exception e) {
                    processFailed(FAIL_TYPE_FILEERROR);
                    AZusLog.e(TAG, e);
                    return;
                }
                if (rowId != -1) {
                    uploadFileConfigMap.put(rowId, uploadFileConfigBean);
                }
            }
        }


        uploadFilePath = aesEncrypt?uploadFileConfigBean.encryptFilePath:uploadFileConfigBean.filePath;
        if(FileUploadHelp.canUseNativeUploader()&& SomaConfigMgr.getInstance().useTcp2Upload()) {
            AZusLog.e(TAG,"Upload use tcp");
            uploader = new Uploader(uid, AppConstants.URL_TCPUPLOAD, uploadFilePath,
                    uploadFileConfigBean.uuid, !aesEncrypt, fileKey, this, isFirstConnect);
        }else{
            AZusLog.e(TAG,"Upload use http");
            uploader = new UploaderHttp(aesEncrypt,this);
        }
        UploadResult upres = null;
        try {
            if(uploader instanceof UploaderHttp){
                upres = ((UploaderHttp)uploader).aPostFile(fileKey,filePath,null,null,rowId);
            }else {
                upres = uploader.resume(usrprikey, usrpubkey, srvpubkey);
            }
        } catch (Exception e) {
            AZusLog.e(TAG, e);
        }
        if(!checkSessionTag()){
            return;
        }
        int ret = -1;
        synchronized (uploadFileConfigMap) {
            if (null != upres) {
                ret = upres.rv;
                switch (ret) {
                    case ErrorCode.CocoErrorcode_UPLOAD_INVALID_USER:
                    case ErrorCode.CocoErrorcode_UPLOAD_FILE_TOOLARGE:
                        processFailed(upres.rv);
                        return;
                    case 0:
                        if(upres.url == null){
                            processFailed(FAIL_TYPE_SERVERERROR);
                            break;
                        }
                        if (upres.newsrvkey != null) {
                            SettingHelper.setServerUploadFilePubKey(upres.newsrvkey.toString());
                        }
                        if (rowId != -1) {
                            synchronized (uploadFileConfigMap) {
                                if (uploadFileConfigMap.containsKey(rowId)) {
                                    UploadFileConfigBean bean = uploadFileConfigMap.remove(rowId);
                                    if (null != bean) {
                                        FileUtil.deleteFile(bean.encryptFilePath);
                                    }
                                }
                            }
                        }
                        if(upres.mediaaeskey == null) {
                            upres.mediaaeskey = uploadFileConfigBean.mediaAesKey;
                        }
                        AZusLog.e(TAG,"success res="+upres);
                        processResult(upres);
                        return;
                }
            }
        }
        AZusLog.e(TAG,"fail res="+upres);
        if (retryCount < MAX_RETRYCOUNT) {
            retryCount++;
            aPostFile(fileKey, uploadFilePath, aesEncrypt, rowId);
        } else {
            processFailed(ret);
        }
    }

    private UploadFileConfigBean generateUploadBean(String filePath, String fileType) throws Exception {
        String crypedFilePath = null;
        long fileSize = 0l;
        String mediaAesKey = null;
        if (aesEncrypt) {
            crypedFilePath = FileCacheStore.genNewCacheFile();
            mediaAesKey = new CryptManager().getAES256Key();
            AESFileUtil.AesCbcCryptFile(filePath,
                    mediaAesKey.getBytes(), crypedFilePath);
            fileSize = new File(crypedFilePath).length();
        } else {
            fileSize = new File(filePath).length();
        }
        return new UploadFileConfigBean(filePath, crypedFilePath, UUID.randomUUID().toString(), mediaAesKey, fileSize, fileType);
    }


    public void processResult(UploadResult result) {

    }

    public void processCanceled() {

    }

    public void publishProgress(long downedSize, long fileSize) {

    }

    public void processFailed(int resultCode) {

    }

    public static void setGlobalSessionValidTag(String tag) {
        gSessionValidTag = tag;
    }

    public static String getGlobalSessionValidTag() {
        return gSessionValidTag;
    }

    public String getSessionTag() {
        return sessionTag;
    }

    public void setSessionTag(String sessionTag) {
        this.sessionTag = sessionTag;
    }

    public boolean checkSessionTag() {
        if (TextUtils.isEmpty(getSessionTag()))
            return true;
        if (getGlobalSessionValidTag() == null)
            return false;
        if (getGlobalSessionValidTag().equals(getSessionTag()))
            return true;
        return false;
    }

    public void cancel() {
        setSessionTag(UUID.randomUUID().toString());
        try {
            processCanceled();
            if (null != uploader) {
                uploader.cancel();
            }
        } catch (Exception e) {
            AZusLog.w("AZusHttp", e);
        } catch (Throwable t) {
            AZusLog.w("AZusHttp", t);
        }
    }

    protected long getFileSize() {
        long size = 0l;
        if (uploadFileConfigBean != null) {
            size = uploadFileConfigBean.fileSize;
        }
        return size;
    }

    public String getUploadType() {
        String fileType = null;
        if (uploadFileConfigBean != null) {
            fileType = uploadFileConfigBean.fileType;
        }
        return fileType;
    }

    public String getUUID() {
        String UUID = null;
        if (uploadFileConfigBean != null) {
            UUID = uploadFileConfigBean.uuid;
        }
        return UUID;
    }
}
