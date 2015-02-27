package ndphu.app.android.cw.io.processor;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Category;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.Page;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.model.Source;
import ndphu.app.android.cw.util.Utils;

import org.apache.commons.io.IOUtils;

import android.util.Log;

public class TT8Processor implements BookProcessor {

	private static final String TAG = TT8Processor.class.getSimpleName();

	static String HOST = "m.truyentranh8.net";

	static Map<Category, String> URL_MAP = new HashMap<Category, String>();

	static Pattern HOME_PAGE_ITEM = Pattern
			.compile(
					"\\<a href=\"(.*?)\" class=\"post\"(.*?)data-original=\"//(.*?)\"(.*?)\\<h2 class=\"title\"\\>(.*?)\\</h2\\>",
					Pattern.DOTALL);

	static Pattern BOOK = Pattern
			.compile(
					"<meta property=\"og:title\" content=\"(.*?)\"(.*?)property=\"og:description\" content=\"(.*?)\"(.*?)\\<span itemprop=\"image\"\\>(.*?)\\</span\\>",
					Pattern.DOTALL);

	static Pattern CHAPTER = Pattern.compile("\\<a href=\"(.*?)\">(.*?)\\<h3\\>(.*?)\\</h3\\>", Pattern.DOTALL);

	static Pattern PAGE = Pattern.compile("lstImages\\.push\\(\"(.*?)\"\\)");

	static boolean KEEP_CREDIT = false;

	static {
		URL_MAP.put(Category.NEW, "/");
		URL_MAP.put(Category.ACTION, "loai/Action-52/");
		URL_MAP.put(Category.ADULT, "loai/Anime-107/");
		URL_MAP.put(Category.DOUJINSHI, "loai/Doujinshi-72/");
		URL_MAP.put(Category.DRAMA, "loai/Drama-73/");
		URL_MAP.put(Category.ECCHI, "loai/Ecchi-74/");
		URL_MAP.put(Category.GENDER_BENDER, "loai/GenderBender-76/");
		URL_MAP.put(Category.HORROR, "loai/Horror-79/");
		URL_MAP.put(Category.JOSEI, "loai/Josei-80/");
		URL_MAP.put(Category.MATURE, "loai/Mature-85/");
		URL_MAP.put(Category.MECHA, "loai/Mecha-86/");
		URL_MAP.put(Category.MYSTERY, "loai/Mystery-87/");
		URL_MAP.put(Category.ROMANCE, "loai/Romance-90/");
		URL_MAP.put(Category.SCHOOL_LIFE, "loai/SchoolLife-91/");
		URL_MAP.put(Category.SCI_FI, "loai/Scifi-92/");
		URL_MAP.put(Category.SEINEN, "loai/Seinen-93/");
		URL_MAP.put(Category.SHOUJO, "loai/Shoujo-94/");
		URL_MAP.put(Category.SHOUJO_AI, "loai/Shoujo-Ai-66/");
		URL_MAP.put(Category.SHOUNEN, "loai/Shounen-96/");
		URL_MAP.put(Category.SHOUNEN_AI, "loai/Shounen-Ai-97/");
		URL_MAP.put(Category.YAOI, "loai/Yaoi-114/");
		URL_MAP.put(Category.YURI, "loai/Yuri-111/");
		URL_MAP.put(Category.OVER_16, "s/DoTuoi/16/");
		URL_MAP.put(Category.OVER_18, "s/DoTuoi/18/");
	}

	private static String buildURL(Category category, int page) {
		if (URL_MAP.get(category) == null) {
			return null;
		}
		return "http://" + HOST + "/" + URL_MAP.get(category) + "page=" + page;
	}

	@Override
	public List<SearchResult> search(String token) {
		return null;
	}

	@Override
	public Book loadBook(String bookUrl, boolean complete) throws Exception {
		Book book = new Book();
		book.setBookUrl(bookUrl);
		book.setSource(Source.TT8);
		String pageContent = IOUtils.toString(URI.create(bookUrl));
		Matcher matcher = BOOK.matcher(pageContent);
		while (matcher.find()) {
			String bookName = matcher.group(1);
			String bookDesc = matcher.group(3);
			String bookCover = matcher.group(5);
			book.setName(bookName);
			book.setDescription(bookDesc);
			book.setCover(bookCover);
		}

		Matcher chapterMatcher = CHAPTER.matcher(pageContent.substring(pageContent.indexOf("<ol>")));
		while (chapterMatcher.find()) {
			String chapterUrl = chapterMatcher.group(1);
			String chapterName = chapterMatcher.group(3).trim().replace("</br>", "");
			Chapter chapter = new Chapter();
			chapter.setSource(Source.TT8);
			chapter.setName(chapterName);
			chapter.setUrl(chapterUrl);
			chapter.setPages(new ArrayList<Page>());
			book.getChapters().add(chapter);
		}
		return book;
	}

	@Override
	public List<Page> getPageList(String chapterUrl) throws IOException {
		List<Page> result = new ArrayList<Page>();
		String pageContent = IOUtils.toString(URI.create(chapterUrl));
		Matcher matcher = PAGE.matcher(pageContent);
		while (matcher.find()) {
			Page page = new Page();
			String pageUrl = matcher.group(1).replace("?imgmax=0", "");
			if (pageUrl.contains("gioithieubanbe3.png")) {
				continue;
			}
			Log.w(TAG, "PageUrl = " + pageUrl);
			page.setUrl(pageUrl);
			page.setHashedUrl(Utils.getMD5Hash(pageUrl));
			result.add(page);
		}
		return result;
	}

	@Override
	public String getCoverUrl(String bookUrl) throws Exception {
		return null;
	}

	@Override
	public List<HomePageItem> getHomePages() {
		return null;
	}

	@Override
	public List<HomePageItem> getHomePageItemByCategory(Category category, int page) {
		List<HomePageItem> items = new ArrayList<HomePageItem>();
		try {
			String pageContent = IOUtils.toString(URI.create(buildURL(category, page + 1)));
			Matcher nameMatcher = HOME_PAGE_ITEM.matcher(pageContent.substring(pageContent
					.indexOf("<div class=\"content\">")));
			while (nameMatcher.find()) {
				HomePageItem item = new HomePageItem();
				String bookURL = nameMatcher.group(1);
				String bookCover = "http://" + nameMatcher.group(3);
				String bookName = nameMatcher.group(5);
				item.bookName = bookName;
				item.bookUrl = bookURL;
				item.cover = bookCover;
				item.source = Source.TT8;
				items.add(item);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return items;
	}

}
