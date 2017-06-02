package tcpupload;

/**
 * Created by nuodundd on 16/4/28.
 */
public class UploadFileConfigBean {
    String filePath;
    String encryptFilePath;
    String uuid;
    String mediaAesKey;
    long fileSize;
    String fileType;
    public UploadFileConfigBean(String filePath,String encryptFilePath,
                                String uuid,String mediaAesKey,long fileSize,String fileType) {
        this.filePath = filePath;
        this.encryptFilePath =encryptFilePath;
        this.uuid = uuid;
        this.mediaAesKey = mediaAesKey;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }
}
