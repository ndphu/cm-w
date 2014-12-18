package ndphu.app.android.cw.dao;

import java.util.List;

import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.Page;
import android.content.Context;

public class DaoUtils {
	public static void savePagesInChapter(Context context, Chapter chapter) {
		PageDao dao = new PageDao(context);
		for (Page p : chapter.getPages()) {
			p.setChapterId(chapter.getId());
			dao.create(p);
		}
	}
	
	public static void loadPagesOfChapter(Context context, Chapter chapter) {
		PageDao dao = new PageDao(context);
		List<Page> pages = dao.readAllWhere(Page.COL_CHAPTER_ID, chapter.getId() + "");
		chapter.getPages().clear();
		chapter.getPages().addAll(pages);
	}
}
