package ndphu.app.android.cw.model;

import java.util.List;
import java.util.ArrayList;

public class Source {
	public static final int MANGA24H = 1;
	public static final int TRUYENHTRANHTUAN = 2;
	public static final String SOURCE_MANGA24H = "";
	public static List<Source> SOURCES = new ArrayList<Source>();
	static {
		synchronized (SOURCES) {
			SOURCES.add(new Source(MANGA24H, "Manga24h", ""));
			// SOURCES.add(new Source(TRUYENHTRANHTUAN, "TruyenTranhTuan", ""));
		}
	}
	private int id = 0;
	private String name = null;
	private String fullLink = null;

	private Source(int id, String name, String fullLink) {
		this.id = id;
		this.name = name;
		this.fullLink = fullLink;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getFullLink() {
		return fullLink;
	}

}
