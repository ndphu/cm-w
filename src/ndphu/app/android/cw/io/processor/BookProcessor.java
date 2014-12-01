package ndphu.app.android.cw.io.processor;

import java.io.IOException;
import java.util.List;

import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.Page;

public interface BookProcessor {
	public List<Book> search(String token);
	public Book loadBook(String bookUrl, boolean complete) throws Exception;
	public List<Page> getPageList(String chapterUrl) throws IOException;
	public String getCoverUrl(String bookUrl) throws Exception;
	public List<HomePageItem> getHomePages();
}
