package ndphu.app.android.cw.dao;

import garin.artemiy.sqlitesimple.library.SQLiteSimpleDAO;

import java.util.List;

import ndphu.app.android.cw.model.Chapter;
import android.content.Context;

public class ChapterDao extends SQLiteSimpleDAO<Chapter> {
	public ChapterDao(Context context) {
		super(Chapter.class, context);
	}
}
