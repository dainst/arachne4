package de.uni_koeln.arachne.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.service.ImageService;

/**
 * This class implements utility functions for working with images
 */
@SuppressWarnings("PMD")
public class ImageUtils {
	/**
	 * This method finds the thumbnailId by finding the image whose filename does not contain a comma or the filename with the
	 *  lowest number after the comma.
	 * @param imageList
	 * @param thumbnail
	 * @return The image id of the thumbnail.
	 */
	public static Long findThumbnailId(final List<? extends Image> imageList) {
		Image thumbnail = imageList.get(0);
		if (imageList.size()>1 && thumbnail.getSubtitle().contains(",")) {
			Integer lowestNumber = extractNumberFromImageFilename(thumbnail.getSubtitle());
			for (Image potentialThumbnail: imageList) {
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
		Integer result = Integer.MAX_VALUE;
		try {
			result = Integer.parseInt(imageFilename.split(",")[1].split("\\.")[0]); 
		} catch (NumberFormatException e) {
			// ignore images where the part after the comma is not a number
			final Logger logger = LoggerFactory.getLogger(ImageService.class);
			logger.debug(e.getMessage());
		}
		return result;
	}
}
