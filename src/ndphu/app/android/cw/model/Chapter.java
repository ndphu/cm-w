package ndphu.app.android.cw.model;

import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.model.SearchResult.Source;

public class Chapter {
		private String name;
		private int chapterOrder;
		private String chapterUrl;
		private List<Page> pages = new ArrayList<Page>();
		private Source chapterSource;

		public void debug() {
			System.out.println("	Chapter " + chapterOrder + "; Name = " + getName());
			for (Page page : getPages()) {
				page.debug();
			}
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getChapterOrder() {
			return chapterOrder;
		}
		public void setChapterOrder(int chapterOrder) {
			this.chapterOrder = chapterOrder;
		}
		public List<Page> getPages() {
			return pages;
		}
		public void setPages(List<Page> pages) {
			this.pages = pages;
		}
		public String getChapterUrl() {
			return chapterUrl;
		}
		public void setChapterUrl(String chapterUrl) {
			this.chapterUrl = chapterUrl;
		}
		public Source getChapterSource() {
			return chapterSource;
		}
		public void setChapterSource(Source chapterSource) {
			this.chapterSource = chapterSource;
		}
	}