package ndphu.app.android.cw.io.processor;

import java.io.IOException;
import java.util.List;

import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Category;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.Page;
import ndphu.app.android.cw.model.SearchResult;

public class TT8Processor implements BookProcessor {

	@Override
	public List<SearchResult> search(String token) {
		return null;
	}

	@Override
	public Book loadBook(String bookUrl, boolean complete) throws Exception {
		return null;
	}

	@Override
	public List<Page> getPageList(String chapterUrl) throws IOException {
		return null;
	}

	@Override
	public String getCoverUrl(String bookUrl) throws Exception {
		return null;
	}

	@Override
	public List<HomePageItem> getHomePages() {
		return null;
	}

	@Override
	public List<HomePageItem> getHomePageItemByCategory(Category category, int page) {
		return null;
	}

}
