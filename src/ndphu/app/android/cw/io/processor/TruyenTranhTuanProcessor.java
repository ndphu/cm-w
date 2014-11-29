package ndphu.app.android.cw.io.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ndphu.app.android.cw.MainActivity;
import ndphu.app.android.cw.model.Book;
import ndphu.app.android.cw.model.BookEntry;
import ndphu.app.android.cw.model.Chapter;
import ndphu.app.android.cw.model.Page;

import org.apache.commons.io.IOUtils;

public class TruyenTranhTuanProcessor implements BookProcessor {
	private static final String BASE_URL = "http://truyentranhtuan.com";
	private static final String ALL_BOOKS_URL = "/components/list.php";

	private static String getList() throws IOException {
		URL allBookURL = new URL(BASE_URL + ALL_BOOKS_URL);
		return IOUtils.toString(allBookURL.openStream());
	}

	public static List<BookEntry> getAllBookEntries() throws IOException {
		List<BookEntry> result = new ArrayList<BookEntry>();
		String list = getList();
		String array = list.split("=")[1].trim();
		String[] entries = array.split("\\],\\[");
		entries[0] = entries[0].substring(2);
		entries[entries.length - 1] = entries[entries.length - 1].substring(0, entries[entries.length - 1].length() - 3);
		for (String entry : entries) {
			String[] split = entry.split(",");
			String title = split[0].trim().replace("\"", "");
			String file = split[1].trim().replace("\"", "");
			BookEntry bookEntry = new BookEntry();
			bookEntry.bookName = title;
			bookEntry.bookUrl = BASE_URL + file;
			bookEntry.bookSource = MainActivity.SOURCE_TRUYENTRANHTUAN;
			result.add(bookEntry);
		}
		return result;
	}

	@Override
	public Book loadBook(String bookUrl, boolean complete) throws Exception {
		URL myUrl = new URL(bookUrl);
		String file = myUrl.getFile();
		String bookName = file.substring(file.lastIndexOf("/") + 1);
		String line;
		String coverUrl = null;
		String bookDesc = null;
		List<String> chapters = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new InputStreamReader(myUrl.openStream()));
		while ((line = in.readLine()) != null) {
			if (line.contains("<div id=\"content-top\">")) {
				int idx1 = line.indexOf("<div class=\"title-logo1\">");
				int idx2 = line.indexOf("border=0");
				coverUrl = line.substring(idx1 + 35, idx2 - 2);
			} else if (line.contains("<div style='position:relative;margin-left:120px;left:0%;'>")) {
				bookDesc = in.readLine();
			} else if (line.contains("<td class=\"tbl_body\">") || line.contains("<td class=\"tbl_body2\">")) {
				String subUrl = line.substring(line.indexOf("/"), line.lastIndexOf("\">"));
				bookUrl = BASE_URL + subUrl + "doc-truyen/";
				chapters.add(bookUrl);
			}
		}
		in.close();

		Book book = new Book();
		book.setBookUrl(bookUrl);
		book.setName(bookName);
		book.setCover(coverUrl);
		book.setBookDesc(bookDesc);
		int len = chapters.size();
		for (int i = 0; i < len; i++) {
			Chapter chapter = new Chapter();
			chapter.setChapterUrl(chapters.get(i));
			chapter.setName(chapters.get(i));
			chapter.setChapterOrder(len - i);
			chapter.setPages(complete ? getPageList(chapters.get(i)) : new ArrayList<Page>());
			book.getChapters().add(chapter);
		}

		Collections.reverse(book.getChapters());

		return book;
	}

	@Override
	public List<Page> getPageList(String chapterUrl) throws IOException {
		List<Page> pages = new ArrayList<Page>();
		URL myUrl = new URL(chapterUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(myUrl.openStream()));

		String line;
		List<String> picUrlList = new ArrayList<String>();

		while ((line = in.readLine()) != null) {
			if (line.contains("var slides2")) {
				String rawPages = line.split("=")[1].trim();
				rawPages = rawPages.replace("\"", "");
				rawPages = rawPages.substring(1);
				rawPages = rawPages.substring(0, rawPages.length() - 2);
				String[] pagesSplited = rawPages.split(",");
				for (String pageUrl : pagesSplited) {
					picUrlList.add(BASE_URL + pageUrl);
				}
				Collections.sort(picUrlList, new Comparator<String>() {

					@Override
					public int compare(String lhs, String rhs) {
						return lhs.compareTo(rhs);
					}
				});
				break;
			}
		}
		in.close();

		for (int i = 0; i < picUrlList.size(); i++) {
			Page page = new Page();
			page.setLink(picUrlList.get(i));
			page.setPageOrder(i);
			pages.add(page);
		}

		return pages;
	}

	@Override
	public String getCoverUrl(String bookUrl) throws Exception {
		URL myUrl = new URL(bookUrl);
		String line;
		String coverUrl = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(myUrl.openStream()));
		while ((line = in.readLine()) != null) {
			if (line.contains("<div id=\"content-top\">")) {
				int idx1 = line.indexOf("<div class=\"title-logo1\">");
				int idx2 = line.indexOf("border=0");
				coverUrl = line.substring(idx1 + 35, idx2 - 2);
				System.out.println(line);
				break;
			}
		}
		in.close();
		return coverUrl;
	}

	@Override
	public List<Book> search(String token) {
		// TODO Auto-generated method stub
		return null;
	}

}
