package de.uni_koeln.arachne.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import de.uni_koeln.arachne.dao.hibernate.ImageRightsDao;
import de.uni_koeln.arachne.mapping.hibernate.ImageRightsGroup;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;
import de.uni_koeln.arachne.util.network.ArachneRestTemplate;

/**
 * Service class to provide communication with the IIP server.
 * 
 * @author Reimar Grabowski
 */
@Service
public class IIPService {

	private static final Logger LOGGER = LoggerFactory.getLogger(IIPService.class);
	
	@Autowired
	private transient EntityIdentificationService arachneEntityIdentificationService; 
	
	@Autowired
	private transient ImageRightsDao imageRightsDao; 

	@Autowired
	private transient ImageRightsGroupService imageRightsGroupService; 
	
	@Autowired
	private transient SingleEntityDataService arachneSingleEntityDataService;
	
	@Autowired
	private transient ArachneRestTemplate restTemplate;
	
	private final transient String imageServerPath;
	private final transient String imageServerName;
	private final transient String imageServerExtension;
	private final transient String imagePath;
		
	private final transient String imageCacheDir;
	
	private final transient int resolution_HIGH;
	private final transient int resolution_PREVIEW;
	private final transient int resolution_THUMBNAIL;
	private final transient int resolution_ICON;
	
	/**
	 * Constructor to initialize the image server parameters set in application.properties.
	 * @param imageServerPath The path of the image server.
	 * @param imageServerName The name of the image server.
	 * @param imageServerExtension The extension (used for watermarking) of the image server
	 * @param imagePath Local image path on the server.
	 * @param resolutionHIGH Width for high resolution images.
	 * @param resolutionTHUMBNAIL Width for thumbnail images.
	 * @param resolutionPREVIEW Width for preview resolution images.
	 * @param resolutionICON Width for icon resolution images. 
	 * @param imageCacheDir Directory where (300px high) images are cached.
	 */
	@Autowired
	public IIPService(final @Value("${imageServerPath}") String imageServerPath,
			final @Value("${imageServerName}") String imageServerName,
			final @Value("${imageServerExtension}") String imageServerExtension,
			final @Value("${imagePath}") String imagePath,
			final @Value("${imageResolutionHIGH}") int resolutionHIGH,
			final @Value("${imageResolutionPREVIEW}") int resolutionPREVIEW,
			final @Value("${imageResolutionTHUMBNAIL}") int resolutionTHUMBNAIL,
			final @Value("${imageResolutionICON}") int resolutionICON,
			final @Value("${imageCacheDir}") String imageCacheDir) {
			
		this.imageServerPath = imageServerPath;
		this.imageServerName = imageServerName;
		this.imageServerExtension = imageServerExtension;
		this.imagePath = imagePath;
		LOGGER.info("ImageServerUrl: " + imageServerPath + imageServerName + imageServerExtension);
		this.resolution_HIGH = resolutionHIGH;
		this.resolution_PREVIEW = resolutionPREVIEW;
		this.resolution_THUMBNAIL = resolutionTHUMBNAIL;
		this.resolution_ICON = resolutionICON;
		
		String cacheDir;
		
		if (!imageCacheDir.startsWith("/")) {
			cacheDir = "/" + imageCacheDir;
		} else {
			cacheDir = imageCacheDir;
		}
		
		if (!cacheDir.endsWith("/")) {
			cacheDir += "/";
		}
		
		this.imageCacheDir = cacheDir;
		
		LOGGER.info("Image cache directory: " + this.imageCacheDir);
	}
		
	/**
	 * This method retrieves images from the image server.
	 * If the requested resolution equals 300 the image is loaded from the local cache directory. If the image isn't 
	 * cached already it will be retrieved from the image server and stored in the cache directory.  
	 * @param entityId The unique ID of the image.
	 * @param requestedWidth The requested width.
	 * @param requestedHeight The requested height.
	 * @return The requested image or <code>null</code> if no image could be retrieved from the server.
	 */
	public TypeWithHTTPStatus<byte[]> getImage(final long entityId, final int requestedWidth, final int requestedHeight) {
		
		final int requestedResolution = Math.max(requestedWidth, requestedHeight); 
		final ImageProperties imageProperties = getImageProperties(entityId, requestedResolution);
		
		if (imageProperties.httpStatus == HttpStatus.OK) {
			final String imageName = imageProperties.name;
			String imageServerInstance = imageProperties.watermark;
			boolean watermarked = true;
			
			int width = -1;
			int height = -1;
			
			// width request
			if (requestedHeight == -1) {
				width = imageProperties.resolution;
				height = imageProperties.maxResolution;
			} else {
				// height request
				if (requestedWidth == -1) {
					width = imageProperties.maxResolution;
					height = imageProperties.resolution;
				} else {
					width = imageProperties.resolution;
					height = width;
				}
			}
						
			if (StrUtils.isEmptyOrNullOrZero(imageServerInstance)) {
				imageServerInstance = imageServerName;
				watermarked = false;
			}
			
			LOGGER.debug("Watermark: " + imageServerInstance);
			
			try {
				byte[] image = null;
				if (requestedHeight == 300) {
					image = getImageFromCacheDir(imageName, watermarked);
				}
				if (image == null) {
					final URL serverAdress = new URL(imageServerPath + imageServerInstance + imageServerExtension 
							+ "?FIF=" + imagePath +	URLEncoder.encode(imageName, "UTF8") 
							+ "&SDS=0,90"
							+ "&CNT=1.0"
							+ "&WID=" + width 
							+ "&HEI=" + height 
							+ "&QLT=99"
							+ "&CVT=jpeg");
					LOGGER.debug("Full server adress: " + serverAdress);

					image = restTemplate.getForObject(serverAdress.toURI(), byte[].class);
					if (requestedHeight == 300) {
						writeImageToCacheDir(imageName, image, watermarked);
					}
				}
				return new TypeWithHTTPStatus<byte[]>(image);
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
			}
		}
		return new TypeWithHTTPStatus<byte[]>(HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Loads a jpeg image from the local cache directory.
	 * @param imageName The image name (*.ptif) including the path.
	 * @param watermarked 
	 * @return The loaded image or <code>null</code> if the image cannot be loaded.
	 */
	private byte[] getImageFromCacheDir(String imageName, boolean watermarked) {
		byte[] image = null;
		Path path = imageNameToCachedImageName(imageName, watermarked);
		try {
			image = Files.readAllBytes(path);
		} catch (IOException e) {
			LOGGER.debug("Failed to load image '" + path.toString() + "' from cache. Cause: " + e.getMessage());
		}
		return image;
	}
	
	/**
	 * Writes a JPEG image to the cache directory. The path of the image will be kept the same and be created if 
	 * necessary (a/b/c.ptif will be stored as $cachedir/a/b/c.jpeg).
	 * @param imageName The image name (*.ptif) including the path.
	 * @param image The image to save.
	 * @param watermarked 
	 */
	private void writeImageToCacheDir(String imageName, byte[] image, boolean watermarked) {
		Path path = imageNameToCachedImageName(imageName, watermarked);
		Path dirPath = path.getParent();
		if (Files.notExists(dirPath, LinkOption.NOFOLLOW_LINKS)) {
			try {
				Files.createDirectories(dirPath);
			} catch (IOException e) {
				LOGGER.error("Failed to create directory '" + dirPath.toString() + "'. Cause: " + e.getMessage());
			}
		}
		try {
			Files.write(path, image);
		} catch (IOException e) {
			LOGGER.error("Failed to write file '" + path.toString() + "'. Cause: " + e.getMessage());
		}
	}
	
	/**
	 * Converts the given <code>imageName</code> to the corresponding name used for caching (in essence it replaces the 
	 * 'ptif' extension with 'jpeg', prefixes the path with the cache directory and returns the result as path object). 
	 * @param imageName The image name (*.ptif) including the path.
	 * @param watermarked 
	 * @return The image path in the cache dir.
	 */
	private Path imageNameToCachedImageName(String imageName, boolean watermarked) {
		String cachedImageName;
		if (watermarked) {
			cachedImageName = imageCacheDir + imageName.substring(0, imageName.length() - 5) + "_watermarked.jpeg";
		} else {
			cachedImageName = imageCacheDir + imageName.substring(0, imageName.length() - 4) + "jpeg";
		}
		return Paths.get(cachedImageName);
	}
	
	/**
	 * Here the real work for the <code>getDataForIIPViewer</code> is done. This method sends a HTTP-request to the image server and
	 * either gets the meta data or a tile of the requested image. If meta data is fetched it is returned. If an image tile is fetched 
	 * it is written to the HTTP response output stream and <code>null</code> is returned.
	 * @param entityId The entity id of an image.
	 * @param queryString The full query string sent by an IIPImage client.
	 * @return Either the meta data of an image wrapped in a <code>ResponseEntity</code> or <code>null</code>.
	 */
	@Deprecated
	public TypeWithHTTPStatus<byte[]> getIIPViewerDataFromImageServer(final long entityId, final String queryString) {
		
		final ImageProperties imageProperties = getImageProperties(entityId, resolution_HIGH());
		
		if (imageProperties.httpStatus != HttpStatus.OK) {
			return new TypeWithHTTPStatus<>(imageProperties.httpStatus);
		}
		
		if (imageProperties.resolution != resolution_HIGH()) {
			return new TypeWithHTTPStatus<>(HttpStatus.FORBIDDEN);
		}

		final String imageName = imageProperties.name;
		String imageServerInstance = imageProperties.watermark;

		if (StrUtils.isEmptyOrNullOrZero(imageServerInstance)) {
			imageServerInstance = imageServerName;
		}

		String remainingQueryString = "";
		if (queryString.contains("&")) {
			remainingQueryString = queryString.split("&", 2)[1];
		}
		final String fullQueryString = "?FIF=" + imagePath + imageName + "&" + remainingQueryString;

		LOGGER.debug("Sent Request: " + fullQueryString);
							
		try {
			final URL serverUrl;
			// if the overview image is requested get it without watermark
			if (queryString.contains("jtl=")) {
				serverUrl = new URL(imageServerPath + imageServerInstance + imageServerExtension + fullQueryString);
			} else {
				serverUrl = new URL(imageServerPath + imageServerName + imageServerExtension + fullQueryString);
			}
			
			LOGGER.debug("Server url: " + serverUrl);
			
			if (fullQueryString.contains("obj=IIP")) {
				final String metaData = restTemplate.getForObject(serverUrl.toURI(), String.class);
				return new TypeWithHTTPStatus<>(metaData.getBytes(StandardCharsets.UTF_8));
			}
			
			final byte[] image = restTemplate.getForObject(serverUrl.toURI(), byte[].class);
			return new TypeWithHTTPStatus<>(image);
		} catch (RestClientException | URISyntaxException | IOException e) {
			LOGGER.error(e.getMessage());
		}
		return new TypeWithHTTPStatus<>(HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Gets the meta data for the zoomify protocol.
	 * @param entityId The entityId of the image.
	 * @return The meta data as String with an added HTTP status.
	 */
	public TypeWithHTTPStatus<String> getImagePropertiesForZoomifyViewer(final long entityId) {
		
		final ImageProperties imageProperties = getImageProperties(entityId, resolution_HIGH);
				
		if (imageProperties.httpStatus != HttpStatus.OK) {
			return new TypeWithHTTPStatus<String>(imageProperties.httpStatus);
		}
		
		if (imageProperties.resolution != resolution_HIGH) {
			return new TypeWithHTTPStatus<String>(HttpStatus.FORBIDDEN);
		}

		final String imageName = imageProperties.name;
		String imageServerInstance = imageProperties.watermark;

		if (StrUtils.isEmptyOrNullOrZero(imageServerInstance)) {
			imageServerInstance = imageServerName;
		}

		try {
			final String queryString = "?Zoomify=" + imagePath + imageName + "/ImageProperties.xml";
			final URL serverAdress = new URL(imageServerPath + imageServerInstance + imageServerExtension + queryString);
			LOGGER.debug("Zoomify request: " + serverAdress);

			final String xml = restTemplate.getForObject(serverAdress.toURI(), String.class);
			return new TypeWithHTTPStatus<String>(xml);
		} catch (RestClientException | URISyntaxException | IOException e) {
			LOGGER.error(e.getMessage());
		}
		return new TypeWithHTTPStatus<String>(HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Retrieves a zoomify image tile from the image server.
	 * @param entityId the unique image ID.
	 * @param z Zoomify resolution level.
	 * @param x Zoomify coloumn.
	 * @param y Zoomify row.
	 * @return The requested image or <code>null</code> and an HTTP status.
	 */
	public TypeWithHTTPStatus<byte[]> getImageForZoomifyViewer(final long entityId, final int z, int x, final int y) {
		
		LOGGER.debug("Zoomify - ID: " + entityId + "TileGroup - z: " + z + " x: " + x + " y: " + y);
		
		final ImageProperties imageProperties = getImageProperties(entityId, resolution_HIGH);
		
		if (imageProperties.httpStatus != HttpStatus.OK) {
			return new TypeWithHTTPStatus<byte[]>(imageProperties.httpStatus);
		}
		
		if (imageProperties.resolution != resolution_HIGH) {
			return new TypeWithHTTPStatus<byte[]>(HttpStatus.FORBIDDEN);
		}

		final String imageName = imageProperties.name;
		String imageServerInstance = imageProperties.watermark;

		if (StrUtils.isEmptyOrNullOrZero(imageServerInstance)) {
			imageServerInstance = imageServerName;
		}

		try {
			final String queryString = "?Zoomify=" + imagePath + imageName + "/TileGroup/" + z + '-' + x + '-' + y;
			final URL serverAdress = new URL(imageServerPath + imageServerInstance + imageServerExtension + queryString);
			LOGGER.debug("Zoomify request: " + serverAdress);

			final byte[] image = restTemplate.getForObject(serverAdress.toURI(), byte[].class);
			return new TypeWithHTTPStatus<byte[]>(image);
		} catch (RestClientException | URISyntaxException | IOException e) {
			LOGGER.error(e.getMessage());
		}
		return new TypeWithHTTPStatus<byte[]>(HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Method to retrieve the name of the image, the allowed maximum resolution and the watermark to use. Maximum 
	 * resolution and watermark depend on the rights of the currently logged in user.
	 * @param entityId The unique image ID.
	 * @param requestedResolution The requested resolution of the image.
	 * @return An instance of <code>ImagePorperties</code> containing the name, granted resolution, maximum resolution 
	 * and watermark of the requested image as well as an HTTP response code indicating success or failure.
	 */
	public ImageProperties getImageProperties(final long entityId, final int requestedResolution) {
		String imageName = null;
		String watermark = null;
		int resolution = requestedResolution;
		int maxResolution;		
		
		if (entityId>0) {
			final EntityId arachneId = arachneEntityIdentificationService.getId(entityId);
			
			if (arachneId.getArachneEntityID() == null || !arachneId.getTableName().equals("marbilder")) {
				LOGGER.error("EntityId {} does not refer to an image.", entityId);
				return new ImageProperties(imageName, -1, -1, watermark, HttpStatus.NOT_FOUND);
			}

			if (!arachneId.isDeleted()) {
				return new ImageProperties(imageName, -1, -1, watermark, HttpStatus.GONE);
			}
			
			final Dataset imageEntity = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId);
			
			imageName = imageEntity.getField("marbilder.PfadNeu");
			LOGGER.debug("Image: " + entityId + ": " + imageName);
			// imageName == null means the user is not allowed to access the image dataset in 'marbilder'
			if (StrUtils.isEmptyOrNullOrZero(imageName)) {
				return new ImageProperties(imageName, -1, -1, watermark, HttpStatus.FORBIDDEN);
			}
			
			// Check image rights
			final ImageRightsGroup imageRightsGroup = imageRightsDao.findByName(imageEntity.getField("marbilder.BildrechteGruppe"));
			watermark = imageRightsGroupService.getWatermarkFilename(imageEntity, imageRightsGroup);
			if(!imageRightsGroupService.checkResolutionRight(imageEntity, resolution, imageRightsGroup)) {
				maxResolution = imageRightsGroupService.getMaxResolution(imageEntity, imageRightsGroup); 
				
				// if 'high' (0) is not allowed it can never be granted here
				if (resolution == 0 || (maxResolution != 0 && maxResolution < resolution)) {
					resolution = maxResolution;
				}
				
				// Forbidden
				if (resolution == -1) {
					return new ImageProperties(imageName, -1, -1, watermark, HttpStatus.FORBIDDEN);
				}
			} else {
				maxResolution = imageRightsGroupService.getMaxResolution(imageEntity, imageRightsGroup);
			}
			
			return new ImageProperties(imageName, resolution, maxResolution, watermark, HttpStatus.OK);
		}
		LOGGER.error("Negative EntityId {} does not refer to an image.", entityId);
		return new ImageProperties(imageName, -1, -1, watermark, HttpStatus.NOT_FOUND);
	}	
	
	/**
	 * Inner class to wrap multiple return values from the <code>getImageProperties</code> method.
	 */
	private class ImageProperties {
		public final transient String name;
		
		public final transient int resolution;
		
		public final transient int maxResolution;
		
		public final transient String watermark;
		
		public final transient HttpStatus httpStatus;
		
		public ImageProperties(final String imageName, final int resolution, final int maxResolution, 
				final String watermark, final HttpStatus httpStatus) {
			this.name = imageName;
			this.resolution = resolution;
			this.maxResolution = maxResolution;
			this.watermark = watermark;
			this.httpStatus = httpStatus;
		}
	}
		
	/**
	 * @return the resolution_HIGH
	 */
	public int resolution_HIGH() {
		return resolution_HIGH;
	}

	/**
	 * @return the resolution_PREVIEW
	 */
	public int resolution_PREVIEW() {
		return resolution_PREVIEW;
	}

	/**
	 * @return the resolution_THUMBNAIL
	 */
	public int resolution_THUMBNAIL() {
		return resolution_THUMBNAIL;
	}

	/**
	 * @return the resolution_ICON
	 */
	public int resolution_ICON() {
		return resolution_ICON;
	}
}
