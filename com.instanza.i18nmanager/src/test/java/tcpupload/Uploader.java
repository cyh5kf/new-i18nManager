package tcpupload;

import com.azus.android.http.ServiceMappingManager;
import com.azus.android.http.ServiceNode;
import com.azus.android.util.AZusLog;
import com.azus.android.util.MD5Util;
import com.instanza.cocovoice.FileUploadHelp;
import com.messenger.javaserver.compjecc.Ecc;
import com.messenger.javaserver.tcpupload.proto.AcquireFileRequest;
import com.messenger.javaserver.tcpupload.proto.AcquireFileResponse;
import com.messenger.javaserver.tcpupload.proto.MessageHeader;
import com.messenger.javaserver.tcpupload.proto.UPLOAD_METHOD_TYPE;
import com.messenger.javaserver.tcpupload.proto.UploadRequest;
import com.messenger.javaserver.tcpupload.proto.UploadResponse;
import com.squareup.wire.Wire;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.zip.CRC32;

import okio.ByteString;

public class Uploader implements UploaderBase {
    private static final int TIMEOUT_CONNECT = 20000;
    private static final int TIMEOUT_READ = 30000;
    private static final String TAG = Uploader.class.getSimpleName();
    private final long uid;
    private String srvaddr;
    private final String fpath;
    private final String fuuid;
    private int port;
    private InetSocketAddress addr;
    private final boolean isperm;
    private int sock;
    private byte[] md5;
    private long flen;
    //    private Thread uprcvThread;
    private boolean isFini = false;
    private byte[] userprikey;
    private byte[] userpubkey;
    private byte[] srvpubkey;
    // upload result
    private UploadResult upres;
    private String type;
    private TcpUploadFileBase tcpUploadFileBase;
    private String url;
    private boolean isFirstConnect;

    public Uploader(long uid, String url, String fpath, String fuuid, boolean isperm, String type, TcpUploadFileBase tcpUploadFileBase, boolean isFirstConnect) {
        this.uid = uid;
//        this.srvaddr = srvaddr;
//        this.port = port;
        this.fpath = fpath;
        this.fuuid = fuuid;
//        this.isperm = isperm;

        if (TcpUploadFileBase.FILE_TYPE_AVATAR.equals(type)) {
            this.isperm = true;
        } else {
            this.isperm = false;
        }

//        this.addr = new InetSocketAddress(srvaddr, port);
        this.sock = -1;
        this.md5 = null;
        this.upres = new UploadResult(-1, null, null);
        this.type = type;
        this.tcpUploadFileBase = tcpUploadFileBase;
        this.url = url;
        this.isFirstConnect = isFirstConnect;
    }

//    public UploadResult start(byte[] prikey, byte[] pubkey, byte[] srvkey) throws Exception {
//        System.out.println(String.format("Start upload:%s(%s) to server:%s", this.fuuid, this.fpath, this.addr));
//        this.userprikey = bcopy(prikey);
//        this.userpubkey = bcopy(pubkey);
//        this.srvpubkey = bcopy(srvkey);
//
//        FileInputStream pf = open();
//        sock = new Socket();
//        uprcvThread = null;
//        this.md5 = calc_md5(pf);
//        try {
//            sock.connect(addr, TIMEOUT_CONNECT);
//            sock.setSoTimeout(TIMEOUT_READ);
//            System.out.println(this.fuuid + ":Connect ok, start upload");
//            do_upload(pf, 0);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            pf.close();
//            if (uprcvThread != null) {
//                uprcvThread.join();
//            }
//            sock.close();
//        }
//        return upres;
//    }

    public UploadResult resume(byte[] prikey, byte[] pubkey, byte[] srvkey) throws Exception {
        AZusLog.d(TAG, String.format("Resume upload:%s to server:%s", this.fuuid, this.addr));
        this.userprikey = bcopy(prikey);
        this.userpubkey = bcopy(pubkey);
        this.srvpubkey = bcopy(srvkey);
        FileInputStream pf = open();
        if (null == this.md5) {
            this.md5 = calc_md5(pf);
        }

        ServiceNode node = null;
        try {
            node = ServiceMappingManager.getSingleton().getServiceMapping(url, 0, true); // catch Throwable?
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (node == null) {
            url = url.split("://")[1];
        } else {
            if (node.url.contains("://")) {
                url = node.url.split("://")[1];
            } else {
                url = node.url;
            }
        }
        String[] urlSplit = url.split(":");
        this.srvaddr = urlSplit[0];
        this.port = Integer.parseInt(urlSplit[1]);
        InetAddress address = InetAddress.getByName(srvaddr);
        srvaddr = address.getHostAddress();
        AZusLog.e(TAG, "url = " + url + " srvaddr = " + srvaddr);
        try {
            sock = FileUploadHelp.getSocket(srvaddr, port);
            if (sock < 0) {
                throw new Exception("socket cann't create");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ServiceMappingManager.getSingleton().updateServiceStatus(node, ServiceNode.SERVER_STATUS_UNREACHABLE);
            try {
                pf.close();
//                if (uprcvThread != null) {
//                    uprcvThread.join();
//                }
                if (sock >= 0) {
                    FileUploadHelp.close(sock);
                    sock = -1;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                ;
            }
            throw e;
        }


        try {
            long offset = get_cur_pos();
            this.isFirstConnect = false;
            AZusLog.e(TAG, this.fuuid + ":Connect ok, start upload from:" + offset + " url=" + url);
            do_upload(pf, offset);
            do_recv_up_result();
            ServiceMappingManager.getSingleton().updateServiceStatus(node, ServiceNode.SERVER_STATUS_REACHABLE);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            ServiceMappingManager.getSingleton().updateServiceStatus(node, ServiceNode.SERVER_STATUS_UNREACHABLE);
            throw e;
        } catch (SocketException e) {
            e.printStackTrace();
            ServiceMappingManager.getSingleton().updateServiceStatus(node, ServiceNode.SERVER_STATUS_UNREACHABLE);
            throw e;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ServiceMappingManager.getSingleton().updateServiceStatus(node, ServiceNode.SERVER_STATUS_UNREACHABLE);
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            ServiceMappingManager.getSingleton().updateServiceStatus(node, ServiceNode.SERVER_STATUS_UNREACHABLE);
            throw e;
        } finally {
            pf.close();
//            if (uprcvThread != null) {
//                uprcvThread.join();
//            }
            if (sock >= 0) {
                FileUploadHelp.close(sock);
                sock = -1;
            }
        }
        return upres;
    }

    public void cancel() {
        this.isFini = true;
        if (sock >= 0) {
            FileUploadHelp.close(sock);
            sock = -1;
        }
//        if (uprcvThread != null) {
//            uprcvThread.interrupt();
//        }
    }

    private long get_cur_pos() throws Exception {
        if (isFirstConnect) {
            return 0;
        }
        AcquireFileRequest.Builder req = new AcquireFileRequest.Builder();
        req.fuuid(fuuid);
        ByteBuffer wbuff = construct_req(UPLOAD_METHOD_TYPE.UM_ACQUIRE.getValue(), req.build().toByteArray());
        byte[] bytes = new byte[wbuff.remaining()];
        wbuff.get(bytes, 0, bytes.length);
        int ret = FileUploadHelp.write(sock, bytes);
        if (ret < 0) {
            throw new IOException("can't send data");
        }
        byte[] rbuff;
        rbuff = FileUploadHelp.read(sock);
        int len = rbuff.length;
        if (len > 0) {
            byte[] brsp = parse_rsp(UPLOAD_METHOD_TYPE.UM_ACQUIRE.getValue(), rbuff);
            AcquireFileResponse rsp = new Wire().parseFrom(brsp, AcquireFileResponse.class);
            if (rsp.ret != 0) {
                throw new IOException("get_cur_pos ret=" + rsp.ret + ", " + fuuid);
            }
            AZusLog.e(TAG, "last pos = " + rsp.fpos);
            return rsp.fpos;
        }
        throw new IOException("get_cur_pos no response received:" + fuuid);
    }

    private void do_upload(FileInputStream pf, long offset) throws Exception {
        pf.getChannel().position(offset);
        // send upload request
        UploadRequest.Builder req = new UploadRequest.Builder();
        req.uid(this.uid);
        req.fuuid(this.fuuid);
        req.ftype(type);
        req.fmd5(ByteString.of(this.md5));
        req.flen(this.flen);
        req.offset(offset);
        if (this.isperm) {
            req.isperm(isperm);
        }
        ByteBuffer wbuff = construct_req(UPLOAD_METHOD_TYPE.UM_UPLOAD.getValue(), req.build().toByteArray());
        this.isFini = false;
//        uprcvThread = new Thread(new UpRecvThread(this));
//        uprcvThread.start();
        byte[] bytes = new byte[wbuff.remaining()];
        int ret;
        wbuff.get(bytes, 0, bytes.length);
        ret = FileUploadHelp.write(sock, bytes);
        if (ret < 0) {
            throw new IOException("can't send data");
        }
        byte[] buff = new byte[1024 * 48];
        long left = this.flen - offset;

        int rlen;
        int chunckSize = 1024 * 4;
        byte[] tmpChunk = new byte[chunckSize];
        byte[] blen = new byte[2];
        byte[] bcrc = new byte[4];

/**
 while (!this.isFini && tcpUploadFileBase.checkSessionTag()) {
 rlen = pf.read(buff);

 if (rlen > 0) {
 ArrayList<byte[]> dstArray = new ArrayList<byte[]>();
 //                byte[] org = new byte[rlen];
 int dstBytes = 0;
 int chunkCount = rlen / chunckSize;
 if (rlen % chunckSize != 0) {
 chunkCount += 1;
 }

 if (tmpChunk.length != chunckSize) {
 tmpChunk = new byte[chunckSize];
 }
 for (int t = 0; t < chunkCount; t++) {
 int tmpLen = chunckSize;
 if (t == chunkCount - 1) {
 int paddLen = rlen % chunckSize;
 if (paddLen != 0) {
 tmpLen = paddLen;
 tmpChunk = new byte[tmpLen];
 }
 }
 System.arraycopy(buff, chunckSize * t, tmpChunk, 0, tmpLen);

 byte[] data = Ecc.encrypt(tmpChunk, userprikey, srvpubkey);

 blen[0] = (byte) ((data.length >> 8) & 0xFF);
 blen[1] = (byte) (data.length & 0xFF);

 CRC32 crc = new CRC32();
 crc.update(tmpChunk);
 int icrc = (int) (crc.getValue() & 0xFFFFFFFF);

 for (int i = bcrc.length - 1; i >= 0; i--) {
 bcrc[i] = (byte) (icrc & 0xFF);
 icrc >>= 8;
 }
 //                    AZusLog.e("upload", this.fuuid + ":send stream, orglen=" + rlen + ", encrypted len:" + data.length
 //                            + ", left:" + left + "blen0:" + blen[0] + ", blen1:" + blen[1]);
 byte[] sendData = new byte[blen.length + bcrc.length + data.length];
 System.arraycopy(blen, 0, sendData, 0, blen.length);
 System.arraycopy(bcrc, 0, sendData, blen.length, bcrc.length);
 System.arraycopy(data, 0, sendData, blen.length + bcrc.length, data.length);
 dstArray.add(sendData);
 dstBytes += sendData.length;
 }
 byte[] tmpDataAray = new byte[dstBytes];
 int dstOffset = 0;
 for (byte[] tarray : dstArray) {
 System.arraycopy(tarray, 0, tmpDataAray, dstOffset, tarray.length);
 dstOffset += tarray.length;
 }
 ret = FileUploadHelp.write(sock, tmpDataAray);
 if (ret < 0) {
 throw new IOException("can't send data");
 }
 left -= rlen;
 long uploaded = flen - left;
 tcpUploadFileBase.publishProgress(flen - left, flen);
 AZusLog.e("upload", this.fuuid + ":send ok." + " uploaded = " + uploaded + " flen = " + flen);
 } else {
 break;
 }
 }
 */
        CRC32 crc = new CRC32();
        while (!this.isFini && tcpUploadFileBase.checkSessionTag()) {
            rlen = pf.read(buff);
            if (rlen > 0) {
                byte[] org = new byte[rlen];
                for (int i = 0; i < org.length; i++) {
                    org[i] = buff[i];
                }
                byte[] data = Ecc.encrypt(org, userprikey, srvpubkey);
//                blen = new byte[2];
                blen[0] = (byte) ((data.length >> 8) & 0xFF);
                blen[1] = (byte) (data.length & 0xFF);

                crc.reset();
                crc.update(org);
                int icrc = (int) (crc.getValue() & 0xFFFFFFFF);
//                bcrc = new byte[4];
                for (int i = bcrc.length - 1; i >= 0; i--) {
                    bcrc[i] = (byte) (icrc & 0xFF);
                    icrc >>= 8;
                }
                AZusLog.e("upload", this.fuuid + ":send stream, orglen=" + rlen + ", encrypted len:" + data.length
                        + ", left:" + left + "blen0:" + blen[0] + ", blen1:" + blen[1]);
                byte[] sendData = new byte[blen.length + bcrc.length + data.length];
                System.arraycopy(blen, 0, sendData, 0, blen.length);
                System.arraycopy(bcrc, 0, sendData, blen.length, bcrc.length);
                System.arraycopy(data, 0, sendData, blen.length + bcrc.length, data.length);

                ret = FileUploadHelp.write(sock, sendData);
                if (ret < 0) {
                    throw new IOException("can't send data");
                }
                left -= rlen;
                long uploaded = flen - left;
                tcpUploadFileBase.publishProgress(flen - left, flen);
                AZusLog.e("upload", this.fuuid + ":send ok." + " uploaded = " + uploaded + " flen = " + flen);
            } else {
                break;
            }
        }

        AZusLog.e("upload", this.fuuid + ":Send Upload stream finished.");
    }

    private FileInputStream open() throws IOException {
        File pf = new File(fpath);
        if (!pf.exists()) {
            throw new IOException("Upload File not exist:" + fpath);
        }
        if (!pf.isFile()) {
            throw new IOException("Upload File is not file:" + fpath);
        }
        flen = pf.length();
        return new FileInputStream(pf);
    }

    private byte[] calc_md5(FileInputStream pf) throws IOException {
        return MD5Util.md5(pf);
    }

    private ByteBuffer construct_req(int method, byte[] plain) throws Exception {
        ByteBuffer wbuff = ByteBuffer.allocate(1500);

        CRC32 crc = new CRC32();
        crc.update(plain);
        crc.update(String.format(Locale.ENGLISH, "%d", method).getBytes());
        int icrc = (int) (crc.getValue() & 0xFFFFFFFF);
        byte[] wdata = Ecc.encrypt(plain, userprikey, srvpubkey);

        MessageHeader.Builder mheader = new MessageHeader.Builder();
        mheader.method(method);
        mheader.crc32(icrc);

        mheader.user_ecckey(ByteString.of(userpubkey));
        mheader.srv_ecckey(ByteString.of(srvpubkey));

        byte[] mdata = mheader.build().toByteArray();
        byte[] bmagic = new byte[4];
        bmagic[0] = (byte) 0x3A;
        bmagic[1] = (byte) 0x97;
        bmagic[2] = (byte) 0xD3;
        bmagic[3] = (byte) 0x4B;
        wbuff.put(bmagic);
        wbuff.put(conv_len(mdata.length, wdata.length));
        wbuff.put(mdata);
        wbuff.put(wdata);

        wbuff.flip();
        return wbuff;
    }

    private byte[] conv_len(int hlen, int qlen) {
        byte[] b = new byte[4];
        b[0] = (byte) ((hlen >> 8) & 0xFF);
        b[1] = (byte) (hlen & 0xFF);
        b[2] = (byte) ((qlen >> 8) & 0xFF);
        b[3] = (byte) (qlen & 0xFF);
        return b;
    }

    private void do_recv_up_result() {
        byte[] buff;
        try {
            AZusLog.e("upload", "read");
            buff = FileUploadHelp.read(sock);
            int len = buff.length;
            AZusLog.e("upload", "upload len = " + len);
            if (len > 0) {
                byte[] brsp = parse_rsp(UPLOAD_METHOD_TYPE.UM_UPLOAD.getValue(), buff);
                UploadResponse rsp = new Wire().parseFrom(brsp, UploadResponse.class);
                AZusLog.e("upload", "upload url = " + rsp.url + " ret=" + rsp.ret + " offset =" + rsp.offset);
                String url = null;
                byte[] newecckey = null;
                if (rsp.url != null && !rsp.url.isEmpty()) {
                    url = rsp.url;
                }
                if (rsp.newecckey != null) {
                    newecckey = rsp.newecckey.toByteArray();
                }

                this.upres = new UploadResult(rsp.ret, url, newecckey);
            }
        } catch (Exception e) {
            AZusLog.e(TAG, e);
        } finally {
            this.isFini = true;
        }
    }

    private byte[] parse_rsp(int wantcmd, byte[] buff) throws Exception {
        int hlen = ((buff[0] & 0xFF) << 8 | (buff[1] & 0xFF)) & 0xFFFF;
        int qlen = ((buff[2] & 0xFF) << 8 | (buff[3] & 0xFF)) & 0xFFFF;

        MessageHeader mheader = new Wire().parseFrom(ByteString.of(buff, 4, hlen).toByteArray(), MessageHeader.class);
        int cmd = mheader.method;
        int rcrc = mheader.crc32;
        byte[] srvkey = mheader.srv_ecckey.toByteArray();
        byte[] usrkey = mheader.user_ecckey.toByteArray();
        if (cmd != wantcmd) {
            AZusLog.e(TAG, this.fuuid + ":parse_rsp wantcmd:" + wantcmd + ", while rsp command is:" + cmd);
            return null;
        }

        // check usrkey
        if (!isEqual(usrkey, userpubkey)) {
            throw new IOException(String.format("parse_rsp, userpubkey in response is invalid."));
        }
        ByteString data = ByteString.of(buff, 4 + hlen, qlen);
        byte[] plain = Ecc.decrypt(data.toByteArray(), userprikey, srvkey);
        CRC32 crc32 = new CRC32();
        crc32.update(plain);
        crc32.update(String.format(Locale.ENGLISH, "%d", cmd).getBytes());
        int icrc = (int) (crc32.getValue() & 0xFFFFFFFF);
        if (icrc != rcrc) {
            throw new IOException(
                    String.format("parse_rsp:%d, crc32 check failed:%d != %d(in header)", wantcmd, icrc, rcrc));
        }
        return plain;
    }

    private byte[] bcopy(byte[] data) {
        byte[] d = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            d[i] = data[i];
        }
        return d;
    }

    private static boolean isEqual(byte[] key1, byte[] key2) {
        if (null == key1 || null == key2) {
            return false;
        }

        if (key1.length != key2.length) {
            return false;
        }
        for (int i = 0; i < key1.length; i++) {
            if (key1[i] != key2[i]) {
                return false;
            }
        }
        return true;
    }

    private static class UpRecvThread implements Runnable {

        private final Uploader uploader;

        public UpRecvThread(Uploader uploader) {
            this.uploader = uploader;
        }

        @Override
        public void run() {
            uploader.do_recv_up_result();
        }

    }

}
