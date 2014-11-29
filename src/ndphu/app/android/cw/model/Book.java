package ndphu.app.android.cw.model;

import java.util.ArrayList;
import java.util.List;

public class Book {
	private String name;
	private String cover;
	private List<Chapter> chapters = new ArrayList<Chapter>();
	private String desc;
	private String bookUrl;

	public void debug() {
		System.out.println("Bookname: " + getName());
		System.out.println("Cover: " + getCover());
		for (Chapter chapter : getChapters()) {
			chapter.debug();
		}
	}

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

	public String getBookDesc() {
		return this.desc;
	}

	public void setBookDesc(String bookDesc) {
		this.desc = bookDesc;
	}

	public String getBookUrl() {
		return bookUrl;
	}

	public void setBookUrl(String bookUrl) {
		this.bookUrl = bookUrl;
	}

	public int findChapterIdx(String chapterUrl) {
		for (int i = 0; i < chapters.size(); ++i) {
			Chapter chapter = chapters.get(i);
			if (chapter.getChapterUrl().equals(chapterUrl)) {
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

}