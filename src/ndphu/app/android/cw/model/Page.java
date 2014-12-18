package ndphu.app.android.cw.model;

import garin.artemiy.sqlitesimple.library.annotations.Column;
import garin.artemiy.sqlitesimple.library.annotations.Table;

@Table(name = "page")
public class Page {
	public static final String COL_ID = "page_id";
	public static final String COL_URL = "url";
	public static final String COL_HASED_URL = "hased_url";
	public static final String COL_CHAPTER_ID = "chapter_id";

	@Column(name = COL_ID, isPrimaryKey = true)
	private Long pageId;

	@Column(name = COL_URL)
	private String url;

	@Column(name = COL_HASED_URL)
	private String hashedUrl;
	
	@Column(name = COL_CHAPTER_ID)
	private Long chapterId;

	public String getUrl() {
		return url;
	}

	public void setUrl(String link) {
		this.url = link;
	}

	public String getHashedUrl() {
		return hashedUrl;
	}

	public void setHashedUrl(String hashedUrl) {
		this.hashedUrl = hashedUrl;
	}

	public Long getId() {
		return pageId;
	}

	public void setId(Long pageId) {
		this.pageId = pageId;
	}

	public Long getChapterId() {
		return chapterId;
	}

	public void setChapterId(Long chapterId) {
		this.chapterId = chapterId;
	}
}