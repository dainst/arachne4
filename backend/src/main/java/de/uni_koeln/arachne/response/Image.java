package de.uni_koeln.arachne.response;

import de.uni_koeln.arachne.service.SQLResponseObject;

public class Image extends SQLResponseObject {
	/**
	 * The primary key of the image.
	 */
	private Long id = null;
	
	/**
	 * The subtitle of the image.
	 */
	private String subtitle = null;

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
	
	@Override
	public String toString() {
		return "[" + String.valueOf(id) + ", " + subtitle + "]";
	}
}
