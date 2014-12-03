package ndphu.app.android.cw.model;

public class SearchResult {

	public String bookName;
	public String bookUrl;
	public Source bookSource;

	public SearchResult(String bookName, String bookUrl, Source bookSource) {
		this.bookName = bookName;
		this.bookUrl = bookUrl;
		this.bookSource = bookSource;
	}
}
