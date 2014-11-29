package ndphu.app.android.cw.model;

public class BookEntry {
	public String bookName;
	public String bookUrl;
	public String bookSource;

	@Override
	public String toString() {
		return bookName + ";" + bookUrl;
	}
}