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
import de.uni_koeln.arachne.util.ArachneId;

/**
 * @author Sven Ole Clemens
 * @author Sebastian Cuy
 *
 */
@Service
public class ImageRightsGroupService {
	
	private static Logger logger = LoggerFactory.getLogger(ImageRightsGroupService.class);
	
	@Autowired
	SingleEntityDataService arachneSingleEntityDataService;
	
	@Autowired
	ImageRightsDao imageRightsDao;

	public ImageRightsGroupService() {
	}

	/**
	 * Method to check, if an user has the correct rights to
	 * see a resolution
	 * @param arachneId
	 * @param currentUser
	 * @param res 
	 * @return
	 */
	public boolean checkResolutionRight(ArachneId arachneId,
			UserAdministration currentUser, ImageResolutionType res) {
		
		ImageResolutionType maxResolution = getMaxResolution(arachneId, currentUser);
		
		if (maxResolution != null && maxResolution.ordinal() >= res.ordinal()) {
			return true;
		} else {
			return false;
		}
		
	}

	/**
	 * Method to get the maximum resolution, based on the
	 * user rights
	 * @return
	 */
	public ImageResolutionType getMaxResolution(ArachneId arachneId,
			UserAdministration currentUser) {
		
		Dataset imageEntity = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId);
		logger.debug("Retrieved Entity for image: {}", imageEntity);
		
		// if user doesn't have group he is not allowed to view the image in any resolution
		if(imageEntity.getField("marbilder.BildrechteGruppe") == null) {
			logger.debug("user doesn't have dataset group of image");
			return null;
		}
		
		ImageRightsGroup imageRightsGroup = imageRightsDao.findByName(imageEntity.getField("marbilder.BildrechteGruppe"));
		
		if (imageRightsGroup == null) {
			throw new IllegalStateException("image with entity id " + arachneId.getArachneEntityID() 
					+ " has illegal rightsGroup " + imageEntity.getField("marbilder.BildrechteGruppe"));
		}
		
		// if override_for_group is set and the user has that exact group, the user is allowed to view the image in high resolution
		if(!imageRightsGroup.getOverrideForGroup().isEmpty()) {
			if(currentUser.hasGroup(imageRightsGroup.getOverrideForGroup())) {
				logger.debug("user has override group, returning HIGH");
				return ImageResolutionType.HIGH;
			}
		}
		
		// get maximum resolution for anonymous users
		if(currentUser.getGroupID() == 0) {
			logger.debug("user is anonymous, returning " + imageRightsGroup.getResolutionAnonymous());
			return imageRightsGroup.getResolutionAnonymous();
		// get maximum resolution for registered user
		} else if (currentUser.getGroupID() > 0 && currentUser.getGroupID() < 550) {
			logger.debug("user is registered, returning " + imageRightsGroup.getResolutionRegistered());
			return imageRightsGroup.getResolutionRegistered();
		// users with gid >= 550 can view any resolution
		} else {
			logger.debug("user has gid >= 500, returning HIGH");
			return ImageResolutionType.HIGH;
		}
		
	}
	
}
