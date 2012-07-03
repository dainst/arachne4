package de.uni_koeln.arachne.service;

/**
 * Enum that represents the resolution type of an image
 * @author Sven Ole Clemens
 *
 */
public enum ImageResolutionType {

	THUMBNAIL(150),
	PREVIEW(400),
	HIGH(0);
	
	private final int width;
	
	ImageResolutionType(final int width) {
		this.width = width;
	}
	
	public int getWidth() {
		return width;
	}
}
