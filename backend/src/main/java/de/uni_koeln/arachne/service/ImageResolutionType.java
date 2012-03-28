package de.uni_koeln.arachne.service;

/**
 * Enum that represents the resolution type of an image
 * @author Sven Ole Clemens
 *
 */
public enum ImageResolutionType {

	THUMBNAIL(150, 150),
	PREVIEW(400, 400),
	HIGH(0, 0);
	
	private final int height;
	private final int width;
	
	ImageResolutionType(final int height, final int width) {
		this.height = height;
		this.width = width;
	}
	
	public int height() {
		return height;
	}
	
	public int width() {
		return width;
	}
}
