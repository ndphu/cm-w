package ndphu.app.android.cw.model;

import garin.artemiy.sqlitesimple.library.annotations.Column;
import garin.artemiy.sqlitesimple.library.annotations.Table;

@Table(name = "cached_image")
public class CachedImage {
	public static final String COL_ID = "id";
	public static final String COL_URL = "url";
	public static final String COL_HASED_URL = "hased_url";
	public static final String COL_FILE_PATH = "file_path";
	public static final String COL_SIZE = "file_size";
	@Column(name = COL_ID, isPrimaryKey = true)
	private Long id;
	@Column(name = COL_URL)
	private String url;
	@Column(name = COL_HASED_URL)
	private String hasedUrl;
	@Column(name = COL_FILE_PATH)
	private String filePath;
	@Column(name = COL_SIZE)
	private Long fileSize;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHasedUrl() {
		return hasedUrl;
	}

	public void setHasedUrl(String hasedUrl) {
		this.hasedUrl = hasedUrl;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
}
