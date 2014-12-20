package ndphu.app.android.cw.dao;

import java.util.List;

import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.Page;
import android.content.Context;

/**
 * @author ndphu
 *
 */
public class DaoUtils {
	private static ChapterDao chapterDao;
	private static BookDao bookDao;
	private static PageDao pageDao;

	public static void initialize(Context context) {
		bookDao = new BookDao(context);
		chapterDao = new ChapterDao(context);
		pageDao = new PageDao(context);
	}

	public static Book saveOrUpdate(Book book) {
		long newBookId = bookDao.create(book);
		book.setId(newBookId);
		for (Chapter chapter : book.getChapters()) {
			chapter.setBookId(newBookId);
			saveOrUpdate(chapter);
		}
		return book;
	}

	public static Book updateBook(Book book) {
		bookDao.update(book.getId(), book);
		return book;
	}

	public static Book getBookByHasedUrl(String hasedUrl) {
		List<Book> books = bookDao.readAllWhere(Book.COL_HASED_URL, hasedUrl);
		if (books.isEmpty()) {
			return null;
		}
		Long bookId = books.get(0).getId();
		return getBookAndChapters(bookId);
	}

	public static List<Book> getFavoriteBooks() {
		List<Book> favoriteBooks = bookDao.readAllWhere(Book.COL_FAVORITE, "1");
		for (Book book : favoriteBooks) {
			loadChaptersOfBook(book);
		}
		return favoriteBooks;
	}

	/**
	 * Get book by id and all of its chapters
	 * 
	 * @param bookId
	 * @return
	 */
	public static Book getBookAndChapters(Long bookId) {
		Book book = bookDao.read(bookId);
		loadChaptersOfBook(book);
		return book;
	}

	private static void loadChaptersOfBook(Book book) {
		List<Chapter> chapters = chapterDao.readAllWhere(Chapter.COL_BOOK_ID, book.getId() + "");
		book.setChapters(chapters);
	}

	public static Chapter saveOrUpdate(Chapter chapter) {
		if (isValidId(chapter.getId())) {
			// update
			chapterDao.update(chapter.getId(), chapter);
		} else {
			// create
			long newId = chapterDao.create(chapter);
			chapter.setId(newId);
		}
		return chapter;
	}

	/**
	 * Get chapter by id
	 * 
	 * @param chapterId
	 * @return
	 */
	public static Chapter getChapterById(Long chapterId) {
		return chapterDao.read(chapterId);
	}

	/**
	 * Get chapter and all of their pages
	 * 
	 * @param chapterId
	 * @return
	 */
	public static Chapter getChapterAndPages(Long chapterId) {
		Chapter chapter = getChapterById(chapterId);
		List<Page> pages = pageDao.readAllWhere(Page.COL_CHAPTER_ID, chapterId + "");
		chapter.setPages(pages);
		return chapter;
	}

	private static boolean isValidId(Long id) {
		return id != null && id > 0;
	}

}
