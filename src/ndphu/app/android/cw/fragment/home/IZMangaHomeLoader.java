package ndphu.app.android.cw.fragment.home;

import java.util.List;

import ndphu.app.android.cw.io.processor.IZMangaProcessor;
import ndphu.app.android.cw.model.Category;
import ndphu.app.android.cw.model.HomePageItem;

public class IZMangaHomeLoader implements HomeLoader {

	@Override
	public List<HomePageItem> getHomePageData() {
		return new IZMangaProcessor().getHomePages();
	}

	@Override
	public List<HomePageItem> getByCategory(Category category, int page) {
		return new IZMangaProcessor().getHomePageItemByCategory(category, page);
	}

	@Override
	public int getMaxItemSize() {
		return 16;
	}

}
