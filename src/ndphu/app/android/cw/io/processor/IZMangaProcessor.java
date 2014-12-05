package ndphu.app.android.cw.io.processor;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import ndphu.app.android.cw.io.parser.BasicParser;
import ndphu.app.android.cw.io.parser.BasicParser.LineHandler;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.HomePageItem;
import ndphu.app.android.cw.model.Page;
import ndphu.app.android.cw.model.SearchResult;
import ndphu.app.android.cw.model.Source;
import ndphu.app.android.cw.util.Utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

public class IZMangaProcessor implements BookProcessor {

	private static final String SEARCH_URL = "http://izmanga.com/home/getjsonsearchstory?searchword=%s&search_style=tentruyen";
	private static final String BASE_URL = "http://izmanga.com/";
	private static final String TAG = IZMangaProcessor.class.getSimpleName();

	@Override
	public List<SearchResult> search(String token) {
		final List<SearchResult> result = new ArrayList<SearchResult>();
		try {
			String searchString = String.format(SEARCH_URL, URLEncoder.encode(token, "UTF-8"));
			String data = IOUtils.toString(URI.create(searchString));
			JSONArray arr = new JSONArray(data);
			for (int idx = 0; idx < arr.length(); ++idx) {
				JSONObject entry = arr.getJSONObject(idx);
				String bookName = entry.getString("name");
				Log.i(TAG, "Got book name: " + bookName);
				String alias = changeAlias(bookName);
				String bookUrl = BASE_URL + alias + "-" + entry.getString("id");
				Log.i(TAG, "Got book url: " + bookUrl);
				SearchResult item = new SearchResult(bookName, bookUrl, Source.IZMANGA);
				result.add(item);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressLint("DefaultLocale")
	private static String changeAlias(String alias) {
		String str = alias.trim();
		str = str.toLowerCase();
		str = str.replaceAll("à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ", "a");
		str = str.replaceAll("è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ", "e");
		str = str.replaceAll("ì|í|ị|ỉ|ĩ", "i");
		str = str.replaceAll("ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ", "o");
		str = str.replaceAll("ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ", "u");
		str = str.replaceAll("ỳ|ý|ỵ|ỷ|ỹ", "y");
		str = str.replaceAll("đ/g", "d");
		str = str.replaceAll("!|@|%|\\^|\\*|\\(|\\)|\\+|\\=|\\<|\\>|\\?|\\/|,|\\.|\\:|\\;|\\'| |\\\"|\\&|\\#|\\[|\\]|~|-|$", "_");
		/* tìm và thay thế các kí tự đặc biệt trong chuỗi sang kí tự - */
		str = str.replaceAll("_+", "_"); // thay thế 2- thành 1-
		str = str.replaceAll("^\\_+|\\_+$", "");
		// cắt bỏ ký tự - ở đầu và cuối chuỗi
		return str;
	}

	@Override
	public Book loadBook(String bookUrl, boolean complete) throws Exception {
		Log.i(TAG, "Load book: " + bookUrl);
		final Book book = new Book();
		final StringBuilder content = new StringBuilder();
		BasicParser.processLineByLine(bookUrl, new LineHandler() {

			private boolean isContentPart = false;
			private boolean isChapterPart;
			private StringBuilder chapterCheck = new StringBuilder();

			@Override
			public boolean processLine(String line) {
				if (isContentPart) {
					if (Utils.countString(content.toString(), "<div") <= Utils.countString(content.toString(), "</div>")) {
						isContentPart = false;
					} else {
						content.append(line);
					}
				} else if (isChapterPart) {
					if (Utils.countString(chapterCheck.toString(), "<div") <= Utils.countString(chapterCheck.toString(), "</div>")) {
						isChapterPart = false;
					} else {
						chapterCheck.append(line);
						if (line.contains("http")) {
							Log.i(TAG, "Chapter line:" + line);
							Chapter chapter = new Chapter();
							chapter.setChapterSource(Source.IZMANGA);
							String chapterName = line.substring(line.lastIndexOf("\">") + 2, line.lastIndexOf("</a>"));
							Log.i(TAG, "Chapter name:" + chapterName);
							chapter.setName(chapterName);
							String chapterUrl = line.substring(line.indexOf("http"), line.lastIndexOf("\" title"));
							Log.i(TAG, "Chapter url:" + chapterUrl);
							chapter.setChapterUrl(chapterUrl);
							book.getChapters().add(chapter);
						}
					}
				} else {
					line = line.trim();
					if (line.contains("og:title")) {
						book.setName(line.substring(line.lastIndexOf("=") + 2, line.lastIndexOf("\"")));
					} else if (line.contains("og:image")) {
						book.setCover(line.substring(line.lastIndexOf("=") + 2, line.lastIndexOf("\"")));
					} else if (line.contains("manga-info-content")) {
						isContentPart = true;
						content.append(line);
					} else if (line.contains("class=\"chapter-list\"")) {
						isChapterPart = true;
						chapterCheck.append(line);
					}
				}
				return false;
			}
		});
		Log.i(TAG, "Content: " + content.toString());
		book.setBookDesc(content.toString());
		return book;
	}

	@Override
	public List<Page> getPageList(String chapterUrl) throws IOException {
		List<Page> result = new ArrayList<Page>();
		final StringBuilder sb = new StringBuilder();
		BasicParser.processLineByLine(chapterUrl, new LineHandler() {

			@Override
			public boolean processLine(String line) {
				line = line.trim();
				if (line.startsWith("data = ")) {
					sb.append(line.substring(line.indexOf("http"), line.lastIndexOf("'")));
					return true;
				}
				return false;
			}
		});

		String[] urls = sb.toString().split("\\|");
		for (String url : urls) {
			Page p = new Page();
			p.setLink(url);
			p.setHashedUrl(Utils.getMD5Hash(url));
			result.add(p);
		}

		return result;
	}

	@Override
	public String getCoverUrl(String bookUrl) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<HomePageItem> getHomePages() {
		return null;
	}

}
