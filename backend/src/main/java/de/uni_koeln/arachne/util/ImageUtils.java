package de.uni_koeln.arachne.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.Image;

/**
 * This class implements utility functions for working with images
 */
public class ImageUtils { // NOPMD
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);
	
	/**
	 * This method finds the thumbnailId by finding the image whose filename does not contain a comma or the filename with the
	 *  lowest number after the comma.
	 * @param imageList
	 * @param thumbnail
	 * @return The image id of the thumbnail or <code>null</code> on error.
	 */
	public static Long findThumbnailId(final List<? extends Image> imageList) {
		Image thumbnail = imageList.get(0);
		final String subtitle = thumbnail.getSubtitle();
		if (subtitle == null) {
			LOGGER.warn("Data integrity warning. Missing subtitle on image '" + thumbnail.getImageId() + "'.");
			return null;
		}
		if (imageList.size()>1 && thumbnail != null && subtitle.contains(",")) {
			Integer lowestNumber = extractNumberFromImageFilename(subtitle);
			for (final Image potentialThumbnail: imageList) {
				if (potentialThumbnail.getSubtitle().contains(",")) {
					final Integer currentNumber = extractNumberFromImageFilename(potentialThumbnail.getSubtitle());
					if (currentNumber<lowestNumber) {
						thumbnail = potentialThumbnail;
						lowestNumber = currentNumber;
					}
				} else {
					thumbnail = potentialThumbnail;
					break;
				}
			}
		}
		return thumbnail.getImageId();
	}
	
	/**
	 * This method finds the number after the comma of a given image filename.
	 * @returns The extracted number as <code>Integer</code> or <code>Integer.MAX_VALUE</code> in case of a parsing error.
	 */
	public static Integer extractNumberFromImageFilename(final String imageFilename) {
		// Use Interger.MIN_VALUE / 4 to not generate an overflow in comparisons
		Integer result = Integer.MAX_VALUE / 4;
		try {
			result = Integer.parseInt(imageFilename.split(",")[1].split("\\.")[0]); 
		} catch (NumberFormatException e) { // NOPMD
			// ignore images where the part after the comma is not a number
		}
		return result;
	}
}
