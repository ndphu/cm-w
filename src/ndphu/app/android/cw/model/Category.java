package ndphu.app.android.cw.model;

public enum Category {
	NEW("New"), ACTION("Action"), ADULT("Adult"), ADVENTURE("Nature"), COMEDY("Comedy"), DEMONS("Demons"), DRAMA(
			"Drama"), FANTASY("Fantasy"), HISTORICAL("Historical"), HORROR("Horror"), LIVE_ACTION("Live Action"), MAGIC(
			"Magic"), MARTIAL_ARTS("Martial Arts"), MATURE("Mature"), MUSIC("Music"), MYSTERY("Mystery"), ONESHOT(
			"One Shot"), ROMANCE("Romance"), SCHOOL_LIFE("School Life"), SPORTS("Sports"), SUPER_POWER("Super Powser"), SUPERNATURAL(
			"Super Natural"), TRAGEDY("Tragedy"), VAMPIRE("Vampire"), WEBTOONS("Webtoons"), COMIC("Comic"), SCAN("Scan");

	private String displayName;

	Category(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}

}
