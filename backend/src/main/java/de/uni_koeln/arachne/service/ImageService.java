package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.mapping.ImageRowMapper;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.util.EntityId;

/**
 * This service class provides the means to retrieve images from the database.
 */
@Service("ArachneImageService")
public class ImageService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);
	
	@Autowired
	private transient GenericSQLService genericSQLService; 
	
	/**
	 * This method retrieves the image ids for a given dataset from the database and adds them to the datasets list
	 * of images. It also finds the preview thumbnail from this list and adds it to the dataset.    
	 * @param dataset The dataset to add images to.
	 */
	public void addImages(final Dataset dataset) {
		final EntityId arachneId = dataset.getArachneId();
		final ArrayList<String> fieldList = new ArrayList<String>(2);
		fieldList.add("DateinameMarbilder");
		@SuppressWarnings("unchecked")
		final List<Image> imageList = (List<Image>) genericSQLService.getStringFieldsEntityIdJoinedWithCustomRowmapper("marbilder"
				, arachneId.getTableName(), arachneId.getInternalKey(), fieldList, new ImageRowMapper());
		dataset.setImages(imageList);
		// get thumbnail from imageList
		if (imageList != null && !imageList.isEmpty()) {
			dataset.setThumbnailId(findThumbnailId(imageList));
		}
	}

	/**
	 * This method finds the thumbnailId by finding the image whose filename does not contain a comma or the filename with the
	 *  lowest number after the comma.
	 * @param imageList
	 * @param thumbnail
	 * @return The image id of the thumbnail.
	 */
	private Long findThumbnailId(final List<Image> imageList) {
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
	
	// TODO check if the parsing must be extended to include numbers not only consisting of digits
	/**
	 * This method finds the number after the comma of a given image filename.
	 * @returns The extracted number as <code>Integer</code> or <code>Integer.MAX_VALUE</code> in case of a parsing error.
	 */
	private Integer extractNumberFromImageFilename(final String imageFilename) {
		Integer result = Integer.MAX_VALUE;
		try {
			result = Integer.parseInt(imageFilename.split(",")[1].split("\\.")[0]); 
		} catch (NumberFormatException e) {
			// ignore images where the part after the comma is not a number
			LOGGER.debug(e.getMessage());
		}
		return result;
	}
}
