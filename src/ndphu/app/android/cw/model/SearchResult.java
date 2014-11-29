package ndphu.app.android.cw.model;

public class SearchResult {
	public String bookName;
	public String bookCoverLink;
	public String bookDesc;
	public String bookLink;
	public String bookSource;

	public SearchResult() {

	}

	public SearchResult(String bookName, String bookDesc, String bookLink, String bookCoverLink, String bookSource) {
		this.bookName = bookName;
		this.bookDesc = bookDesc;
		this.bookCoverLink = bookCoverLink;
		this.bookLink = bookLink;
		this.bookSource = bookSource;
	}
}
