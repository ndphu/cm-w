package ndphu.app.android.cw.fragment.home;

import java.util.List;

import ndphu.app.android.cw.model.Category;
import ndphu.app.android.cw.model.HomePageItem;

public interface HomeLoader {
	public List<HomePageItem> getHomePageData();

	List<HomePageItem> getByCategory(Category category, int page);

	public int getMaxItemSize();
}
