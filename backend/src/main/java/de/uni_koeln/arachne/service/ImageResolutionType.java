package de.uni_koeln.arachne.service;

/**
 * Enum that represents the resolution type of an image
 * @author Sven Ole Clemens
 *
 */
public enum ImageResolutionType {

	THUMBNAIL(80, 80),
	PREVIEW(300, 300),
	HIGH(800, 800);
	
	private final int height;
	private final int width;
	
	ImageResolutionType(int h, int w) {
		this.height = h;
		this.width = w;
	}
	
	public int height() {
		return height;
	}
	
	public int width() {
		return width;
	}
}
