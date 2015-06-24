package de.uni_koeln.arachne.controller;

import java.awt.image.BufferedImage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.uni_koeln.arachne.service.IIPService;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

/**
 * Handles HTTP GET requests for images
 * 
 * @author Sven Ole Clemens
 * @author Sebastian Cuy
 * @author Reimar Grabowski
 */
@Controller
public class ImageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);
	
	@Autowired
	private transient IIPService iipService;
	
	/**
	 * Handles the request for /image/width/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @return A <code>BufferedImage</code> wrapped in a <code>ResponseEntity</code>.
	 */
	@RequestMapping(value = "/image/width/{entityId}", 
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<BufferedImage> getWidth(
			@RequestParam(value = "width", required = true) final int requestedWidth, 
			@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<BufferedImage> image = iipService.getImage(entityId, requestedWidth, -1);
		return ResponseEntity.status(image.getStatus()).body(image.getValue());
	}
	
	/**
	 * Handles the request for /image/height/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @return A <code>BufferedImage</code> wrapped in a <code>ResponseEntity</code>.
	 */
	@RequestMapping(value = "/image/height/{entityId}",
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<BufferedImage> getHeight(
			@RequestParam(value = "height", required = true) final int requestedHeight, 
			@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<BufferedImage> image = iipService.getImage(entityId, -1, requestedHeight);
		return ResponseEntity.status(image.getStatus()).body(image.getValue());
	}
	
	/**
	 * Handles the request for /image/{entityId}. 
	 * @param entityId The unique ID of the image.
	 * @return A <code>BufferedImage</code> wrapped in a <code>ResponseEntity</code>.
	 */
	@RequestMapping(value = "/image/{entityId}",
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<BufferedImage> getImage(@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<BufferedImage> image = iipService.getImage(entityId
				, iipService.resolution_HIGH(), iipService.resolution_HIGH());
		return ResponseEntity.status(image.getStatus()).body(image.getValue());
	}
	
	/**
	 * Handles the request for /image/preview/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @return A <code>BufferedImage</code> wrapped in a <code>ResponseEntity</code>.
	 */
	@Deprecated
	@RequestMapping(value = "/image/preview/{entityId}",
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<BufferedImage> getPreview(@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<BufferedImage> image = iipService.getImage(entityId
				, iipService.resolution_PREVIEW(), iipService.resolution_PREVIEW());
		return ResponseEntity.status(image.getStatus()).body(image.getValue());
	}
	
	/**
	 * Handles the request for /image/thumbnail/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @return A <code>BufferedImage</code> wrapped in a <code>ResponseEntity</code>.
	 */
	@Deprecated
	@RequestMapping(value = "/image/thumbnail/{entityId}",
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<BufferedImage> getThumbnail(@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<BufferedImage> image = iipService.getImage(entityId
				, iipService.resolution_THUMBNAIL(), iipService.resolution_THUMBNAIL());
		return ResponseEntity.status(image.getStatus()).body(image.getValue());
	}
	
	/**
	 * Handles the request for /image/icon/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @return A <code>BufferedImage</code> wrapped in a <code>ResponseEntity</code>.
	 */
	@Deprecated
	@RequestMapping(value = "/image/icon/{entityId}",
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<BufferedImage> getIcon(@PathVariable("entityId") final long entityId, final HttpServletResponse response) {
		
		final TypeWithHTTPStatus<BufferedImage> image = iipService.getImage(entityId
				, iipService.resolution_ICON(), iipService.resolution_ICON());
		return ResponseEntity.status(image.getStatus()).body(image.getValue());
	}
		
	/**
	 * This method handles requests using the IIP protocol. If meta data is requested plain text is
	 * returned wrapped in a <code>ResponseEntity&ltString&gt</code> else a JPEG image is returned via the <code>HttpServletResponse</code>.
	 * @param entityId the unique image ID. (mandatory)
	 * @param request The incoming HTTP request.
	 * @param response The outgoing HTTP response.
	 * @return Either the meta data or the image returned by the image server.
	 */
	@RequestMapping(value = "/image/iipviewer",
			method = RequestMethod.GET,
			produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.TEXT_PLAIN_VALUE})
	@Deprecated
	public ResponseEntity<?> getDataForIIPViewer(@RequestParam(value = "FIF", required = true) final long entityId,
			final HttpServletRequest request, final HttpServletResponse response) {
		
		LOGGER.debug("Received Request: " + request.getQueryString());
				
		TypeWithHTTPStatus<?> imageServerResponse = iipService.getIIPViewerDataFromImageServer(entityId, request.getQueryString());
		return ResponseEntity.status(imageServerResponse.getStatus()).body(imageServerResponse.getValue());
	}

	/**
	 * This method handles meta data requests using the Zoomify protocol. The meta data is returned as XML.
	 * @param entityId the unique image ID. (mandatory)
	 * @param response The outgoing HTTP response.
	 * @return The meta data as 'ImageProperties.xml'.
	 */
	@RequestMapping(value = "/image/zoomify/{entityId}/ImageProperties.xml",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getImagePropertiesForZoomifyViewer(@PathVariable("entityId") final long entityId,
			final HttpServletResponse response) {
		
		TypeWithHTTPStatus<String> imageServerResponse = iipService.getImagePropertiesForZoomifyViewer(entityId);
		return ResponseEntity.status(imageServerResponse.getStatus()).body(imageServerResponse.getValue());
	}
	
	/**
	 * This method handles image requests following the Zoomify protocol.
	 * @param entityId the unique image ID. (mandatory)
	 * @param z Zoomify resolution level. (mandatory)
	 * @param x Zoomify coloumn. (mandatory)
	 * @param y Zoomify row. (mandatory)
	 * @return The requested jpeg image.
	 */
	@RequestMapping(value = "/image/zoomify/{entityId}/{z}-{x}-{y}.jpg", method = RequestMethod.GET)
	public ResponseEntity<BufferedImage> getImageForZoomifyViewer(@PathVariable("entityId") final long entityId,
			@PathVariable("z") final int z, @PathVariable("x") final int x, @PathVariable("y") final int y,
			final HttpServletRequest request, final HttpServletResponse response) {
		
		TypeWithHTTPStatus<BufferedImage> imageServerResponse = iipService.getImageForZoomifyViewer(entityId, z, x, y);
		return ResponseEntity.status(imageServerResponse.getStatus()).body(imageServerResponse.getValue());
	}
}
