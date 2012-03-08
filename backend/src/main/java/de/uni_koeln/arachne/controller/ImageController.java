/**
 * 
 */
package de.uni_koeln.arachne.controller;

import java.io.IOException;
import java.net.MalformedURLException;

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

import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.ImageResolutionType;
import de.uni_koeln.arachne.service.ImageStreamService;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * Handles http requests for images, currently only get
 * @author Sven Ole Clemens
 *
 */
@Controller
public class ImageController {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	
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
	public @ResponseBody byte[] getImage(
			@PathVariable("id") String id,
			HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("Get image for id: " + id);
		
		ArachneId arachneId = arachneEntityIdentificationService.getId(Long.valueOf(id));
		
		if(!arachneId.getTableName().equals("marbilder")) {
			response.setStatus(404);
			return null;
		}
		
		try {
			
			response.setStatus(200);
			
			return imageStreamService.getArachneImage(ImageResolutionType.HIGH, arachneId.getInternalKey());
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		response.setStatus(404);
		return null;
	}
	
	/**
	 * Handles the request for /image/thumbnail/{id} (id is the entityId for an image)
	 * @param id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/image/thumbnail/{id}", method = RequestMethod.GET)
	public @ResponseBody byte[] getThumbnail(
			@PathVariable("id") String id,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		ArachneId arachneId = arachneEntityIdentificationService.getId(Long.valueOf(id));
		
		if(!arachneId.getTableName().equals("marbilder")) {
			response.setStatus(404);
			return null;
		}
		
		try {
			
			response.setStatus(200);
			
			return imageStreamService.getArachneImage(ImageResolutionType.THUMBNAIL, arachneId.getInternalKey());
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		response.setStatus(404);
		return null;
	}
	
	/**
	 * Handles the request for /image/preview/{id} (id is the entityId for an image)
	 * @param id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/image/preview/{id}", method = RequestMethod.GET)
	public @ResponseBody byte[] getPreview(
			@PathVariable("id") String id,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		ArachneId arachneId = arachneEntityIdentificationService.getId(Long.valueOf(id));
		
		if(!arachneId.getTableName().equals("marbilder")) {
			response.setStatus(404);
			return null;
		}
		
		try {
			
			response.setStatus(200);
			
			return imageStreamService.getArachneImage(ImageResolutionType.PREVIEW, arachneId.getInternalKey());
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		response.setStatus(404);
		return null;
	}
}
