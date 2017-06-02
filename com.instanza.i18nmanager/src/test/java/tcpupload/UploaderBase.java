package tcpupload;

/**
 * Created by nuodundd on 16/5/20.
 */
public interface UploaderBase {
    public UploadResult resume(byte[] prikey, byte[] pubkey, byte[] srvkey) throws Exception;
    public void cancel();
}
