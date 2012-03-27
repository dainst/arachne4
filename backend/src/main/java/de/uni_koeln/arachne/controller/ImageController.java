/**
 * 
 */
package de.uni_koeln.arachne.controller;

import java.awt.image.BufferedImage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.dao.ImageRightsDao;
import de.uni_koeln.arachne.mapping.ImageRightsGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.ImageResolutionType;
import de.uni_koeln.arachne.service.ImageRightsGroupService;
import de.uni_koeln.arachne.service.ImageStreamService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * Handles http requests for images, currently only get
 * @author Sven Ole Clemens
 * @author Sebastian Cuy
 */
@Controller
public class ImageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);
	
	@Autowired
	private UserRightsService userRightsService;
	
	@Autowired
	private EntityIdentificationService arachneEntityIdentificationService;
	
	@Autowired
	private ImageStreamService imageStreamService;

	@Autowired
	private ImageRightsDao imageRightsDao;

	@Autowired
	private ImageRightsGroupService imageRightsGroupService;
	
	@Autowired
	private SingleEntityDataService arachneSingleEntityDataService;
	
	/**
	 * Handles the request for /image/{id} (id is the entityId for an image)
	 * @param id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/image/{id}", method = RequestMethod.GET)
	public @ResponseBody BufferedImage getImage(
			@PathVariable("id") String id,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		return getImageStream(id, ImageResolutionType.HIGH, response);
		
	}
	
	/**
	 * Handles the request for /image/thumbnail/{id} (id is the entityId for an image)
	 * @param id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/image/thumbnail/{id}", method = RequestMethod.GET)
	public @ResponseBody BufferedImage getThumbnail(
			@PathVariable("id") String id,
			HttpServletRequest request,
			HttpServletResponse response) {
				
		return getImageStream(id, ImageResolutionType.THUMBNAIL, response);
		
	}
	
	/**
	 * Handles the request for /image/preview/{id} (id is the entityId for an image)
	 * @param id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/image/preview/{id}", method = RequestMethod.GET)
	public @ResponseBody BufferedImage getPreview(
			@PathVariable("id") String id,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		return getImageStream(id, ImageResolutionType.PREVIEW, response);
		
	}
	
	private BufferedImage getImageStream(String id, ImageResolutionType requestedResolution
			, HttpServletResponse response) {
		
		ImageResolutionType resolution = requestedResolution;
		ArachneId arachneId = arachneEntityIdentificationService.getId(Long.valueOf(id));
		
		if(!arachneId.getTableName().equals("marbilder")) {
			LOGGER.error("Error: entityId {} does not refer to an image.");
			response.setStatus(404);
			return null;
		}
		
		Dataset imageEntity = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId);
		LOGGER.debug("Retrieved Entity for image: {}", imageEntity);
		
		// Check image rights
		ImageRightsGroup imageRightsGroup = imageRightsDao.findByName(imageEntity.getField("marbilder.BildrechteGruppe"));
		UserAdministration currentUser = userRightsService.getCurrentUser();
		String watermarkFilename = imageRightsGroupService.getWatermarkFilename(imageEntity, currentUser, imageRightsGroup);
		if(!imageRightsGroupService.checkResolutionRight(imageEntity, currentUser, resolution, imageRightsGroup)) {
			resolution = imageRightsGroupService.getMaxResolution(imageEntity, currentUser, imageRightsGroup);
			
			// Forbidden
			if (resolution == null) {
				response.setStatus(403);
				return null;
			}
		}
		
		try {
			response.setStatus(200);
			BufferedImage bufferedImage = imageStreamService.getArachneImage(resolution, imageEntity, watermarkFilename);
			return bufferedImage;
		} catch (Exception e) {
			LOGGER.error("Error while retrieving thumbnail with entity id from image service" + arachneId.getArachneEntityID(),e);			
			response.setStatus(404);
			return null;
		}
		
	}
	
}
