package ndphu.app.android.cw.app;

import garin.artemiy.sqlitesimple.library.SQLiteSimple;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Chapter;
import android.app.Application;

public class MainApplication extends Application {
	private final static int DATABASE_VERSION = 14;

	@Override
	public void onCreate() {
		super.onCreate();
		SQLiteSimple databaseSimple = new SQLiteSimple(this, DATABASE_VERSION);
		databaseSimple.create(Book.class, Chapter.class);
	}
}
