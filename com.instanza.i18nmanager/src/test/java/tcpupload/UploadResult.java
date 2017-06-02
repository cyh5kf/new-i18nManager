package tcpupload;

import java.util.Arrays;

public class UploadResult {
	public final int rv;
	public final String url;
	final byte[] newsrvkey;
	public String mediaaeskey;
	public UploadResult(int rv, String url, byte[] newsrvkey) {
		this.rv = rv;
		this.url = url;
		this.newsrvkey = newsrvkey;
	}

	public UploadResult(int rv, String url, byte[] newsrvkey, String mediaaeskey) {
		this.rv = rv;
		this.url = url;
		this.newsrvkey = newsrvkey;
		this.mediaaeskey = mediaaeskey;
	}

	@Override
	public String toString() {
		return "UploadResult{" +
				"rv=" + rv +
				", url='" + url + '\'' +
				", newsrvkey=" + Arrays.toString(newsrvkey) +
				", mediaaeskey='" + mediaaeskey + '\'' +
				'}';
	}
}
