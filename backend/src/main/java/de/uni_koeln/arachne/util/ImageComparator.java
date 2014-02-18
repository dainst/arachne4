package de.uni_koeln.arachne.util;

import java.io.Serializable;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.Image;

/**
 * Implements a <code>Comparator</code> to sort images by their 'Bildnummer'.
 */
public class ImageComparator implements Comparator<Image>, Serializable {
	// Do not forget to update this version number if the comparator is changed
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageComparator.class);
	
	@Override
	public int compare(Image image1, Image image2) {
		String subTitle1 = image1.getSubtitle();
		String subTitle2 = image2.getSubtitle();

		if (subTitle1 == null) {
			LOGGER.warn("Data integrity warning. Missing subtitle on image '" + image1.getImageId() + "'.");
			return 0;
		}

		if (subTitle2 == null) {
			LOGGER.warn("Data integrity warning. Missing subtitle on image '" + image2.getImageId() + "'.");
			return 0;
		}

		// Use Interger.MIN_VALUE / 4 to not generate an overflow;
		int imageNumber1 = subTitle1.contains(",") ? ImageUtils.extractNumberFromImageFilename(image1.getSubtitle()) 
				: Integer.MIN_VALUE / 4;
		int imageNumber2 = subTitle2.contains(",") ? ImageUtils.extractNumberFromImageFilename(image2.getSubtitle()) 
				: Integer.MIN_VALUE / 4;

		LOGGER.debug("Compare: " + subTitle1 + "[" + imageNumber1 + "]" + " - " + subTitle2 + "[" + imageNumber2 + "]");
		LOGGER.debug("Comparion result : " + (imageNumber1 - imageNumber2));
		
		return imageNumber1 - imageNumber2;
	}
}