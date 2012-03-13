/**
 * 
 */
package de.uni_koeln.arachne.controller;

import java.awt.image.BufferedImage;
import java.util.Set;

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

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.ImageResolutionType;
import de.uni_koeln.arachne.service.ImageStreamService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * Handles http requests for images, currently only get
 * @author Sven Ole Clemens
 * @author Sebastian Cuy
 */
@Controller
public class ImageController {

	private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
	
	@Autowired
	private UserRightsService userRightsService;
	
	@Autowired
	private EntityIdentificationService arachneEntityIdentificationService;
	
	@Autowired
	private ImageStreamService imageStreamService;
	
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
		
		return getImageStream(id, ImageResolutionType.HIGH, request, response);
		
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
				
		return getImageStream(id, ImageResolutionType.THUMBNAIL, request, response);
		
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
		
		return getImageStream(id, ImageResolutionType.PREVIEW, request, response);
		
	}
	
	private BufferedImage getImageStream(String id, ImageResolutionType res, HttpServletRequest request, HttpServletResponse response) {
		
		ArachneId arachneId = arachneEntityIdentificationService.getId(Long.valueOf(id));
		
		if(!arachneId.getTableName().equals("marbilder")) {
			logger.error("Error: entityId {} does not refer to an image.");
			response.setStatus(404);
			return null;
		}
		
		UserAdministration currentUser = userRightsService.getCurrentUser();
		
		Set<DatasetGroup> datasetGroups = currentUser.getDatasetGroups();
		
		try {
			response.setStatus(200);
			BufferedImage bufferedImage = imageStreamService.getArachneImage(res, arachneId.getInternalKey());
			return bufferedImage;
		} catch (Exception e) {
			logger.error("Error while retrieving thumbnail with entity id from image service" + arachneId.getArachneEntityID(),e);			
			response.setStatus(404);
			return null;
		}
		
	}
	
}
