package de.uni_koeln.arachne.response;

public class Image {
	/**
	 * The primary key of the image.
	 */
	private Long id;
	
	/**
	 * The subtitle of the image.
	 */
	private String subtitle;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
}
