package ndphu.app.android.cw.dao;

import garin.artemiy.sqlitesimple.library.SQLiteSimpleDAO;

import java.util.List;

import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.Page;
import android.content.Context;

public class PageDao extends SQLiteSimpleDAO<Page> {
	public PageDao(Context context) {
		super(Page.class, context);
	}
}
