package de.uni_koeln.arachne.response;

import de.uni_koeln.arachne.service.SQLResponseObject;

/**
 * This class is the standard container for images. It is derived from <code>SQLResponseObject</code> so that a
 * specialized <code>RowMapper</code> can be used for retrieval from the database.
 */
public class Image extends SQLResponseObject {
	/**
	 * The primary key of the image.
	 */
	private Long imageId = null;
	
	/**
	 * The subtitle of the image.
	 */
	private String subtitle = null;

	public Long getImageId() {
		return imageId;
	}

	public void setImageId(final Long imageId) {
		this.imageId = imageId;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(final String subtitle) {
		this.subtitle = subtitle;
	}
	
	@Override
	public String toString() {
		return "[" + String.valueOf(imageId) + ", " + subtitle + "]";
	}
}