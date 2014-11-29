package ndphu.app.android.cw.io.processor;

import java.io.IOException;
import java.util.List;

import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Page;

public interface BookProcessor {
	public List<Book> searchOnline(String token);
	public Book prepareOnlineBook(String bookUrl, boolean complete) throws Exception;
	public List<Page> getChapterPages(String chapterUrl) throws IOException;
	public String getBookCoverLink(String bookUrl) throws Exception;
}
