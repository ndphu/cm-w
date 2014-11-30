package ndphu.app.android.cw.model;

public class Page {
	private String link;
	private int pageOrder;
	private String hashedUrl;

	public void debug() {
		System.out.println("		Page link: " + getLink());
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setPageOrder(int order) {
		this.pageOrder = order;
	}

	public int getPageOrder() {
		return this.pageOrder;
	}

	public String getHashedUrl() {
		return hashedUrl;
	}

	public void setHashedUrl(String hashedUrl) {
		this.hashedUrl = hashedUrl;
	}
}