package ndphu.app.android.cw.dao;

import java.util.List;

import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.CachedImage;
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
	private static CachedImageDao cachedImageDao;

	public static void initialize(Context context) {
		bookDao = new BookDao(context);
		chapterDao = new ChapterDao(context);
		pageDao = new PageDao(context);
		cachedImageDao = new CachedImageDao(context);
	}

	public static Book saveOrUpdate(Book book) {
		if (isValidId(book.getId())) {
			bookDao.update(book.getId(), book);
		} else {
			long newBookId = bookDao.create(book);
			book.setId(newBookId);
			for (Chapter chapter : book.getChapters()) {
				chapter.setBookId(newBookId);
				saveOrUpdate(chapter);
			}
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
			for (Page p : pageDao.readAllWhere(Page.COL_CHAPTER_ID, chapter.getId() + "")) {
				pageDao.delete(p.getId());
			}
			for (Page p : chapter.getPages()) {
				p.setChapterId(chapter.getId());
				pageDao.create(p);
			}
		} else {
			// create
			long newId = chapterDao.create(chapter);
			chapter.setId(newId);
			for (Page p : chapter.getPages()) {
				p.setChapterId(newId);
				pageDao.create(p);
			}
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

	public static void loadPagesToChapter(Chapter chapter) {
		chapter.getPages().clear();
		chapter.getPages().addAll(pageDao.readAllWhere(Page.COL_CHAPTER_ID, chapter.getId() + ""));
	}

	/**
	 * Check if the book is added to db or not
	 *
	 * @param id
	 * @return
	 */
	private static boolean isValidId(Long id) {
		return id != null && id > 0;
	}

	public static void saveOrUpdate(CachedImage cachedImage) {
		if (isValidId(cachedImage.getId())) {
			// update
			cachedImageDao.update(cachedImage.getId(), cachedImage);
		} else {
			// save
			cachedImageDao.create(cachedImage);
		}
	}

	public static CachedImage getCachedImageByUrl(String url) {
		List<CachedImage> cachedList = cachedImageDao.readAllWhere(CachedImage.COL_URL, url);
		if (cachedList.size() == 0) {
			return null;
		} else {
			return cachedList.get(0);
		}

	}

	public static CachedImage getCachedImageByHasedUrl(String hasedUrl) {
		List<CachedImage> cachedList = cachedImageDao.readAllWhere(CachedImage.COL_HASED_URL, hasedUrl);
		if (cachedList.size() == 0) {
			return null;
		} else {
			return cachedList.get(0);
		}
	}

}
