/**
 * 
 */
package de.uni_koeln.arachne.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.ImageRightsDao;
import de.uni_koeln.arachne.mapping.ImageRightsGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.response.Dataset;

/**
 * @author Sven Ole Clemens
 * @author Sebastian Cuy
 *
 */
@Service
public class ImageRightsGroupService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageRightsGroupService.class);
	
	@Autowired
	SingleEntityDataService arachneSingleEntityDataService;
	
	@Autowired
	ImageRightsDao imageRightsDao;
	
	@Autowired
	IUserRightsService userRightsService;

	private final transient int resolution_HIGH;
	private final transient int resolution_THUMBNAIL;
	private final transient int resolution_PREVIEW;
	
	/**
	 * Constructor to initialize the image resolution parameters set in application.properties.
	 * @param resolutionHIGH Width for high resolution images.
	 * @param resolutionTHUMBNAIL Width for thumbnail images.
	 * @param resolutionPREVIEW Width for preview resolution images.
	 */
	@Autowired
	public ImageRightsGroupService(final @Value("#{config.imageResolutionHIGH}") int resolutionHIGH,
			final @Value("#{config.imageResolutionTHUMBNAIL}") int resolutionTHUMBNAIL,
			final @Value("#{config.imageResolutionPREVIEW}") int resolutionPREVIEW) {
		
		this.resolution_HIGH = resolutionHIGH;
		this.resolution_THUMBNAIL = resolutionTHUMBNAIL;
		this.resolution_PREVIEW = resolutionPREVIEW;
	}

	/**
	 * Method to check, if an user has the correct rights to
	 * see a resolution
	 * @param arachneId
	 * @param currentUser
	 * @param res 
	 * @param imageRightsGroup 
	 * @return
	 */
	public boolean checkResolutionRight(final Dataset imageEntity, final int resolution
			, final ImageRightsGroup imageRightsGroup) {
		
		final int maxResolution = getMaxResolution(imageEntity, imageRightsGroup);
		
		LOGGER.debug("checkResolution: " + resolution + " - " + maxResolution);
		
		if (maxResolution == resolution_HIGH) {
			return true;
		} else {
			if (resolution == resolution_HIGH) {
				return maxResolution == resolution;
			} else {
				return maxResolution >= resolution;
			}
		}		
	}

	/**
	 * Method to get the maximum resolution, based on the
	 * user rights
	 * @return
	 */
	public int getMaxResolution(final Dataset imageEntity, final ImageRightsGroup imageRightsGroup) {
		
		final UserAdministration currentUser = userRightsService.getCurrentUser();
		
		// if user doesn't have group he is not allowed to view the image in any resolution
		if(imageEntity.getField("marbilder.BildrechteGruppe") == null) {
			LOGGER.debug("user doesn't have dataset group of image");
			return -1;
		}
		
		if (imageRightsGroup == null) {
			throw new IllegalStateException("image with entity id " + imageEntity.getArachneId() 
					+ " has illegal rightsGroup " + imageEntity.getField("marbilder.BildrechteGruppe"));
		}
		
		// if override_for_group is set and the user has that exact group, the user is allowed to view the image in high resolution
		if (!imageRightsGroup.getOverrideForGroup().isEmpty() && currentUser.hasGroup(imageRightsGroup.getOverrideForGroup())) {
			LOGGER.debug("user has override group, returning HIGH");
			return resolution_HIGH;
		}
		
		// get maximum resolution for anonymous users
		if(currentUser.getGroupID() == 0) {
			LOGGER.debug("user is anonymous, returning " + imageRightsGroup.getResolutionAnonymous());
			return resolutionNameToInt(imageRightsGroup.getResolutionAnonymous()); 
		// get maximum resolution for registered user
		} else if (currentUser.getGroupID() > 0 && currentUser.getGroupID() < 550) {
			LOGGER.debug("user is registered, returning " + imageRightsGroup.getResolutionRegistered());
			return resolutionNameToInt(imageRightsGroup.getResolutionRegistered());
		// users with gid >= 550 can view any resolution
		} else {
			LOGGER.debug("user has gid >= 500, returning HIGH");
			return resolution_HIGH;
		}
	}

	/**
	 * Convert database text fields to the corresponding ints as defined in application.properties.
	 * @param resolutionName
	 * @return
	 */
	private int resolutionNameToInt(final String resolutionName) {
		// since switch is not supported for strings in Java 6 use ugly ifs
		if ("THUMBNAIL".equals(resolutionName)) { return resolution_THUMBNAIL; }
		if ("PREVIEW".equals(resolutionName)) { return resolution_PREVIEW; }
		if ("HIGH".equals(resolutionName)) { return resolution_HIGH; }
		return -1;
	}

	/**
	 * Get the filename of the watermark based on the user and an image rights group.
	 * @param imageEntity
	 * @param currentUser
	 * @param imageRightsGroup
	 * @return
	 */
	public String getWatermarkFilename(final Dataset imageEntity, final ImageRightsGroup imageRightsGroup) {
		
		final UserAdministration currentUser = userRightsService.getCurrentUser(); 
		
		// if override_for_group is set and the user has that exact group, the user is allowed to view the image without watermark
		if(!imageRightsGroup.getOverrideForGroup().isEmpty() &&  currentUser.hasGroup(imageRightsGroup.getOverrideForGroup())) {
				LOGGER.debug("user has override group, returning no watermark");
				return "";
		}
		
		if (currentUser.getGroupID() == 0) {
			return imageRightsGroup.getWatermarkAnonymous();
		} else if (currentUser.getGroupID() > 0 && currentUser.getGroupID() < 550) {
			return imageRightsGroup.getWatermarkRegistered();
		}
		return "";
		
	}
	
}
