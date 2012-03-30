/**
 * 
 */
package de.uni_koeln.arachne.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	public ImageRightsGroupService() {
		// needed for spring autowiring
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
	public boolean checkResolutionRight(final Dataset imageEntity,
			final UserAdministration currentUser, final ImageResolutionType res, final ImageRightsGroup imageRightsGroup) {
		
		final ImageResolutionType maxResolution = getMaxResolution(imageEntity, currentUser, imageRightsGroup);
		
		return maxResolution != null && maxResolution.ordinal() >= res.ordinal();		
	}

	/**
	 * Method to get the maximum resolution, based on the
	 * user rights
	 * @return
	 */
	public ImageResolutionType getMaxResolution(final Dataset imageEntity,
			final UserAdministration currentUser, final ImageRightsGroup imageRightsGroup) {
		
		// if user doesn't have group he is not allowed to view the image in any resolution
		if(imageEntity.getField("marbilder.BildrechteGruppe") == null) {
			LOGGER.debug("user doesn't have dataset group of image");
			return null;
		}
		
		if (imageRightsGroup == null) {
			throw new IllegalStateException("image with entity id " + imageEntity.getArachneId() 
					+ " has illegal rightsGroup " + imageEntity.getField("marbilder.BildrechteGruppe"));
		}
		
		// if override_for_group is set and the user has that exact group, the user is allowed to view the image in high resolution
		if(!imageRightsGroup.getOverrideForGroup().isEmpty()) {
			if(currentUser.hasGroup(imageRightsGroup.getOverrideForGroup())) {
				LOGGER.debug("user has override group, returning HIGH");
				return ImageResolutionType.HIGH;
			}
		}
		
		// get maximum resolution for anonymous users
		if(currentUser.getGroupID() == 0) {
			LOGGER.debug("user is anonymous, returning " + imageRightsGroup.getResolutionAnonymous());
			return imageRightsGroup.getResolutionAnonymous();
		// get maximum resolution for registered user
		} else if (currentUser.getGroupID() > 0 && currentUser.getGroupID() < 550) {
			LOGGER.debug("user is registered, returning " + imageRightsGroup.getResolutionRegistered());
			return imageRightsGroup.getResolutionRegistered();
		// users with gid >= 550 can view any resolution
		} else {
			LOGGER.debug("user has gid >= 500, returning HIGH");
			return ImageResolutionType.HIGH;
		}
		
	}

	/**
	 * Get the filename of the watermark based on the user and an image rights group.
	 * @param imageEntity
	 * @param currentUser
	 * @param imageRightsGroup
	 * @return
	 */
	public String getWatermarkFilename(final Dataset imageEntity,
			final UserAdministration currentUser, final ImageRightsGroup imageRightsGroup) {
		
		// if override_for_group is set and the user has that exact group, the user is allowed to view the image without watermark
		if(!imageRightsGroup.getOverrideForGroup().isEmpty()) {
			if(currentUser.hasGroup(imageRightsGroup.getOverrideForGroup())) {
				LOGGER.debug("user has override group, returning no watermark");
				return "";
			}
		}
		
		if (currentUser.getGroupID() == 0) {
			return imageRightsGroup.getWatermarkAnonymous();
		} else if (currentUser.getGroupID() > 0 && currentUser.getGroupID() < 550) {
			return imageRightsGroup.getWatermarkRegistered();
		}
		return "";
		
	}
	
}
