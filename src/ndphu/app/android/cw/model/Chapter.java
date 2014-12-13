package ndphu.app.android.cw.model;

import garin.artemiy.sqlitesimple.library.annotations.Column;
import garin.artemiy.sqlitesimple.library.annotations.Table;

import java.util.ArrayList;
import java.util.List;

@Table(name = "chapter")
public class Chapter {
	public static final String COL_ID = "chapter_id";
	public static final String COL_NAME = "name";
	public static final String COL_URL = "url";
	public static final String COL_SOURCE_NAME = "source_name";
	public static final String COL_BOOK_ID = "book_id";

	@Column(name = COL_ID, isPrimaryKey = true)
	private Long id;
	@Column(name = COL_NAME)
	private String name;
	@Column(name = COL_URL)
	private String url;
	@Column(name = COL_SOURCE_NAME)
	private String sourceName;
	@Column(name = COL_BOOK_ID)
	private Long bookId;

	private List<Page> pages = new ArrayList<Page>();
	private Source source;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Page> getPages() {
		return pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String chapterUrl) {
		this.url = chapterUrl;
	}

	public Source getSource() {
		if (source == null) {
			source = Source.valueOf(getChapterSourceName());
		}
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
		this.setChapterSourceName(source.name());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBookId() {
		return bookId;
	}

	public void setBookId(Long bookId) {
		this.bookId = bookId;
	}

	public String getChapterSourceName() {
		return sourceName;
	}

	public void setChapterSourceName(String chapterSourceName) {
		this.sourceName = chapterSourceName;
	}
}