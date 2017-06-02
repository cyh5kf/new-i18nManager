package tcpupload;

/**
 * Created by nuodundd on 16/5/3.
 */
public interface ErrorCode {
    //出现错误需要重新建立连接
    int CocoErrorcode_UPLOAD_INVALID_OFFSET = 19001; // 上传offset错误，会返回已记录的offset，需重连&上传
    int CocoErrorcode_UPLOAD_SAVE_EXCEPTION = 19002; // save时异常；
    int CocoErrorcode_UPLOAD_INVALID_USER = 19003; // 用户非法
    int CocoErrorcode_UPLOAD_WAIT_STREAM = 19004; // 未出现，忽略
    int CocoErrorcode_UPLOAD_FILE_TOOLARGE= 19005; // 文件太大
    int CocoErrorcode_UPLOAD_INVALID_PARAM= 19006; // 参数错误
    int CocoErrorcode_UPLOAD_FILE_CORRUPTED= 19007; // 文件MD5校验失败
    int CocoErrorcode_UPLOAD_BLOCK_CORRUPTED= 19008; // 上传block crc校验失败，上传的tcp流，基于block做ecc加密，并crc校验；大小可选，比如1024Byte一个block
}
