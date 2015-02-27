package ndphu.app.android.cw.model;

public enum Category {
	NEW("New"),
	ACTION("Action"),
	ADULT("Adult"),
	DOUJINSHI("Doujinshi"),
	DEMONS("Demons"),
	DRAMA("Drama"),
	ECCHI("Ecchi"),
	GENDER_BENDER("Gender-Bender"),
	HAREM("Harem"),
	HORROR("Horror"),
	JOSEI("Josei"),
	MATURE("Mature"),
	MECHA("Mecha"),
	MYSTERY("Mystery"),
	ROMANCE("Romance"),
	SCHOOL_LIFE("School-Life"),
	SCI_FI("Sci-fi"),
	SEINEN("Seinen"),
	SHOUJO("Shoujo"),
	SHOUJO_AI("Shoujo Ai"),	 
	SHOUNEN("Shounen"),
	SHOUNEN_AI("Shounen Ai"),
	YAOI("Yaoi"),
	YURI("Yuri"),
	OVER_16("16+"),
	OVER_18("18+");
	
	private String displayName;

	Category(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

}
