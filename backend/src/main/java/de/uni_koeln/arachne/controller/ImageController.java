package de.uni_koeln.arachne.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import de.uni_koeln.arachne.dao.ImageRightsDao;
import de.uni_koeln.arachne.mapping.ImageRightsGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
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
	
	// width for the different image types
	private static final int THUMBNAIL = 150;
	private static final int PREVIEW = 400;
	private static final int HIGH = 0;
	
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
	
	private final transient String imageServerUrl;
	private final transient String imagePath;
	private final transient int imageServerReadTimeout;
	
	@Autowired
	public ImageController(final @Value("#{config.imageServerUrl}") String imageServerUrl,
			final @Value("#{config.imagePath}") String imagePath,
			final @Value("#{config.imageServerReadTimeout}") int imageServerReadTimeout) {
			
		this.imageServerUrl = imageServerUrl;
		this.imagePath = imagePath;
		this.imageServerReadTimeout = imageServerReadTimeout;
		LOGGER.info("ImageServerUrl: " + imageServerUrl);
	}
	
	/**
	 * This method handles requests from the <code>mooviewer</code> to the image server.
	 * @param imageServerUrl
	 * @param imageServerReadTimeout
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/image/viewer", method = RequestMethod.GET)
	public ResponseEntity<Object> getFromImageServer(final HttpServletRequest request, final HttpServletResponse response) {
		
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
					responseHeaders.setContentType(MediaType.IMAGE_JPEG);
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
						
		response.setStatus(404);
		return null;
	}
	
	/**
	 * Handles the request for /image/{id} (id is the entityId for an image)
	 * @param entityId
	 * @param response
	 * @return The requested image
	 */
	@RequestMapping(value = "/image/{entityId}", method = RequestMethod.GET)
	public ResponseEntity<Object> getImage(	@PathVariable("entityId") final long entityId, final HttpServletResponse response) {
		return getImageFromServer(-1, HIGH, response);
	}
	
	/**
	 * Handles the request for /image/thumbnail/{id} (id is the entityId for an image)
	 * @param entityId
	 * @param response
	 * @return The requested image
	 */
	@RequestMapping(value = "/image/thumbnail/{entityId}", method = RequestMethod.GET)
	public ResponseEntity<Object> getThumbnail(	@PathVariable("entityId") final long entityId, final HttpServletResponse response) {
		return getImageFromServer(-1, THUMBNAIL, response);
	}
	
	/**
	 * Handles the request for /image/preview/{id} (id is the entityId for an image)
	 * @param entityId
	 * @param response
	 * @return The requested image
	 */
	@RequestMapping(value = "/image/preview/{entityId}", method = RequestMethod.GET)
	public ResponseEntity<Object> getPreview(@PathVariable("entityId") final long entityId, final HttpServletResponse response) {
		return getImageFromServer(-1, PREVIEW, response);
	}
	
	private ResponseEntity<Object> getImageFromServer(final long entityId, final int requestedResolution, final HttpServletResponse response) {
		
		HttpURLConnection connection = null;
		// TODO replace when the correct images are accessible by the image server
		String imageName = "ptif_test.tif";
		if (entityId>0) {
			final EntityId arachneId = arachneEntityIdentificationService.getId(entityId);
			
			if(!arachneId.getTableName().equals("marbilder")) {
				LOGGER.error("EntityId {} does not refer to an image.", entityId);
				response.setStatus(404);
				return null;
			}
			
			final Dataset imageEntity = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId
					, userRightsService.getCurrentUser());
			// TODO get correct image name not the old one
			imageName = imageEntity.getField("marbilder.Pfad");
			LOGGER.debug("Image: " + entityId + ": " + imageName);
			
			// TODO implement watermarking
			// Check image rights
			/*final ImageRightsGroup imageRightsGroup = imageRightsDao.findByName(imageEntity.getField("marbilder.BildrechteGruppe"));
			final UserAdministration currentUser = userRightsService.getCurrentUser();
			final String watermarkFilename = imageRightsGroupService.getWatermarkFilename(imageEntity, currentUser, imageRightsGroup);
			if(!imageRightsGroupService.checkResolutionRight(imageEntity, currentUser, resolution, imageRightsGroup)) {
				resolution = imageRightsGroupService.getMaxResolution(imageEntity, currentUser, imageRightsGroup);
				
				// Forbidden
				if (resolution == null) {
					response.setStatus(403);
					return null;
				}
			}*/
		}
		
		try {
			final URL serverAdress = new URL(imageServerUrl + "?FIF=" + imagePath + imageName + "&SDS=0,90&CNT=1.0&WID="
					+ requestedResolution + "&QLT=99&CVT=jpeg");
			connection = (HttpURLConnection)serverAdress.openConnection();			
			connection.setRequestMethod("GET");
			connection.setReadTimeout(imageServerReadTimeout);
			connection.connect();
			
			if (connection.getResponseCode() == 200) {
				final HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentType(MediaType.IMAGE_JPEG);
				return new ResponseEntity<Object>(ImageIO.read(connection.getInputStream()), responseHeaders, HttpStatus.OK);
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
						
		return null;
	}	
}