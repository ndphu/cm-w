package ndphu.app.android.cw.fragment.home;

import java.util.List;

import ndphu.app.android.cw.io.processor.TT8Processor;
import ndphu.app.android.cw.model.Category;
import ndphu.app.android.cw.model.HomePageItem;

public class TT8HomeLoader implements HomeLoader {

	@Override
	public List<HomePageItem> getHomePageData() {
		return new TT8Processor().getHomePages();
	}

	@Override
	public List<HomePageItem> getByCategory(Category category, int page) {
		return new TT8Processor().getHomePageItemByCategory(category, page);
	}

	@Override
	public int getMaxItemSize() {
		return 15;
	}

}
