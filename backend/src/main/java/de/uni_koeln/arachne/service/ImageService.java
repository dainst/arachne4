package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.mapping.ImageRowMapper;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.ImageUtils;

/**
 * This service class provides the means to retrieve images from the database.
 */
@Service("ArachneImageService")
public class ImageService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);
	
	@Autowired
	private transient GenericSQLService genericSQLService; 
	
	private transient final List<String> excludeList;
	
	@Autowired
	public ImageService(final @Value("#{config.imageExcludeList}") String imageExcludeListCS) {
		excludeList = new ArrayList<String>(Arrays.asList(imageExcludeListCS.split(",")));
	}
	
	/**
	 * This method retrieves the image ids for a given dataset from the database and adds them to the datasets list
	 * of images. It also finds the preview thumbnail from this list and adds it to the dataset.    
	 * @param dataset The dataset to add images to.
	 */
	public void addImages(final Dataset dataset) {
		final EntityId arachneId = dataset.getArachneId();
		if (excludeList.contains(arachneId.getTableName())) {
			LOGGER.debug("excluding " + arachneId.getTableName());
			return;
		} else {
			final ArrayList<String> fieldList = new ArrayList<String>(2);
			fieldList.add("DateinameMarbilder");
			@SuppressWarnings("unchecked")
			final List<Image> imageList = (List<Image>) genericSQLService.getStringFieldsEntityIdJoinedWithCustomRowmapper("marbilder"
					, arachneId.getTableName(), arachneId.getInternalKey(), fieldList, new ImageRowMapper());
			dataset.setImages(imageList);
			// get thumbnail from imageList
			if (imageList != null && !imageList.isEmpty()) {
				dataset.setThumbnailId(ImageUtils.findThumbnailId(imageList));
			}
		}
	}

}
