package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import de.uni_koeln.arachne.util.image.ImageComparator;
import de.uni_koeln.arachne.util.image.ImageUtils;
import de.uni_koeln.arachne.util.sql.SQLToolbox;

/**
 * This service class provides the means to retrieve image meta data from the database.
 * 
 * @author Reimar Grabowski
 */
@Service("ImageService")
public class ImageService {
	
	//private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);
	
	@Autowired
	private transient DataIntegrityLogService dataIntegrityLogService;
	
	@Autowired
	private transient GenericSQLDao genericSQLDao; 
	
	private transient final List<String> excludeList;
	
	@Autowired
	public ImageService(final @Value("#{'${imageExcludeList}'.split(',')}") List<String> imageExcludeList) {
		excludeList = imageExcludeList;
	}
	
	/**
	 * This method retrieves the image ids for a given dataset from the database and adds them to the datasets list
	 * of images. It also finds the preview thumbnail from this list and adds it to the dataset.    
	 * @param dataset The dataset to add images to.
	 */
	public void addImages(final Dataset dataset) {
		final EntityId arachneId = dataset.getArachneId();
		if (excludeList.contains(arachneId.getTableName())) {
			return;
		} else {
			if ("marbilder".equals(arachneId.getTableName())) {
				final Image image = new Image();
				image.setImageId(arachneId.getArachneEntityID());
				String fileName = dataset.getField("marbilder.DateinameMarbilder");
				image.setImageSubtitle(fileName.substring(0, fileName.lastIndexOf('.')));
				final List<Image> imageList = new ArrayList<Image>();
				imageList.add(image);
				dataset.setImages(imageList);
				dataset.setThumbnailId(arachneId.getArachneEntityID());
			} else {
				final List<Image> imageList = (List<Image>) genericSQLDao.getImageList(arachneId.getTableName()
						, arachneId.getInternalKey());
				// sort image List
				if (imageList != null && imageList.size() > 1) {
					Collections.sort(imageList, new ImageComparator());
				}

				dataset.setImages(imageList);
				// get thumbnail from imageList
				if (imageList != null && !imageList.isEmpty()) {
					final Long thumbnailId = ImageUtils.findThumbnailId(imageList);
					dataset.setThumbnailId(thumbnailId);
					if (thumbnailId == null) {
						dataIntegrityLogService.logWarning(arachneId.getInternalKey()
								, SQLToolbox.generatePrimaryKeyName(arachneId.getTableName()), "Could not determine "
								+ "thumbnailId.");
					}
				}
			}
		}
	}

	/**
	 * Method to get a sublist of connected images. It will not return images for entities which themselves are images 
	 * (in contrast to 'addImages') as the only image connected to such entity is itself. 
	 * @param arachneId The entity ID object of an entity.
	 * @param offset An offset into the image list. 
	 * @param limit The maximum number of images in the sublist (0 for no limit).
	 * @return The image sublist and a corresponding HTTP status.
	 */
	public TypeWithHTTPStatus<List<Image>> getImagesSubList(final EntityId arachneId, final int offset, final int limit) {
		if (!excludeList.contains(arachneId.getTableName()) && (!"marbilder".equals(arachneId.getTableName()))) {
			final List<Image> imageList = (List<Image>) genericSQLDao.getImageList(arachneId.getTableName()
					, arachneId.getInternalKey());
			// sort image List
			if (imageList != null && imageList.size() > 1) {
				Collections.sort(imageList, new ImageComparator());
			}
			int upperBound = limit + offset;
			upperBound = (upperBound > imageList.size() || limit == 0) ? imageList.size() : upperBound; 
			try {
				return new TypeWithHTTPStatus<List<Image>>(imageList.subList(offset, upperBound));
			} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
				return new TypeWithHTTPStatus<>(HttpStatus.BAD_REQUEST);
			}
		}
		return new TypeWithHTTPStatus<>(HttpStatus.NOT_FOUND);
	}

}
