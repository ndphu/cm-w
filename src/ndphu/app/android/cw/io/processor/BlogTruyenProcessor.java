package ndphu.app.android.cw.io.processor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class BlogTruyenProcessor implements BookProcessor {

	private static final String TAG = BlogTruyenProcessor.class.getSimpleName();
	private static final String PREFIX = "http://blogtruyen.com";
	private static final String QUICK_SEARCH = "http://blogtruyen.com/Partial/QuickSearch";

	@Override
	public List<SearchResult> search(String token) {
		final List<SearchResult> result = new ArrayList<SearchResult>();
		Log.i(TAG, "Search token: " + token);
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(QUICK_SEARCH);
		HttpResponse response = null;
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("keyword", token));
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = client.execute(post);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (response != null) {
			try {
				BasicParser.processLineByLine(response.getEntity().getContent(), new LineHandler() {

					@Override
					public boolean processLine(String line) {
						line = line.trim();
						if (line.contains("href=")) {
							Log.d(TAG, "Got book: " + line);
							String bookName = line.substring(line.lastIndexOf("\">") + 2, line.lastIndexOf("</a>"));
							String bookUrl = line.substring(line.indexOf("\"") + 1, line.indexOf("\">"));
							bookUrl = PREFIX + bookUrl;
							SearchResult searchResult = new SearchResult(bookName, bookUrl, Source.BLOGTRUYEN);
							result.add(searchResult);
						}
						return false;
					}
				});
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public Book loadBook(String bookUrl, boolean complete) throws Exception {
		final Book book = new Book();
		book.setBookUrl(bookUrl);
		final StringBuilder content = new StringBuilder();

		BasicParser.processLineByLine(bookUrl, new LineHandler() {
			boolean isInContentPart = false;

			@Override
			public boolean processLine(String line) {
				line = line.trim();
				if (isInContentPart) {
					content.append(line);
					String currentContent = content.toString();
					isInContentPart = !(Utils.countString(currentContent, "<div") == Utils.countString(currentContent, "</div>"));
					return false;
				}
				if (line.startsWith("<title>")) {
					// Title
					book.setName(line.substring(line.indexOf(">") + 1, line.indexOf("-")));
				} else if (line.contains("image_src")) {
					// Cover
					book.setCover(line.substring(line.indexOf("http"), line.lastIndexOf("\"")));
				} else if (line.startsWith("<div class=\"detail\">")) {
					content.append(line);
					isInContentPart = true;
				} else if (line.startsWith("<span class=\"title\">")) {
					Chapter chapter = new Chapter();
					chapter.setName(line.substring(line.lastIndexOf("\">") + 2, line.indexOf("</a>")));
					chapter.setChapterOrder(0);
					chapter.setChapterUrl(PREFIX + line.substring(line.indexOf("/truyen"), line.indexOf("\" title")));
					chapter.setChapterSource(Source.BLOGTRUYEN);
					book.getChapters().add(chapter);
				}
				return false;
			}
		});
		book.setBookDesc(content.toString());
		return book;
	}

	@Override
	public List<Page> getPageList(String chapterUrl) throws IOException {
		List<Page> result = new ArrayList<Page>();
		final StringBuilder sb = new StringBuilder();
		BasicParser.processLineByLine(chapterUrl, new LineHandler() {
			boolean isPagePart = false;

			@Override
			public boolean processLine(String line) {
				line = line.trim();
				if (isPagePart) {
					sb.append(line);
				}
				if (line.contains("<article id=\"content\">")) {
					isPagePart = true;
				} else if (line.contains("</article>")) {
					isPagePart = false;
				}
				return false;
			}
		});
		String imgs = sb.toString();
		Log.i(TAG, "Raw data" + imgs);
		String[] imgsArray = imgs.split("<img src=");
		for (String img : imgsArray) {
			if (img == null || img.trim().isEmpty()) {
				continue;
			}
			Log.i(TAG, "Page img: " + img);
			Page page = new Page();
			String pageUrl = img.substring(img.indexOf("http"), img.lastIndexOf("\""));
			String hashedUrl = Utils.getMD5Hash(pageUrl);
			page.setHashedUrl(hashedUrl);
			page.setLink(pageUrl);
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

}
