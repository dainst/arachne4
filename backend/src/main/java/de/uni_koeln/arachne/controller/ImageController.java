package de.uni_koeln.arachne.controller;

import java.security.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import de.uni_koeln.arachne.service.IIPService;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * Handles HTTP GET requests for images
 * 
 * @author Sven Ole Clemens
 * @author Sebastian Cuy
 * @author Reimar Grabowski
 */
@Controller
@RequestMapping("/image")
public class ImageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);
	
	@Autowired
	private transient IIPService iipService;
	
	/**
	 * Handles the request for /image/width/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @param requestedWidth The requested width.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@RequestMapping(value = "width/{entityId}", 
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getWidth(
			@RequestParam(value = "width", required = true) final int requestedWidth, 
			@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, requestedWidth, -1);
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}
	
	/**
	 * Handles the request for /image/height/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @param requestedHeight The requested height.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@RequestMapping(value = "height/{entityId}",
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getHeight(
			@RequestParam(value = "height", required = true) final int requestedHeight, 
			@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, -1, requestedHeight);
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}
	
	/**
	 * Handles the request for /image/{entityId}. 
	 * @param entityId The unique ID of the image.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@RequestMapping(value = "{entityId}",
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getImage(@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId
				, iipService.resolution_HIGH(), iipService.resolution_HIGH());
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}
	
	/**
	 * Handles the request for /image/preview/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@Deprecated
	@RequestMapping(value = "preview/{entityId}",
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getPreview(@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId
				, iipService.resolution_PREVIEW(), iipService.resolution_PREVIEW());
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}
	
	/**
	 * Handles the request for /image/thumbnail/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@Deprecated
	@RequestMapping(value = "thumbnail/{entityId}",
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getThumbnail(@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId
				, iipService.resolution_THUMBNAIL(), iipService.resolution_THUMBNAIL());
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}
	
	/**
	 * Handles the request for /image/icon/{entityId}.
	 * @param entityId The unique ID of the image.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@Deprecated
	@RequestMapping(value = "icon/{entityId}",
			method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getIcon(@PathVariable("entityId") final long entityId) {
		
		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId
				, iipService.resolution_ICON(), iipService.resolution_ICON());
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}
		
	/**
	 * This method handles requests using the IIP protocol. If meta data is requested plain text is
	 * returned wrapped in a <code>ResponseEntity&ltString&gt</code> else a JPEG image is returned via the <code>HttpServletResponse</code>.
	 * @param entityId the unique image ID. (mandatory)
	 * @param request The incoming HTTP request.
	 * @param response The outgoing HTTP response.
	 * @return Either the meta data or the image returned by the image server.
	 */
	@RequestMapping(value = "iipviewer",
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
	@RequestMapping(value = "zoomify/{entityId}/ImageProperties.xml",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getImagePropertiesForZoomifyViewer(@PathVariable("entityId") final long entityId,
			final HttpServletResponse response) {
		
		TypeWithHTTPStatus<String> imageServerResponse = iipService.getImagePropertiesForZoomifyViewer(entityId);
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.APPLICATION_XML_VALUE);
		return ResponseEntity.status(imageServerResponse.getStatus()).headers(headers).body(imageServerResponse.getValue());
	}
	
	/**
	 * This method handles image requests following the Zoomify protocol.
	 * @param entityId the unique image ID. (mandatory)
	 * @param z Zoomify resolution level. (mandatory)
	 * @param x Zoomify coloumn. (mandatory)
	 * @param y Zoomify row. (mandatory)
	 * @return The requested jpeg image.
	 */
	@RequestMapping(value = "zoomify/{entityId}/{z}-{x}-{y}.jpg", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getImageForZoomifyViewer(@PathVariable("entityId") final long entityId,
			@PathVariable("z") final int z, @PathVariable("x") final int x, @PathVariable("y") final int y) {
		
		TypeWithHTTPStatus<byte[]> imageServerResponse = iipService.getImageForZoomifyViewer(entityId, z, x, y);
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(imageServerResponse.getStatus()).headers(headers).body(imageServerResponse.getValue());
	}

    /**
     * This method handles requests for an image checksum (/image/checksum/entityId)
     * @param entityId the unique image ID. (mandatory)
     * @return The requested md5-checksum
     */
	@RequestMapping(value = "checksum/{entityId}",
            method = RequestMethod.GET,
            produces={APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<String> getChecksum(@PathVariable("entityId") final long entityId) {

        final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, iipService.resolution_HIGH(), iipService.resolution_HIGH());
        if(image.getStatus().equals(HttpStatus.NOT_FOUND))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The requested image (" + entityId + ") has not been found.");

	    final byte[] imageByte = image.getValue();
	    try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] outputByte = md.digest(imageByte);
            return ResponseEntity.status(image.getStatus()).body(DatatypeConverter.printHexBinary(outputByte));
        }
        catch(NoSuchAlgorithmException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
