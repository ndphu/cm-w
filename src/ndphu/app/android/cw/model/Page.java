package ndphu.app.android.cw.model;
public class Page {
		private String link;
		private int pageOrder;
		
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
	}