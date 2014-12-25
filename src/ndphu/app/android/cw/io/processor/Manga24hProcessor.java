package ndphu.app.android.cw.io.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.io.Utils;
import ndphu.app.android.cw.io.parser.BasicParser;
import ndphu.app.android.cw.io.parser.BasicParser.LineHandler;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Category;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.Page;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.model.Source;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Manga24hProcessor implements BookProcessor {
	private static final String SEARCH_URL_TEMPLATE = "http://manga24h.com/index.php?module=user&act=ajax&feed_truyen2&q=%s";
	private static final String CHAPTER_PAGE_TEMPLATE = "http://manga24h.com/index.php?module=anime&act=ajax&resource=1&id=%s";
	private static final String TAG = Manga24hProcessor.class.getSimpleName();

	@Override
	public Book loadBook(String bookUrl, boolean complete) throws Exception {
		URL myUrl = new URL(bookUrl);
		String file = myUrl.getFile();
		String bookName = file.substring(file.lastIndexOf("/") + 1);
		String line;
		String coverUrl = null;
		String bookDesc = "Load later";
		List<String[]> chapters = new ArrayList<String[]>();
		BufferedReader in = new BufferedReader(new InputStreamReader(myUrl.openStream()));
		while ((line = in.readLine()) != null) {
			if (line.contains("<img itemprop='image'")) {
				coverUrl = line.substring(line.lastIndexOf("src=") + 5, line.lastIndexOf("class") - 2);
			} else if (line.contains("<option value=")) {
				String chapterUrl = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("/\">"));
				String chapterName = line.substring(line.indexOf("\">") + 2, line.lastIndexOf("<"));
				chapters.add(new String[] { chapterName, chapterUrl });
			} else if (line.contains("<div class=\"noidung\" itemprop='description'>")) {
				Log.i(TAG, "Line of book desc");
				StringBuilder sb = new StringBuilder();
				while ((line = in.readLine()) != null) {
					if (line.contains("</div>")) {
						break;
					}
					sb.append(line);
					sb.append(" ");
				}
				bookDesc = sb.toString();
			}
		}
		in.close();

		Book book = new Book();
		book.setBookUrl(bookUrl);
		book.setSource(Source.MANGA24H);
		book.setName(bookName);
		book.setCover(coverUrl);
		book.setDescription(bookDesc);
		int len = chapters.size();
		for (int i = 0; i < len; i++) {
			Chapter chapter = new Chapter();
			chapter.setName(chapters.get(i)[0]);
			chapter.setUrl(chapters.get(i)[1]);
			chapter.setSource(Source.MANGA24H);
			chapter.setPages(complete ? getPageList(chapters.get(i)[1]) : new ArrayList<Page>());
			book.getChapters().add(chapter);
		}
		// don't reverse anymore
		// Collections.reverse(book.getChapters());

		return book;
	}

	@Override
	public List<Page> getPageList(String chapterUrl) throws IOException {
		List<Page> pages = new ArrayList<Page>();
		String chapId = chapterUrl.substring(chapterUrl.indexOf("//") + 2);
		chapId = chapId.substring(chapId.indexOf("/") + 1);
		chapId = chapId.substring(0, chapId.indexOf("/"));
		Log.e(TAG, chapId);
		String data = null;
		String url = String.format(CHAPTER_PAGE_TEMPLATE, chapId);
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpGet get = new HttpGet(url);
			get.addHeader("X-Requested-With", "XMLHttpRequest");
			HttpResponse execute = client.execute(get);
			data = IOUtils.toString(execute.getEntity().getContent());
			String[] split = data.split("\\|");
			Log.i(TAG, split.length + " pages");
			for (String line : split) {
				line = line.trim();
				if (line == null || line.isEmpty()) {
					continue;
				}
				Page page = new Page();
				page.setUrl(line);
				page.setHashedUrl(ndphu.app.android.cw.util.Utils.getMD5Hash(line));
				pages.add(page);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return pages;
	}

	@Override
	public List<SearchResult> search(String token) {

		List<SearchResult> result = new ArrayList<SearchResult>();
		try {
			String url = String.format(SEARCH_URL_TEMPLATE, URLEncoder.encode(token, "UTF-8"));
			byte[] rawDataFromURL = Utils.getRawDataFromURL(url);
			if (rawDataFromURL == null) {
				return new ArrayList<SearchResult>();
			}
			String response = new String(rawDataFromURL);
			JSONArray arr = new JSONArray(response);
			for (int i = 0; i < arr.length(); ++i) {
				JSONObject bookJson = arr.getJSONObject(i);
				String bookUrl = bookJson.getString("id");
				if (bookUrl.equals("0")) {
					continue;
				}
				SearchResult book = new SearchResult(bookJson.getString("text"), bookUrl, Source.MANGA24H);
				result.add(book);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String getCoverUrl(String bookUrl) throws Exception {
		Log.i(TAG, "Get cover url requested");
		URL myUrl = new URL(bookUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(myUrl.openStream()));
		String line = null;
		while ((line = in.readLine()) != null) {
			if (line.contains("<img itemprop='image'")) {
				in.close();
				String coverUrl = line.substring(line.lastIndexOf("src=") + 5, line.lastIndexOf("class") - 2);
				Log.i(TAG, "Got cover url:" + coverUrl);
				return coverUrl;
			}
		}
		in.close();
		Log.i(TAG, "Cannot get cover url");
		return null;
	}

	@Override
	public List<HomePageItem> getHomePages() {
		final List<HomePageItem> result = new ArrayList<HomePageItem>();
		try {
			BasicParser.processLineByLine("http://manga24h.com/", new LineHandler() {
				HomePageItem item = null;

				@Override
				public boolean processLine(String line) {
					line = line.trim();
					if (line.length() == 0) {
						return false;
					}
					if (line.contains("item_anime_box")) {
						item = new HomePageItem();
					} else if (line.contains("lazy img-responsive")) {
						item.cover = line.substring(line.lastIndexOf("http"), line.lastIndexOf("\" style"));
					} else if (line.contains("<div class=\"title\">")) {
						item.bookUrl = line.substring(line.indexOf("http"), line.lastIndexOf("\">"));
						item.bookName = line.substring(line.lastIndexOf("\">") + 2, line.lastIndexOf("</a>"));
					} else if (line.contains("<div class=\"title2\">")) {
						item.chapterUrl = line.substring(line.indexOf("http"), line.lastIndexOf("\">"));
						item.chapterName = line.substring(line.lastIndexOf("\">") + 2, line.lastIndexOf("</a>"));
						result.add(item);
					}
					return false;
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	@Override
	public List<HomePageItem> getHomePageItemByCategory(Category category, int page) {
		return null;
	}

}
