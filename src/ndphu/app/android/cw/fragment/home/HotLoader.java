package ndphu.app.android.cw.fragment.home;

import java.util.List;

import ndphu.app.android.cw.io.processor.Manga24hProcessor;
import ndphu.app.android.cw.model.HomePageItem;

public class HotLoader implements HomeLoader {

	@Override
	public List<HomePageItem> getHomePageData() {
		return new Manga24hProcessor().getHomePages();
	}

}
