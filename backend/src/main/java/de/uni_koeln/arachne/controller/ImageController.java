package de.uni_koeln.arachne.controller;

import java.security.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
	 * 
	 * @param entityId       The unique ID of the image.
	 * @param requestedWidth The requested width.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@GetMapping(value = "width/{entityId}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getWidth(
			@RequestParam(value = "width", required = true) final int requestedWidth,
			@PathVariable final long entityId) {

		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, requestedWidth, -1);
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}

	/**
	 * Handles the request for /image/height/{entityId}.
	 * 
	 * @param entityId        The unique ID of the image.
	 * @param requestedHeight The requested height.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@GetMapping(value = "height/{entityId}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getHeight(
			@RequestParam(value = "height", required = true) final int requestedHeight,
			@PathVariable final long entityId) {

		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, -1, requestedHeight);
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}

	/**
	 * Handles the request for /image/{entityId}.
	 * 
	 * @param entityId The unique ID of the image.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@GetMapping(value = "{entityId}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getImage(@PathVariable final long entityId) {

		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, iipService.resolution_HIGH(),
				iipService.resolution_HIGH());
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}

	/**
	 * Handles the request for /image/preview/{entityId}.
	 * 
	 * @param entityId The unique ID of the image.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@Deprecated
	@GetMapping(value = "preview/{entityId}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getPreview(@PathVariable final long entityId) {

		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, iipService.resolution_PREVIEW(),
				iipService.resolution_PREVIEW());
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}

	/**
	 * Handles the request for /image/thumbnail/{entityId}.
	 * 
	 * @param entityId The unique ID of the image.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@Deprecated
	@GetMapping(value = "thumbnail/{entityId}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getThumbnail(@PathVariable final long entityId) {

		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, iipService.resolution_THUMBNAIL(),
				iipService.resolution_THUMBNAIL());
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}

	/**
	 * Handles the request for /image/icon/{entityId}.
	 * 
	 * @param entityId The unique ID of the image.
	 * @return A byte array wrapped in a <code>ResponseEntity</code>.
	 */
	@Deprecated
	@GetMapping(value = "icon/{entityId}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getIcon(@PathVariable final long entityId) {

		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, iipService.resolution_ICON(),
				iipService.resolution_ICON());
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(image.getStatus()).headers(headers).body(image.getValue());
	}

	/**
	 * This method handles requests using the IIP protocol. If meta data is
	 * requested plain text is
	 * returned wrapped in a <code>ResponseEntity&ltString&gt</code> else a JPEG
	 * image is returned via the <code>HttpServletResponse</code>.
	 * 
	 * @param entityId the unique image ID. (mandatory)
	 * @param request  The incoming HTTP request.
	 * @param response The outgoing HTTP response.
	 * @return Either the meta data or the image returned by the image server.
	 */
	@GetMapping(value = "iipviewer", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.TEXT_PLAIN_VALUE })
	@Deprecated
	public ResponseEntity<byte[]> getDataForIIPViewer(@RequestParam(value = "FIF", required = true) final long entityId,
			final HttpServletRequest request, final HttpServletResponse response) {

		LOGGER.debug("Received Request: " + request.getQueryString());

		TypeWithHTTPStatus<byte[]> imageServerResponse = iipService.getIIPViewerDataFromImageServer(entityId,
				request.getQueryString());
		return ResponseEntity.status(imageServerResponse.getStatus()).body(imageServerResponse.getValue());
	}

	/**
	 * This method handles meta data requests using the Zoomify protocol. The meta
	 * data is returned as XML.
	 * 
	 * @param entityId the unique image ID. (mandatory)
	 * @param response The outgoing HTTP response.
	 * @return The meta data as 'ImageProperties.xml'.
	 */
	@GetMapping(value = "zoomify/{entityId}/ImageProperties.xml", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> getImagePropertiesForZoomifyViewer(@PathVariable final long entityId,
			final HttpServletResponse response) {

		TypeWithHTTPStatus<String> imageServerResponse = iipService.getImagePropertiesForZoomifyViewer(entityId);
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.APPLICATION_XML_VALUE);
		return ResponseEntity.status(imageServerResponse.getStatus()).headers(headers)
				.body(imageServerResponse.getValue());
	}

	/**
	 * This method handles image requests following the Zoomify protocol.
	 * 
	 * @param entityId the unique image ID. (mandatory)
	 * @param z        Zoomify resolution level. (mandatory)
	 * @param x        Zoomify coloumn. (mandatory)
	 * @param y        Zoomify row. (mandatory)
	 * @return The requested jpeg image.
	 */
	@GetMapping(value = "zoomify/{entityId}/{z}-{x}-{y}.jpg", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getImageForZoomifyViewer(@PathVariable final long entityId,
			@PathVariable final int z, @PathVariable final int x, @PathVariable final int y) {

		TypeWithHTTPStatus<byte[]> imageServerResponse = iipService.getImageForZoomifyViewer(entityId, z, x, y);
		final HttpHeaders headers = new HttpHeaders();
		headers.add("content-Type", MediaType.IMAGE_JPEG_VALUE);
		return ResponseEntity.status(imageServerResponse.getStatus()).headers(headers)
				.body(imageServerResponse.getValue());
	}

	/**
	 * This method handles requests for an image checksum (/image/checksum/entityId)
	 * 
	 * @param entityId the unique image ID. (mandatory)
	 * @return The requested md5-checksum
	 */
	@GetMapping(value = "checksum/{entityId}", produces = { APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<String> getChecksum(@PathVariable final long entityId) {

		final TypeWithHTTPStatus<byte[]> image = iipService.getImage(entityId, iipService.resolution_HIGH(),
				iipService.resolution_HIGH());
		if (image.getStatus().equals(HttpStatus.NOT_FOUND) || image.getStatus().equals(HttpStatus.BAD_REQUEST))
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("The requested image (" + entityId + ") has not been found.");

		final byte[] imageByte = image.getValue();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] outputByte = md.digest(imageByte);
			return ResponseEntity.status(image.getStatus()).body(DatatypeConverter.printHexBinary(outputByte));
		} catch (NoSuchAlgorithmException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
}
