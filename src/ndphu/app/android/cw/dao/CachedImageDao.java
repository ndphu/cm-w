package ndphu.app.android.cw.dao;

import garin.artemiy.sqlitesimple.library.SQLiteSimpleDAO;
import ndphu.app.android.cw.model.CachedImage;
import android.content.Context;

public class CachedImageDao extends SQLiteSimpleDAO<CachedImage> {
	public CachedImageDao(Context context) {
		super(CachedImage.class, context);
	}
}
