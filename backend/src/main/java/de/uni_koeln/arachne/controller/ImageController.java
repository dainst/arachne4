/**
 * 
 */
package de.uni_koeln.arachne.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.dao.ImageRightsDao;
import de.uni_koeln.arachne.mapping.ImageRightsGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.ImageResolutionType;
import de.uni_koeln.arachne.service.ImageRightsGroupService;
import de.uni_koeln.arachne.service.ImageStreamService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.EntityId;

/**
 * Handles http requests for images, currently only get
 * @author Sven Ole Clemens
 * @author Sebastian Cuy
 */
@Controller
public class ImageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);
	
	@Autowired
	private UserRightsService userRightsService; // NOPMD
	
	@Autowired
	private EntityIdentificationService arachneEntityIdentificationService; // NOPMD
	
	@Autowired
	private ImageStreamService imageStreamService; // NOPMD

	@Autowired
	private ImageRightsDao imageRightsDao; // NOPMD

	@Autowired
	private ImageRightsGroupService imageRightsGroupService; // NOPMD
	
	@Autowired
	private SingleEntityDataService arachneSingleEntityDataService; // NOPMD
	
	@RequestMapping(value = "/image/viewer", method = RequestMethod.GET)
	public ResponseEntity<Object> getFromImageServer(
			final @Value("#{config.imageServerUrl}") String imageServerUrl,
			final @Value("#{config.imageServerReadTimeout}") Integer imageServerReadTimeout,
			final HttpServletRequest request,
			final HttpServletResponse response) {
		
		LOGGER.debug("Viewer called.");
		
		LOGGER.debug("Request: " + request.getQueryString());
		
		HttpURLConnection connection = null;
						
		try {
			final URL serverAdress = new URL(imageServerUrl + "?" + request.getQueryString());
			connection = (HttpURLConnection)serverAdress.openConnection();			
			connection.setRequestMethod("GET");
			connection.setReadTimeout(imageServerReadTimeout);
			connection.connect();
			
			if (connection.getResponseCode() == 200) {
				final HttpHeaders responseHeaders = new HttpHeaders();

				// check if viewer calls for metadata - if the request query string contains "obj=IIP" it does
				if (request.getQueryString().contains("obj=IIP")) {
					final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

					final StringBuilder stringBuilder = new StringBuilder();
					String line = null;

					while ((line = bufferedReader.readLine()) != null) {
						stringBuilder.append(line  + '\n');
					}

					responseHeaders.setContentType(MediaType.TEXT_PLAIN);
					return new ResponseEntity<Object>(stringBuilder.toString(), responseHeaders, HttpStatus.OK);
				} else {
					responseHeaders.setContentType(MediaType.IMAGE_PNG);
					return new ResponseEntity<Object>(ImageIO.read(connection.getInputStream()), responseHeaders, HttpStatus.OK);
				}
			}
		
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage());
		} catch (ProtocolException e) {
			LOGGER.error(e.getMessage());
		} catch (SocketTimeoutException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		} finally {
			connection.disconnect();
			connection = null;
		}
						
		response.setStatus(403);
		return null;
	}
	
	/**
	 * Handles the request for /image/{id} (id is the entityId for an image)
	 * @param entityId
	 * @param request
	 * @param response
	 * @return The requested image (?)
	 */
	@RequestMapping(value = "/image/{entityId}", method = RequestMethod.GET)
	public @ResponseBody BufferedImage getImage(
			@PathVariable("entityId") final String entityId,
			final HttpServletRequest request,
			final HttpServletResponse response) {
		
		return getImageStream(entityId, ImageResolutionType.HIGH, response);
	}
	
	/**
	 * Handles the request for /image/thumbnail/{id} (id is the entityId for an image)
	 * @param entityId
	 * @param request
	 * @param response
	 * @return The requested image (?)
	 */
	@RequestMapping(value = "/image/thumbnail/{entityId}", method = RequestMethod.GET)
	public @ResponseBody BufferedImage getThumbnail(
			@PathVariable("entityId") final String entityId,
			final HttpServletRequest request,
			final HttpServletResponse response) {
				
		return getImageStream(entityId, ImageResolutionType.THUMBNAIL, response);
	}
	
	/**
	 * Handles the request for /image/preview/{id} (id is the entityId for an image)
	 * @param entityId
	 * @param request
	 * @param response
	 * @return The requested image (?)
	 */
	@RequestMapping(value = "/image/preview/{entityId}", method = RequestMethod.GET)
	public @ResponseBody BufferedImage getPreview(
			@PathVariable("entityId") final String entityId,
			final HttpServletRequest request,
			final HttpServletResponse response) {
		
		return getImageStream(entityId, ImageResolutionType.PREVIEW, response);
	}
	
	private BufferedImage getImageStream(final String entityId, final ImageResolutionType requestedResolution
			, final HttpServletResponse response) {
		
		ImageResolutionType resolution = requestedResolution;
		final EntityId arachneId = arachneEntityIdentificationService.getId(Long.valueOf(entityId));
		
		if(!arachneId.getTableName().equals("marbilder")) {
			LOGGER.error("Error: entityId {} does not refer to an image.");
			response.setStatus(404);
			return null;
		}
		
		final Dataset imageEntity = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId
				, userRightsService.getCurrentUser());
		LOGGER.debug("Retrieved Entity for image: {}", imageEntity);
		
		// Check image rights
		final ImageRightsGroup imageRightsGroup = imageRightsDao.findByName(imageEntity.getField("marbilder.BildrechteGruppe"));
		final UserAdministration currentUser = userRightsService.getCurrentUser();
		final String watermarkFilename = imageRightsGroupService.getWatermarkFilename(imageEntity, currentUser, imageRightsGroup);
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
			return imageStreamService.getArachneImage(resolution, imageEntity, watermarkFilename);
		} catch (Exception e) {
			LOGGER.error("Error while retrieving thumbnail with entity id from image service" + arachneId.getArachneEntityID(),e);			
			response.setStatus(404);
			return null;
		}
		
	}
	
}
