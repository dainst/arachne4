package de.uni_koeln.arachne.controller;

import java.io.*;
import java.net.*;

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
import org.springframework.web.bind.annotation.RequestParam;

import de.uni_koeln.arachne.dao.ImageRightsDao;
import de.uni_koeln.arachne.mapping.ImageRightsGroup;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.ImageRightsGroupService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * Handles http requests for images, currently only get
 * @author Sven Ole Clemens
 * @author Sebastian Cuy
 */
@Controller
public class ImageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);
	
	@Autowired
	private transient EntityIdentificationService arachneEntityIdentificationService; 
	
	@Autowired
	private transient ImageRightsDao imageRightsDao; 

	@Autowired
	private transient ImageRightsGroupService imageRightsGroupService; 
	
	@Autowired
	private transient SingleEntityDataService arachneSingleEntityDataService; 
	
	private final transient String imageServerPath;
	private final transient String imageServerName;
	private final transient String imageServerExtension;
	private final transient String imagePath;
	private final transient int imageServerReadTimeout;
	
	private final transient int resolution_HIGH;
	private final transient int resolution_PREVIEW;
	private final transient int resolution_THUMBNAIL;
	private final transient int resolution_ICON;
	
	/**
	 * Constructor to initialize the image server parameters set in application.properties.
	 * @param imageServerUrl URL of the image server instance.
	 * @param imagePath Local image path on the server.
	 * @param imageServerReadTimeout Read timeout for HTTP requests accessing the image server.
	 * @param resolutionHIGH Width for high resolution images.
	 * @param resolutionTHUMBNAIL Width for thumbnail images.
	 * @param resolutionPREVIEW Width for preview resolution images.
	 */
	@Autowired
	public ImageController(final @Value("#{config.imageServerPath}") String imageServerPath,
			final @Value("#{config.imageServerName}") String imageServerName,
			final @Value("#{config.imageServerExtension}") String imageServerExtension,
			final @Value("#{config.imagePath}") String imagePath,
			final @Value("#{config.imageServerReadTimeout}") int imageServerReadTimeout,
			final @Value("#{config.imageResolutionHIGH}") int resolutionHIGH,
			final @Value("#{config.imageResolutionPREVIEW}") int resolutionPREVIEW,
			final @Value("#{config.imageResolutionTHUMBNAIL}") int resolutionTHUMBNAIL,
			final @Value("#{config.imageResolutionICON}") int resolutionICON) {
			
		this.imageServerPath = imageServerPath;
		this.imageServerName = imageServerName;
		this.imageServerExtension = imageServerExtension;
		this.imagePath = imagePath;
		this.imageServerReadTimeout = imageServerReadTimeout;
		LOGGER.info("ImageServerUrl: " + imageServerPath + imageServerName + imageServerExtension);
		this.resolution_HIGH = resolutionHIGH;
		this.resolution_PREVIEW = resolutionPREVIEW;
		this.resolution_THUMBNAIL = resolutionTHUMBNAIL;
		this.resolution_ICON = resolutionICON;
	}
	
	/**
	 * This method handles requests from the <code>mooviewer</code> to the image server. If meta data is requested plain text is
	 * returned wrapped in a <code>ResponseEntity&ltString&gt</code> else a JPEG image is returned via the <code>HttpServletResponse</code>.
	 * @param entityId the unique image ID. (mandatory)
	 * @param request The incoming HTTP request.
	 * @param response The outgoing HTTP response.
	 * @return Either the meta data or the image returned by the image server.
	 */
	@RequestMapping(value = "/image/iipviewer", method = RequestMethod.GET)
	public ResponseEntity<String> getDataForIIPViewer(@RequestParam(value = "FIF", required = true) final long entityId,
			final HttpServletRequest request, final HttpServletResponse response) {
		
		LOGGER.debug("Received Request: " + request.getQueryString());
				
		final ImageProperties imageProperties = getImageProperties(entityId, resolution_HIGH);
				
		if (imageProperties.httpResponseCode != 200) {
			response.setStatus(imageProperties.httpResponseCode);
			return null;
		}
		
		if (imageProperties.resolution != resolution_HIGH) {
			response.setStatus(403);
			return null;
		}

		final String imageName = imageProperties.name;
		String imageServerInstance = imageProperties.watermark;

		if (StrUtils.isEmptyOrNull(imageServerInstance)) {
			imageServerInstance = imageServerName;
		}

		final String remainingQueryString = request.getQueryString().split("&", 2)[1];
		final String fullQueryString = "?FIF=" + imagePath + imageName + "&" + remainingQueryString;

		LOGGER.debug("Sent Request: " + fullQueryString);

		return getIIPViewerDataFromImageServer(request, response, imageServerInstance, fullQueryString);
	}

	/**
	 * Here the real work for the <code>getDataForIIPViewer</code> is done. This method sends a HTTP-request to the image server and
	 * either gets the meta data or a tile of the requested image. If meta data is fetched it is returned. If an image tile is fetched 
	 * it is written to the HTTP response output stream and <code>null</code> is returned.
	 * @param request The incoming HTTP request
	 * @param response The outgoing HTTP response
	 * @param imageServerInstance The inastance of the image server to use. Sets which watermark is used.
	 * @param fullQueryString The full query string sent by an IIPImage client.
	 * @return Either the meta data of an image wrapped in a <code>ResponseEntity</code> or <code>null</code>.
	 */
	private ResponseEntity<String> getIIPViewerDataFromImageServer(final HttpServletRequest request, final HttpServletResponse response,
			final String imageServerInstance, final String fullQueryString) {
		
		HttpURLConnection connection = null;
		try {
			final URL serverAdress;
			// if the overview image is requested get it without watermark
			if (request.getQueryString().contains("jtl=")) {
				serverAdress = new URL(imageServerPath + imageServerInstance + imageServerExtension + fullQueryString);
			} else {
				serverAdress = new URL(imageServerPath + imageServerName + imageServerExtension + fullQueryString);
			}
			LOGGER.debug("SA: " + serverAdress);
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
					return new ResponseEntity<String>(stringBuilder.toString(), responseHeaders, HttpStatus.OK);
				} else {
					response.setContentType("image/jpeg");
					final OutputStream outputStream = response.getOutputStream();
					ImageIO.write(ImageIO.read(connection.getInputStream()), "jpg", outputStream);
					// no return object needed as the result is directly written to the HTTPResponse
					return null;
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
		return null;
	}
	
	/**
	 * Handles the request for /image/{entityId}. 
	 * @param entityId The unique ID of the image.
	 * @param response The outgoing HTTP response.
	 */
	@RequestMapping(value = "/image/{entityId}", method = RequestMethod.GET)
	public void getImage(@PathVariable("entityId") final long entityId, final HttpServletResponse response) {
		getImageFromServer(entityId, resolution_HIGH, response);
	}
	
	/**
	 * Handles the request for /image/preview/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @param response The outgoing HTTP response.
	 */
	@RequestMapping(value = "/image/preview/{entityId}", method = RequestMethod.GET)
	public void getPreview(@PathVariable("entityId") final long entityId, final HttpServletResponse response) {
		getImageFromServer(entityId, resolution_PREVIEW, response);
	}
	
	/**
	 * Handles the request for /image/thumbnail/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @param response The outgoing HTTP response.
	 */
	@RequestMapping(value = "/image/thumbnail/{entityId}", method = RequestMethod.GET)
	public void getThumbnail(@PathVariable("entityId") final long entityId, final HttpServletResponse response) {
		getImageFromServer(entityId, resolution_THUMBNAIL, response);
	}
	
	/**
	 * Handles the request for /image/icon/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @param response The outgoing HTTP response.
	 */
	@RequestMapping(value = "/image/icon/{entityId}", method = RequestMethod.GET)
	public void getIcon(@PathVariable("entityId") final long entityId, final HttpServletResponse response) {
		getImageFromServer(entityId, resolution_ICON, response);
	}
	
	/**
	 * This private method retrieves images from the image server and writes them as JPEG directly to the HTTP response. 
	 * @param entityId The unique ID of the image.
	 * @param requestedResolution The requested resolution. Only the constants <code>ImageController.ICON</code>, 
	 * <code>ImageController.THUMBNAIL</code>, <code>ImageController.PREVIEW</code> and <code>ImageController.HIGH</code> 
	 * are currently in use but any integer value is allowed.
	 * @param response The outgoing HTTP response.
	 */
	private void getImageFromServer(final long entityId, final int requestedResolution, final HttpServletResponse response) {
		
		HttpURLConnection connection = null;
		
		final ImageProperties imageProperties = getImageProperties(entityId, requestedResolution);
		
		if (imageProperties.httpResponseCode == 200) {
			final String imageName = imageProperties.name;
			String imageServerInstance = imageProperties.watermark;
			final int resolution = imageProperties.resolution;
			
			if (StrUtils.isEmptyOrNull(imageServerInstance)) {
				imageServerInstance = imageServerName;
			}
			
			LOGGER.debug("Watermark: " + imageServerInstance);
			
			try {
				final URL serverAdress = new URL(imageServerPath + imageServerInstance + imageServerExtension + "?FIF=" + imagePath + imageName 
						+ "&SDS=0,90&CNT=1.0&WID=" + resolution + "&HEI=" + resolution + "&QLT=99&CVT=jpeg");
				LOGGER.debug("Full server adress: " + serverAdress);
				connection = (HttpURLConnection)serverAdress.openConnection();			
				connection.setRequestMethod("GET");
				connection.setReadTimeout(imageServerReadTimeout);
				connection.connect();

				if (connection.getResponseCode() == 200) {
					response.setContentType("image/jpeg");
					final OutputStream outputStream = response.getOutputStream();
					ImageIO.write(ImageIO.read(connection.getInputStream()), "jpg", outputStream);
					response.setStatus(200);
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
		} else {
			response.setStatus(imageProperties.httpResponseCode);
		}
	}

	/**
	 * Method to retrieve the name of the image, the allowed maximum resolution and the watermark to use. Maximum resolution and watermark
	 * depend on the rights of the currently logged in user.
	 * @param entityId The unique image ID.
	 * @return An instance of <code>ImagePorperties</code> containing the name, maximum resolution and watermark of the requested image
	 * as well as an HTTP response code indicating success or failure.
	 */
	private ImageProperties getImageProperties(final long entityId, final int requestedResolution) {
		String imageName = null;
		String watermark = null;
		int resolution = requestedResolution;
		
		if (entityId>0) {
			final EntityId arachneId = arachneEntityIdentificationService.getId(entityId);
			
			if (!arachneId.getTableName().equals("marbilder")) {
				LOGGER.error("EntityId {} does not refer to an image.", entityId);
				return new ImageProperties(imageName, resolution, watermark, 404);
			}
			
			final Dataset imageEntity = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId);
			
			imageName = imageEntity.getField("marbilder.PfadNeu");
			LOGGER.debug("Image: " + entityId + ": " + imageName);
			// imageName == null means the user is not allowed to access the image dataset in 'marbilder'
			if (StrUtils.isEmptyOrNull(imageName)) {
				return new ImageProperties(imageName, resolution, watermark, 403);
			}
			
			// Check image rights
			final ImageRightsGroup imageRightsGroup = imageRightsDao.findByName(imageEntity.getField("marbilder.BildrechteGruppe"));
			watermark = imageRightsGroupService.getWatermarkFilename(imageEntity, imageRightsGroup);
			if(!imageRightsGroupService.checkResolutionRight(imageEntity, resolution, imageRightsGroup)) {
				resolution = imageRightsGroupService.getMaxResolution(imageEntity, imageRightsGroup);
				
				// Forbidden
				if (resolution == -1) {
					return new ImageProperties(imageName, resolution, watermark, 403);
				}
			}
		}
		return new ImageProperties(imageName, resolution, watermark, 200);
	}	
	
	/**
	 * Inner class to wrap multiple return values from the <code>getImageProperties</code> method.
	 */
	private class ImageProperties {
		public final transient String name;
		
		public final transient int resolution;
		
		public final transient String watermark;
		
		public final transient int httpResponseCode;
		
		public ImageProperties(final String imageName, final int resolution, final String watermark, final int httpResponseCode) {
			this.name = imageName;
			this.resolution = resolution;
			this.watermark = watermark;
			this.httpResponseCode = httpResponseCode;
		}
	}
}
