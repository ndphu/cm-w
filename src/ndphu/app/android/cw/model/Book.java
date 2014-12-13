package ndphu.app.android.cw.model;

import garin.artemiy.sqlitesimple.library.annotations.Column;
import garin.artemiy.sqlitesimple.library.annotations.Table;

import java.util.ArrayList;
import java.util.List;

@Table(name = "book")
public class Book {
	public static final String COL_ID = "book_id";
	public static final String COL_NAME = "name";
	public static final String COL_URL = "url";
	public static final String COL_DESC = "desc";
	public static final String COL_SOURCE_NAME = "source_name";
	public static final String COL_CURRENT_CHAPTER = "current_chapter";
	public static final String COL_FAVORITE = "favorite";
	public static final String COL_HASED_URL = "hased_url";
	public static final String COL_COVER = "cover";
	@Column(name = COL_ID, isPrimaryKey = true)
	private Long id;

	@Column(name = COL_NAME)
	private String name;

	@Column(name = COL_COVER)
	private String cover;

	@Column(name = COL_DESC)
	private String description;

	@Column(name = COL_URL)
	private String bookUrl;

	@Column(name = COL_CURRENT_CHAPTER)
	private Integer currentChapter = -1;

	@Column(name = COL_FAVORITE)
	private Boolean favorite = false;

	@Column(name = COL_HASED_URL)
	private String hasedUrl;

	@Column(name = COL_SOURCE_NAME)
	private String sourceName;

	private Source source;
	private List<Chapter> chapters = new ArrayList<Chapter>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public List<Chapter> getChapters() {
		return chapters;
	}

	public void setChapters(List<Chapter> chapters) {
		this.chapters = chapters;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String bookDesc) {
		this.description = bookDesc;
	}

	public String getBookUrl() {
		return bookUrl;
	}

	public void setBookUrl(String bookUrl) {
		this.bookUrl = bookUrl;
		this.setHasedUrl(ndphu.app.android.cw.util.Utils.getMD5Hash(bookUrl));
	}

	public int findChapterIdx(String chapterUrl) {
		for (int i = 0; i < chapters.size(); ++i) {
			Chapter chapter = chapters.get(i);
			if (chapter.getUrl().equals(chapterUrl)) {
				return i;
			}
		}
		return -1;
	}

	public boolean hasNext(String currentChapterUrl) {
		if (currentChapterUrl == null) {
			return false;
		}
		return findChapterIdx(currentChapterUrl) < chapters.size() - 1;
	}

	public boolean hasPrevious(String currentChapterUrl) {
		if (currentChapterUrl == null) {
			return false;
		}
		return findChapterIdx(currentChapterUrl) > 0;
	}

	public Integer getCurrentChapter() {
		return currentChapter;
	}

	public void setCurrentChapter(Integer currentChapter) {
		this.currentChapter = currentChapter;
	}

	public Boolean getFavorite() {
		return favorite;
	}

	public void setFavorite(Boolean favorite) {
		this.favorite = favorite;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Source getSource() {
		if (source == null) {
			source = Source.valueOf(getSourceName());
		}
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
		this.setSourceName(source.name());
	}

	public String getHasedUrl() {
		return hasedUrl;
	}

	public void setHasedUrl(String hasedUrl) {
		this.hasedUrl = hasedUrl;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String bookSourceName) {
		this.sourceName = bookSourceName;
	}

}