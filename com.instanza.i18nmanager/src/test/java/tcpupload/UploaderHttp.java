package tcpupload;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;

import com.azus.android.core.ApplicationHelper;
import com.azus.android.http.ServiceMappingManager;
import com.azus.android.http.ServiceMappingManager.MappedURLConnection;
import com.azus.android.http.ServiceNode;
import com.azus.android.util.AESFileUtil;
import com.azus.android.util.AZusIntentConstant;
import com.azus.android.util.AZusLog;
import com.azus.android.util.FileCacheStore;
import com.azus.android.util.FileUtil;
import com.azus.android.util.JSONUtils;
import com.azus.android.util.MD5Util;
import com.google.android.gcm.GCMRegistrar;
import com.hime.messenger.HimeAppRuntime;
import com.hime.messenger.HimeApplication;
import com.hime.messenger.R;
import com.hime.messenger.activity.setting.LanguageSettingHelper;
import com.hime.messenger.consts.AppConstants;
import com.hime.messenger.dao.LoginedUserMgr;
import com.hime.messenger.dao.model.CurrentUser;
import com.hime.messenger.httpservice.CryptManager;
import com.hime.messenger.httpservice.action.ActionBase;
import com.hime.messenger.httpservice.action.HttpRequestBody;
import com.hime.messenger.httpservice.action.VerifyPhoneReturnCode;
import com.hime.messenger.utils.HelperFunc;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.cert.CertPathValidatorException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLHandshakeException;

public class UploaderHttp implements UploaderBase{
	private static final String TAG = UploaderHttp.class.getSimpleName();
	public final static int MAX_FAIL = 10;
	public final static int MAX_LOOP = 5000;
	public final static int MIN_SIZE = 16 * 1024;
	public static final int FAIL_TYPE_FAIL = 1000;
	public static final int FAIL_TYPE_FILEERROR = 1001;
	public static final int FAIL_TYPE_SERVERERROR = 1002;

	//mediafile加密aeskey
	protected static final String MEDIAAESKEY="_mediaAesKey";
	private static final int TIMEOUT_CONNECT = 20000;
	private static final int TIMEOUT_READ = 30000;
	private static final int UPLOAD_FILE_SIZE = 10;
	String boundary = "----" + System.currentTimeMillis() + "----";
	private static Map<Long, UploadFileConfigBean> uploadFileConfigMap = new HashMap<Long, UploadFileConfigBean>();
	private static Set<Long> uploadingFileSet=new HashSet<Long>();
	protected static ExecutorService mThreadPool = Executors
			.newCachedThreadPool();
	private static String gSessionValidTag = null;
	private String sessionTag = null;
	private String url;
	protected Context mContext;
//	private String extraParam = "";
	private Map<String,String> extraParam = new HashMap<>();
	private long rowId = -1;

    private CryptManager cryptManager;
    private long totalBytes;
    private String uploadType;
    private String mediaAesKey;
    private String crypedFilePath;
    private long orgFileSize;//原始文件大小
    private long flength;//加密文件大小
    protected String uuid = null;
    private String fileMd5;
	private boolean aesEncrypt;
	private TcpUploadFileBase tcpUploadFileBase;
    static{
    	System.setProperty("http.maxConnections","50");
//		myX509TrustManager xtm = new myX509TrustManager();
//
//		SSLContext sslContext = null;
//		try {
//			sslContext = SSLContext.getInstance("TLS"); // 或SSL
//			X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };
//			sslContext.init(null, xtmArray, new java.security.SecureRandom());
//		} catch (GeneralSecurityException e) {
//			e.printStackTrace();
//		}
//		if (sslContext != null) {
//			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
//					.getSocketFactory());
//		}
//    	myHostnameVerifier hnv = new myHostnameVerifier();
//		HttpsURLConnection.setDefaultHostnameVerifier(hnv);
    }

    public UploaderHttp(boolean aesEncrypt,TcpUploadFileBase tcpUploadFileBase) {
		this.aesEncrypt = aesEncrypt;
		this.tcpUploadFileBase = tcpUploadFileBase;
        CurrentUser loginUser = LoginedUserMgr.getCurrentLoginedUser();
        if (null != loginUser) {
            setSessionTag(loginUser.getSessionTag());
        }
    }
    
    public long getFileSize(){
    	return totalBytes;
    }
    public String getUploadType(){
    	return uploadType;
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

    protected CryptManager getCryptManager(){
        if(cryptManager == null){
            cryptManager = new CryptManager();
        }
        return cryptManager;
    }
/*
	public static String getFileMD5(byte buffer[]) {
		java.security.MessageDigest digest = null;
		try {
			digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(buffer);
			byte[] array = digest.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static String getFileMD5(File file) {
		java.security.MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = java.security.MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
			byte[] array = digest.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
*/
	private MappedURLConnection getConnection(String url) {
//		AZusLog.d(TAG, "threadid="+Thread.currentThread()+",getconnection,url="+url);
        HttpURLConnection conn = null;
        String host = null;
        MappedURLConnection mappedConn = null;
        try {
            mappedConn = ServiceMappingManager.getSingleton().openURLConnection(url, true);
            conn = mappedConn.connection;
            //https://groups.google.com/forum/?fromgroups=#!topic/android-developers/e_FExl6jl90
            conn.setRequestProperty("Connection", "close");// 默认是keep-alive
            // 为解决操作系统4.0版本以上
            // connection会被cache起来，拿来重用，导致重用出问题
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Cache-Control", "no-transform");
            AZusLog.d("CocoAsyncUploadRequestBase" , "normal host = " + host);
            conn.setRequestProperty("User-Agent", ApplicationHelper.getUserAgent());
            conn.setConnectTimeout(TIMEOUT_CONNECT);
            conn.setReadTimeout(TIMEOUT_READ);
//            conn.setRequestProperty("deviceId", com.hime.messenger.utils.device.UUID.getDeviceUUID());
        }catch (UnknownHostException e){
            e.printStackTrace();
            ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
        }catch (SocketException e){
            e.printStackTrace();
            ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
        }  catch (MalformedURLException e) {
            e.printStackTrace();
            ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
            return null;
        }
		return mappedConn;
	}

	/**
	 * 确定数据块offset length
	 * 
	 * @param uploadType
	 * @param uuid
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private JSONObject sendQuery(String uploadType, String uuid) throws  Exception  {
//		String para = String
//				.format("&method=query&uploadType=%s&uuid=%s&md5=%s&fileSize=%d&clientTime=%d",
//						uploadType, uuid, md5, fileSize,
//						System.currentTimeMillis());
//		HttpURLConnection conn = getConnection(url + para);
		
		Map<String,Object> reqParams=new HashMap<String, Object>(extraParam);
		reqParams.put("method","query");
		reqParams.put("uploadType",uploadType);
		reqParams.put("uuid",uuid);
		reqParams.put("userId", user.getUserId());
		reqParams.put("token", user.getLoginToken());
		
		reqParams.put("fileSize",flength);
		reqParams.put("orgFileSize",orgFileSize);
		reqParams.put("clientTime",System.currentTimeMillis());
		reqParams.put("devicetype", ActionBase.DEVICETYPE);
		reqParams.put("mediaAesKey", mediaAesKey);
		reqParams.put("fileMd5", fileMd5);
		String data = encryptParam(reqParams);
		//TODO
		MappedURLConnection mappedConn = getConnection(url);
		if (mappedConn == null || mappedConn.connection == null) {
			return null;
		}
		HttpURLConnection conn = mappedConn.connection;
//		AZusLog.d(TAG, "sendQuery url="+conn.getURL().toString());
//		String u = "http://112.126.66.21:17080/upload/file2/upload/upSend.json";
//		URL requestURL = new URL(u);
//		conn = (HttpURLConnection) requestURL.openConnection();
		BufferedReader in = null;
		StringBuffer response = null;
		String responseString = null;
		OutputStream outputStream = null;
		ByteArrayOutputStream outputStreamCache = new ByteArrayOutputStream();
		try {
			addFormField(outputStreamCache, "data", data);
			outputStreamCache.write((LINE_FEED+"--"+boundary+"--").getBytes());
			outputStreamCache.flush();
			int length = outputStreamCache.size();
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.addRequestProperty("Content-length", length+"");
			conn.connect();
			outputStream = conn.getOutputStream();
			outputStream.write(outputStreamCache.toByteArray());
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String inputLine;
			response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_REACHABLE);
			AZusLog.d(TAG, "sendQuery url="+conn.getURL().toString()+",result="+response.toString());
			responseString = decryptDataString(response.toString());
			AZusLog.d(TAG, "sendQuery,responseString="+responseString);
			if(retryForInvalidRsaPubkey(responseString)){
//				sendQuery(uploadType, uuid);
			}
		}catch(UnknownHostException e){
			ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
			AZusLog.e(TAG, e);
			throw e;
		}catch(SocketException e){
			ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
			AZusLog.e(TAG, e);
			throw e;
		}catch (Exception e) {
			ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
			AZusLog.e(TAG, e);
			if(e instanceof SSLHandshakeException){
             	Intent intent=new Intent(AZusIntentConstant.ACTION_CERTINVALID_EVT);
             	intent.putExtra("url", url);
             	LocalBroadcastManager.getInstance(ApplicationHelper.getContext()).sendBroadcast(intent);
             }
			 if(e instanceof CertPathValidatorException){
             	Intent intent=new Intent(AZusIntentConstant.ACTION_CERTINVALID_EVT);
             	intent.putExtra("url", url);
             	LocalBroadcastManager.getInstance(ApplicationHelper.getContext()).sendBroadcast(intent);
             }
			throw e;
		} finally {

			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {

				}
			}
			if (outputStreamCache != null) {
				try {
					outputStreamCache.close();
				} catch (IOException e) {

				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {

				}
			}
			conn.disconnect();
		}
//		if(null == response){
//			return new JSONObject();
//		}
		return new JSONObject(responseString);
	}
	
	private String decryptDataString(String response) throws Exception{
		JSONObject returnObj = new JSONObject(response.toString());
		String return_data = CryptManager.decryptDataString(returnObj.getString("data"),getCryptManager().getAES256Key(),true);
		returnObj.put("data", new JSONObject(return_data));
		return returnObj.toString();
	}
	protected boolean retryForInvalidRsaPubkey(String response) {

		boolean isRetry = false;

		try {
			JSONObject returnObj = new JSONObject(response);
			JSONObject rspData = returnObj.getJSONObject("data");
			int ret = rspData.optInt("returncode");
			if (ret == VerifyPhoneReturnCode.HTTP_INVALID_RSAPUBKEY) {
				String pubkey = rspData.getString("pubkey");
                String synaeskey = rspData.optString("synaeskey");
                if(isValidServerAeskey(synaeskey)&&!TextUtils.isEmpty(pubkey)){
					CryptManager cryptManager = getCryptManager();
					cryptManager.reset();
					cryptManager.saveRsaPubKeyData2FileCache(pubkey);
					isRetry = true;
                }
			}
		} catch (Exception e) {
			AZusLog.e(this.getClass().getSimpleName(), e);
		}

		return isRetry;
	}
	private boolean isValidServerAeskey(String synaeskey) {
        if (TextUtils.isEmpty(synaeskey)) {
            return false;
        }

        return synaeskey.equals(cryptManager.getAES256Key());
    }
	public void cancel() {
		setSessionTag(UUID.randomUUID().toString());
		try {
			processCanceled();
		} catch (Exception e) {
			AZusLog.w("AZusHttp", e);
		} catch (Throwable t) {
			AZusLog.w("AZusHttp", t);
		}
	}

	@Override
	public UploadResult resume(byte[] prikey, byte[] pubkey, byte[] srvkey) throws Exception {
		return null;
	}

	private byte[] getAESEncrypedData(byte[] buffer)throws Exception{
		return CryptManager.encrypt2(buffer,mediaAesKey, true);
	}
	/**
	 * 查询数据块offset length
	 * 
	 * @param uploadType
	 * @param uuid
	 * @param offset
	 * @param length
	 * @return
	 * @throws Exception
	 */
	private JSONObject sendData(File f, String uploadType, String uuid,int offset, int length) throws Exception {
		length = getMaxSize(length);
		if(length <= 0){
			length = 1024*32;
		}
		if(length+offset>flength){
			length=(int) (flength-offset);
		}
//		length = (int) Math.min(length, flength - offset);
		String nettype= HelperFunc.getNetworkType(ApplicationHelper.getContext());
		int netenv=1;
		if(nettype.startsWith("2G_")){
			netenv=0;
		}else if(nettype.startsWith("3G")){
			netenv=1;
		}else if(nettype.startsWith("4G")){
			netenv=2;
		}else if(nettype.startsWith("WIFI")){
			netenv=3;
		}
		Map<String,Object> reqParams=new HashMap<String, Object>(extraParam);
		reqParams.put("method","upload");
		reqParams.put("uploadType",uploadType);
		reqParams.put("uuid",uuid);
		reqParams.put("fileSize",flength);
		reqParams.put("orgFileSize",orgFileSize);
		reqParams.put("clientTime", HimeAppRuntime.getInstance().getServerTimeMillisecond());
		reqParams.put("timeseq", HimeAppRuntime.getInstance().getNextRowId());
		reqParams.put("offset",offset);
		reqParams.put("length",length);
		reqParams.put("env",netenv);
		reqParams.put("userId", user.getUserId());
		reqParams.put("token", user.getLoginToken());
		reqParams.put("devicetype", ActionBase.DEVICETYPE);
		reqParams.put("mediaAesKey", mediaAesKey);
		reqParams.put("fileMd5", fileMd5);
		//TODO:
//		String para = String
//				.format("&method=upload&uploadType=%s&uuid=%s&md5=%s&fileSize=%d&clientTime=%d&offset=%d&length=%d",
//						uploadType, uuid, md5, fileSize,
//						System.currentTimeMillis(), offset, length);
//		HttpURLConnection conn = getConnection(url + para + extraParam);

		MappedURLConnection mappedConn = getConnection(url);
		if (mappedConn == null || mappedConn.connection == null) {
			AZusLog.e("HttpRequest", "Upload Fail,getConnection return null");
			return null;
		}
		HttpURLConnection conn = mappedConn.connection;
		AZusLog.d(TAG, "sendData,url="+conn.getURL().toString());
//		if (length > 256 * 1024) {
//			conn.setChunkedStreamingMode(0);
//		} else {
//			conn.setFixedLengthStreamingMode(length);
//		}

		// post
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		
//		conn.setRequestProperty("Content-Length", String.valueOf(length));
//		conn.connect();
		BufferedReader in = null;
		StringBuffer response = null;
		String responseString = null;
		OutputStream outputStream = null;
		RandomAccessFile ff = new RandomAccessFile(f, "r");
		ByteArrayOutputStream outputStreamCache = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[4096];
			int actualLen = 0;
			int showLen = 0;
			int icount = 0;
			int uploaded = 0;
			boolean isLastBlock = false;
			ff.seek(offset);
			/*
			 * outputStream = conn.getOutputStream();
			while ((len = ff.read(buffer)) != -1) {
				if (uploaded + len > length) {
					len = length - uploaded;
				}
				outputStream.write(buffer, 0, len);
				icount++;
				uploaded += len;

				if (icount > UPLOAD_FILE_SIZE) {
					outputStream.flush();
					icount = 0;
					publishProgress(offset * 100 + uploaded * 100, fileSize);
					// publishProgress(offset+uploaded, fileSize);
				}

				if (uploaded >= length) {
					break;
				}
			}
			*/
			buffer = new byte[length];
			showLen=ff.read(buffer);
			if(showLen<0){
				throw new Exception("Reached end file");
			}
			if(showLen<length){
				byte[] tmpBuffer=new byte[showLen];
				System.arraycopy(buffer, 0, tmpBuffer, 0, showLen);
				buffer=tmpBuffer;
				reqParams.put("length",showLen);
			}
//			if(showLen>0){
//				buffer=getAESEncrypedData(buffer);
//			}
//			String tmpmd5str=getFileMD5(buffer);
			String tmpmd5str= MD5Util.md5str(buffer, false);
			reqParams.put("md5",tmpmd5str);
			if (showLen < 0 || offset + showLen >= flength) {
				isLastBlock = true;
			}
			reqParams.put("isLastBlock",isLastBlock);
			buffer = Base64.encode(buffer, Base64.NO_WRAP);
			String data = encryptParam(reqParams);
			actualLen=buffer.length;
			
			addFormField(outputStreamCache, "data", data);
			addFilePart(outputStreamCache, "data2");
			outputStreamCache.write(buffer);
			outputStreamCache.write((LINE_FEED+"--"+boundary+"--").getBytes());
			outputStreamCache.flush();
			actualLen = outputStreamCache.size();
			
			conn.setRequestProperty("Content-Length", String.valueOf(actualLen));
			if (length > 256 * 1024) {
				conn.setChunkedStreamingMode(0);
			} else {
				conn.setFixedLengthStreamingMode(actualLen);
			}
			conn.connect();
			outputStream = conn.getOutputStream();
			outputStream.write(outputStreamCache.toByteArray());
			outputStream.flush();
			uploaded += showLen;
//			publishProgress(offset*100 + uploaded*100, fileSize);
			int responseCode = conn.getResponseCode();
			if(responseCode != HttpURLConnection.HTTP_OK){
				//可能是服务器临时bug
//                ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
				throw new Exception("Server Bad Error");
			}
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			
			String inputLine;
			response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_REACHABLE);
			AZusLog.d(TAG, "sendData,response="+response.toString());
			responseString = decryptDataString(response.toString());
			AZusLog.d(TAG, "sendData,responseString="+responseString);
			if(retryForInvalidRsaPubkey(responseString)){
//				sendData(f, uploadType, uuid,offset, length);
			}
		}catch(UnknownHostException e){
			ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
			throw e;
		}catch(SocketException e){
			ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
			throw e;
		}catch (Exception e) {
			ServiceMappingManager.getSingleton().updateServiceStatus(mappedConn, ServiceNode.SERVER_STATUS_UNREACHABLE);
			 if(e instanceof SSLHandshakeException){
             	Intent intent=new Intent(AZusIntentConstant.ACTION_CERTINVALID_EVT);
             	intent.putExtra("url", url);
             	LocalBroadcastManager.getInstance(ApplicationHelper.getContext()).sendBroadcast(intent);
             }
			 if(e instanceof CertPathValidatorException){
             	Intent intent=new Intent(AZusIntentConstant.ACTION_CERTINVALID_EVT);
             	intent.putExtra("url", url);
             	LocalBroadcastManager.getInstance(ApplicationHelper.getContext()).sendBroadcast(intent);
             }
			throw e;
		} finally {
			if(null != ff){
				try{
					ff.close();
				}catch(Exception e){
					
				}
				ff=null;
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {

				}
			}

			if (conn != null) {
				conn.disconnect();
			}

			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {

				}
			}
			if (outputStreamCache != null) {
				try {
					outputStreamCache.close();
				} catch (IOException e) {

				}
			}
			
		}

		return new JSONObject(responseString);
	}
	
	private int getMaxSize(long length) {
		int maxSegSize = 1024*200;
		String netType = HelperFunc
				.getNetworkType(HimeApplication.getContext());
		if (netType.startsWith("2G_")) {
			maxSegSize = 1024 * 16;
			
		} else if (netType.startsWith("3G_")) {
			maxSegSize = 1024 * 128;
		} else if (netType.startsWith("4G_") || netType.startsWith("WIFI")) {
			maxSegSize = VERSION.SDK_INT < VERSION_CODES.HONEYCOMB ? 1024 * 200
					: 1024 * 1024;
		}
		if(length<32*1024){
			length=32*1024;
		}
		maxSegSize = (int) Math.min(length, maxSegSize);
		
		if(maxSegSize %16 !=0){
			maxSegSize=maxSegSize-maxSegSize%16;
		}
		
		return maxSegSize;
	}
	
	private static final String LINE_FEED = "\r\n";
	/**
     * Adds a form field to the request
     * @param name field name
     * @param value field value
	 * @throws IOException 
     */
	public void addFormField(OutputStream outputStream, String name,
			String value) throws IOException {
		StringBuilder build = new StringBuilder();
		build.append("--" + boundary).append(LINE_FEED);
		build.append("Content-Disposition: form-data; name=\"" + name + "\"")
				.append(LINE_FEED);
		build.append("Content-Type: text/plain; charset=utf-8" ).append(
				LINE_FEED);
		build.append(LINE_FEED);
		build.append(value).append(LINE_FEED);
		
		outputStream.write(build.toString().getBytes());
		outputStream.flush();
	}
 
    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @throws IOException
     */
    public void addFilePart(OutputStream outputStream,String fieldName)
            throws IOException {
    		StringBuilder build = new StringBuilder();
    		build.append("--" + boundary).append(LINE_FEED);
    		build.append(
                "Content-Disposition: form-data; name=\"" + fieldName+"\"")
                .append(LINE_FEED);
//    		build.append(
//                "Content-Type: "
//                        + "image/jpeg")
//                .append(LINE_FEED);
//    		build.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
    		build.append(LINE_FEED);
    		outputStream.write(build.toString().getBytes());
    		outputStream.flush();
    }
    
	private JSONObject resumableUpload(String uploadType, File f)
			throws  Exception {
		boolean bFirst=true;
		if (rowId == -1) {
			f = prepareUploadFile(f);
		} else {
			synchronized (uploadFileConfigMap) {
				if (uploadFileConfigMap.containsKey(rowId)) {
//					if(uploadingFileSet.contains(rowId)){
//						return null;
//					}
					UploadFileConfigBean bean= uploadFileConfigMap.get(rowId);
					uuid = bean.uuid;
					f = new File(bean.filePath);
					flength = bean.encryptFileLength;
					mediaAesKey = bean.aesKey;
					orgFileSize = bean.fileLength;
					bFirst = false;
					fileMd5 = bean.fileMd5;
					uploadingFileSet.add(rowId);
				} else {
					f = prepareUploadFile(f);
				}
			}
		}
		StringBuilder builder = new StringBuilder();
		url+=builder.append("?uuid=").append(uuid);
		totalBytes=flength;
		int lastLength = (int)flength;
		int failCount = 0;
		int loop = 0;
		boolean hasIoErr = false;
		JSONObject o=null;
		if(bFirst){
		    o=new JSONObject();
		    o.put("code", 200);
		    JSONObject dataO=new JSONObject();
		    dataO.put("xOffset", 0);
		    long xLength=flength;
		    xLength = getMaxSize(xLength);
		    dataO.put("xLength", xLength);
		    dataO.put("ret", 308);
		    o.put("data", dataO);
		    lastLength=(int)xLength;
		}else{
			 o = sendQuery(uploadType, uuid);
		}

		while (true) {
			if (!checkSessionTag()) {
				AZusLog.e("HttpRequest", "Upload Failed,checkSessionTag fail");
				fail();
				return null;
			}
			loop++;
			if (loop >= MAX_LOOP) {// 防止服务器bug导致客户端死循环
				AZusLog.e("HttpRequest", "Upload Failed,MAX_LOOP fail");
				fail();
				return null;
			}
			try {
				int code = o.getInt("code");
				if (code != 200) {
					throw new Exception(o.toString());
				}
				JSONObject rspData = JSONUtils.getJSONObject(o, "data");
				int ret = rspData.optInt("ret");
				if(ret==0){
					ret = rspData.getInt("returncode");
				}
				if (ret == 308) {// continue
					int offset = rspData.getInt("xOffset");
					int length = rspData.getInt("xLength");
					if (hasIoErr) {
						// io err, client use exponential backoff
						length = lastLength / 2;
						if (length < MIN_SIZE) {
							length = MIN_SIZE;
						}
						if(length%16 !=0){
							length =length-length%16;
						}
						hasIoErr = false;
					}
					o = sendData(f, uploadType, uuid, offset,
							length);
					
					if (null == o) {
						AZusLog.e("HttpRequest", "Upload Failed,sendData fail");
						fail();
						return null;
					}
					if (null != o.optJSONObject("data")&&o.optJSONObject("data").optInt("returncode") != VerifyPhoneReturnCode.HTTP_INVALID_RSAPUBKEY) {
						failCount = 0;
						if (checkSessionTag()) {
							publishProgress(offset * 100 + length * 100, flength);
						}
						lastLength = 2*length;
					}
				} else if (ret == 200) {// finished
					o.put(MEDIAAESKEY, mediaAesKey);
					sucess(o);
					if (rowId != -1) {
						synchronized (uploadFileConfigMap) {
							if (uploadFileConfigMap.containsKey(rowId)) {
								UploadFileConfigBean bean = uploadFileConfigMap.remove(rowId);
								if(null != bean){
									FileUtil.deleteFile(bean.filePath);
								}
							}
						}
					}
					break;
				} else if (ret == 309) {
					AZusLog.e("HttpRequest", "Upload Failed, 309 fail");
					//重新开始上传
					synchronized (uploadFileConfigMap) {
						if (uploadFileConfigMap.containsKey(rowId)) {
							uploadFileConfigMap.remove(rowId);
						}
					}
					fail();
					return null;
				} else if (ret == VerifyPhoneReturnCode.HTTP_INVALID_RSAPUBKEY){
					o = sendQuery(uploadType, uuid);
				}
			} catch (Exception e) {
				AZusLog.e(TAG, e);
				failCount++;
				hasIoErr = true;
				if (failCount >= MAX_FAIL) {
					AZusLog.e("HttpRequest", "Upload Failed,MAX_FAIL fail");
					fail();
					return null;
				}
				Thread.sleep(500);
				// 发生异常，重新查询从哪里开始upload数据
				o = sendQuery(uploadType, uuid);
				/*//避免对服务器产生巨大压力
				while (true) {
					try {
						o = sendQuery(uploadType, uuid, f.length(), md5);
						break;
					} catch (Exception e2) {
						AZusLog.e(TAG, e2);
						hasIoErr = true;
					}
					failCount++;
					hasIoErr = true;
					if (failCount >= MAX_FAIL) {
						fail();
						return null;
					}
					Thread.sleep(500);
				}
				*/
			}
		}
		return o;
	}
	
	private File prepareUploadFile(File f) throws Exception {
		uuid = UUID.randomUUID().toString();
		crypedFilePath= FileCacheStore.genNewCacheFile();
		mediaAesKey = new CryptManager().getAES256Key();
		AESFileUtil.AesCbcCryptFile(f.getAbsolutePath(),
				mediaAesKey.getBytes(), crypedFilePath);
		File cryptedFile = new File(crypedFilePath);
		orgFileSize = f.length();
		flength = cryptedFile.length();
		//jay modify to fix md5 multi-thread bug
//		fileMd5 = getFileMD5(cryptedFile);
		fileMd5= MD5Util.md5sum(crypedFilePath);
		//end.
		if (rowId != -1) {
			synchronized (uploadFileConfigMap) {
				UploadFileConfigBean bean = new UploadFileConfigBean(
						mediaAesKey, cryptedFile.getAbsolutePath(), uuid,
						fileMd5, orgFileSize, flength);
				uploadFileConfigMap.put(rowId, bean);
				uploadingFileSet.add(rowId);
			}
		}
		return cryptedFile;
	}
	private void fail() {
		if(rowId != -1){
			uploadingFileSet.remove(rowId);
		}
		if (checkSessionTag()) {
			processFailed(FAIL_TYPE_FAIL);
		}
	}

	private void sucess(JSONObject json) {
		if(rowId != -1){
			uploadingFileSet.remove(rowId);
		}
		if (checkSessionTag()) {
			processResult(json);
		}
	}

	CurrentUser user;
	private UploadResult uploadFile(File file, String uploadType) {
		this.uploadType=uploadType;
		AZusLog.e(TAG, "uploadFile,rowid="+rowId);
		try {
			url = getUrl();
			StringBuilder builder = new StringBuilder();
			user = LoginedUserMgr.getCurrentLoginedUser();
			if (null == user) {
				processFailed(FAIL_TYPE_FAIL);
				return new UploadResult(FAIL_TYPE_FAIL,null,null);
			}
//			String token=user.getLoginToken();
//			byte[] aestoken=CryptUtil.aesEncrypt(user.getLoginToken().getBytes("utf-8"),
//                    getCryptManager().getAES256Key().getBytes(),true);
//			token=Base64.encodeToString(aestoken, Base64.NO_WRAP);
//		    token=URLEncoder.encode(token);
//					
//			builder.append("?userId=").append(user.getUserId())
//					.append("&token=").append(token);
//			url += builder.toString();
			JSONObject json = resumableUpload(uploadType, file);
			JSONObject jsonData;
			try {
				jsonData = json.getJSONObject("data");
			} catch (JSONException e) {
				e.printStackTrace();
				jsonData = null;
			}

			if (null == jsonData) {
				processFailed(FAIL_TYPE_SERVERERROR);
				return new UploadResult(FAIL_TYPE_SERVERERROR,null,null);
			}

			final int ret = jsonData.optInt("ret");
			if (ret != 200) {
				processFailed(FAIL_TYPE_SERVERERROR);
				return new UploadResult(FAIL_TYPE_SERVERERROR,null,null);
			}

			String aesKey = JSONUtils.getJSONString(json, MEDIAAESKEY);
			String encryptUrl = JSONUtils.getJSONString(jsonData, "encryptUrl");
			String fileUrl = JSONUtils.getJSONString(jsonData, "url");
			if(aesEncrypt){
				return new UploadResult(0,encryptUrl,null,aesKey);
			}else{
				return new UploadResult(0,fileUrl,null);
			}

		} catch (IOException e) {
			AZusLog.e(TAG, "uploadFile Fail,rowid="+rowId);
			fail();
//			processFailed(FAIL_TYPE_FAIL);
			e.printStackTrace();
		} catch (Exception e) {
			AZusLog.e(TAG, "uploadFile Fail,rowid="+rowId);
//			processFailed(FAIL_TYPE_FAIL);
			fail();
			e.printStackTrace();
		}
		return new UploadResult(FAIL_TYPE_FAIL,null,null);
	}
	public UploadResult aPostFile(String fileKey, String filePath, String fileName,
			Map<String, String> extraParams) {
		File file = new File(filePath);
		if (!file.exists() || !file.isFile() || file.length() == 0) {
			AZusLog.e("HttpRequest", "aPostFile Failed, file not exist, fileKey="+fileKey+",filePath="+filePath);
			processFailed(FAIL_TYPE_FILEERROR);
			return new UploadResult(FAIL_TYPE_FILEERROR,null,null);
		}
		if (null != extraParams) {
//			StringBuilder builder = new StringBuilder();
//			for (Map.Entry<String, String> entry : extraParams.entrySet()) {
//				builder.append("&").append(entry.getKey()).append("=")
//						.append(entry.getValue());
//			}
			extraParam = extraParams;
		}
		return startUpload(file, fileKey);
	}

	public UploadResult aPostFile(String fileKey, String filePath, String fileName,
			Map<String, String> extraParams, long rowId) {
		this.rowId = rowId;
		return aPostFile(fileKey, filePath, fileName, extraParams);
	}

	private UploadResult startUpload(final File file, final String uploadType) {
//		mThreadPool.execute(new Runnable() {
//
//			@Override
//			public void run() {
//				uploadFile(file, uploadType);
//			}
//		});
		return uploadFile(file, uploadType);
	}

	public void processResult(JSONObject json) {

	}

	public String getUrl() {
		return HimeApplication.getContext().getString(R.string.url_upload_securesharesend,
				AppConstants.URL_HTTP);
	}

	public void processCanceled() {

	}

	public void publishProgress(long downedSize, long fileSize) {
		tcpUploadFileBase.publishProgress(downedSize,fileSize);
	}

	public void processFailed(int resultCode) {

	}
	
	public  String encryptParam(Map<String, Object> map) throws Exception {
		String requestRSAToken = getRequestAesToken();
		String requestData = getRequestData(map);
		String devicetype = ActionBase.DEVICETYPE;
		String pukmd5 = CryptManager.getRsaPubkeyMd5();
		String requestBody = JSONUtils.toJson(new HttpRequestBody(
				requestRSAToken, requestData, pukmd5, devicetype));

		// BASE64(AES(json)
		String data = CryptManager.encrypt(requestBody,
				CryptManager.getRequstParamsCryptAeskey(), true, false);
		return data;
	}

	private  String getRequestData(Map<String, Object> map)
			throws Exception {
		// 固定参数先设置,因为有的请求的参数可能会需要覆盖固定参数，比如 REQUEST_DEVICEKEY
		map.put(ActionBase.REQUEST_REQDATA_JSON_AESKEY, getCryptManager()
				.getAES256Key());
		map.put(ActionBase.REQUEST_DEVICETYPE, ActionBase.DEVICETYPE);
		map.put(ActionBase.REQUEST_VERSION, HimeApplication.getVersion());
		map.put(ActionBase.REQUEST_DEVICEKEY, com.hime.messenger.utils.device.UUID.getDeviceUUID());
		map.put(ActionBase.REQUEST_LANGUAGE, LanguageSettingHelper
				.getInstance().getAppLanguage());
		map.put(ActionBase.REQUEST_DEVICETOKEN,
				GCMRegistrar.getRegistrationId(HimeApplication.getContext()));
		JSONObject jsonObject = new JSONObject(map);
		String URLEncoderBase64AESJson = CryptManager.encrypt(jsonObject.toString(),
				getCryptManager().getAES256Key(), true, true);
		return URLEncoderBase64AESJson;
	}


	protected  String getRequestAesToken() throws Exception {
		String aesToken = getCryptManager().getRSAEncrypedAESKey();
		return URLEncoder.encode(aesToken, "utf-8");
	}
	
	class UploadFileConfigBean{
		String aesKey;
		String filePath;
		String uuid;
		String fileMd5;
		long fileLength;
		long encryptFileLength;
		public UploadFileConfigBean(String aesKey, String filePath,
				String uuid, String fileMd5,long fileLength, long encryptFileLength) {
			this.aesKey = aesKey;
			this.filePath = filePath;
			this.uuid = uuid;
			this.fileMd5 = fileMd5;
			this.fileLength = fileLength;
			this.encryptFileLength = encryptFileLength;
		}
	}
}
