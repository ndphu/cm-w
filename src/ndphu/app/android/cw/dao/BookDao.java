package ndphu.app.android.cw.dao;

import garin.artemiy.sqlitesimple.library.SQLiteSimpleDAO;
import ndphu.app.android.cw.model.Book;
import android.content.Context;

public class BookDao extends SQLiteSimpleDAO<Book> {
	public BookDao(Context context) {
		super(Book.class, context);
	}
}
