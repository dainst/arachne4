package de.uni_koeln.arachne.controller;

import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.JSONUtil;
import de.uni_koeln.arachne.util.image.ImageMimeUtil;

/**
 * This class serves 3D model data and texture data as well as the JSON meta data needed by the Javascript 3D viewer.
 */
@Controller
public class Model3DController {
	private static final Logger LOGGER = LoggerFactory.getLogger(Model3DController.class);

	private static final String[] BINARY_FORMATS = {"ply", "nxs", "nxz"};

	@Autowired
	private transient EntityIdentificationService entityIdentificationService;

	@Autowired
	private transient UserRightsService userRightsService;

	@Autowired
	private transient SingleEntityDataService singleEntityDataService;

	private transient final String basePath;

	/**
	 * Constructor setting the path to the 3D models directory.
	 * @param basePath The path.
	 */
	@Autowired
	public Model3DController(final @Value("${model3dBasePath}") String basePath) {
		this.basePath = basePath;
	}

	/**
	 * Sends either model data or metadata if requested. Supported formats for
	 * model data are '.obj', '.stl' (ASCII or binary encoded), '.svg', '.ply'
	 * and '.nxz'.
	 *
	 * Model data is either transferred as a whole (standard) or partially
	 * (when a Range-Header is present).
	 *
	 * This method acts as a facade and forwards to more specific endpoints that
	 * can also be used directly in order to avoid getting the dataset twice.
	 *
	 * @param modelId The internal ID of the 3D model.
	 * @param isMeta Flag to indicate wether meta data or the actual model should be served.
	 * @param response The <code>HTTPServeletResponse</code>
	 * @param headers The request's <code>HttpHeaders</code>
	 * @return Either the metadata as JSON or model data in one of the supported formats.
	 */
	@RequestMapping(value = "/model/{modelId}", method = RequestMethod.GET)
	public String handleModelRequest(@PathVariable("modelId") final Long modelId
			, @RequestParam(value = "meta", required = false) final Boolean isMeta
			, final HttpServletResponse response
			, @RequestHeader final HttpHeaders headers) {

		final Dataset dataset = getDataset(modelId, response);

		if (dataset == null) {
			return null;
		}

		if (isMeta != null && isMeta) {
			return "forward:/model/meta/" + modelId;
		} else {
			List<HttpRange> range = headers.getRange();
			if (range != null && range.size() > 0) {
				return "forward:/model/partial/" + modelId;
			} else {
				return "forward:/model/full/" + modelId;
			}
		}
	}

	/**
	 * Sends full model data.
	 * @param modelId The internal ID of the 3D model.
	 * @param response <code>The HTTPServeletResponse</code>
	 * @return The model data
	 */
	@RequestMapping(value = "/model/full/{modelId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<byte[]> handleFullModelRequest(@PathVariable("modelId") final Long modelId
			, final HttpServletResponse response) {

		final Dataset dataset = getDataset(modelId, response);
		if (dataset == null) {
			return null;
		}
		return buildFullModelResponse(dataset);
	}

	/**
	 * Sends partial model data as given in the Range header.
	 * @param modelId The internal ID of the 3D model.
	 * @param response <code>The HTTPServeletResponse</code>
	 * @param headers The request's <code>HttpHeaders</code>
	 * @return The partial model data
	 */
	@RequestMapping(value = "/model/partial/{modelId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<ResourceRegion> handlePartialModelRequest(@PathVariable("modelId") final Long modelId
			, final HttpServletResponse response
			, @RequestHeader final HttpHeaders headers) {

		final Dataset dataset = getDataset(modelId, response);
		if (dataset == null) {
			return null;
		}
		List<HttpRange> range = headers.getRange();
		if (range != null && range.size() > 0) {
			return buildPartialModelResponse(dataset, range.get(0));
		} else {
			HttpHeaders reponseHeaders = new HttpHeaders();
			reponseHeaders.add("Location", "/model/full/" + modelId);
			return new ResponseEntity<>(reponseHeaders, HttpStatus.FOUND);
		}
	}

	/**
	 * Sends model metadata.
	 * @param modelId The internal ID of the 3D model.
	 * @param response <code>The HTTPServeletResponse</code>
	 * @return Model metadata as JSON
	 */
	@RequestMapping(value = "/model/meta/{modelId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<String> handleMetadataRequest(@PathVariable("modelId") final Long modelId
			, final HttpServletResponse response) {

		final Dataset dataset = getDataset(modelId, response);
		if (dataset == null) {
			return null;
		}
		return buildMetadataResponse(dataset);
	}

	/**
	 * Request handler for material data. The only supported format is '.mtl'.
	 * @param modelId The ID of the 3D model.
	 * @param response The <code>HTTPServletResponse</code>
	 * @return The '.mtl'-file.
	 */
	@RequestMapping(value = "/model/material/{modelId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<String> handleMaterialRequest(@PathVariable("modelId") final Long modelId
			, final HttpServletResponse response) {

		final Dataset dataset = getDataset(modelId, response);

		if (dataset == null) {
			return null;
		}
		final String materialData = getMaterialData(dataset);
		final HttpHeaders responseHeaders = new HttpHeaders();
	    responseHeaders.add("Content-Type", "text/plain; charset=utf-8");
	    return new ResponseEntity<String>(materialData, responseHeaders, HttpStatus.OK);
	}

	/**
	 * Request handler for texture requests.
	 * @param modelId The internal ID of the 3D Model.
	 * @param request The <code>HTTPServletRequest</code>
	 * @param response The <code>HTTPServletResponse</code>
	 * @return The image file.
	 */
	@RequestMapping(value = "/model/material/{modelId}/**",
			method = RequestMethod.GET,
			produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE})
	public @ResponseBody ResponseEntity<byte[]> handleTextureRequest(@PathVariable("modelId") final Long modelId
			, final HttpServletRequest request
			, final HttpServletResponse response) {

		final Dataset dataset = getDataset(modelId, response);

		if (dataset == null) {
			return null;
		}

		String requestURL = request.getRequestURL().toString();
		String texturePath = requestURL.split("/model/material/\\d+/")[1];

		final byte[] textureData = getTextureData(dataset, texturePath);
		String mimeType = ImageMimeUtil.getImageType(textureData);
		if (mimeType == null) {
			return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
		}

		final HttpHeaders responseHeaders = new HttpHeaders();
	    responseHeaders.add("Content-Type", mimeType + "; charset=utf-8");
		return new ResponseEntity<byte[]>(textureData, responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/model/file/**", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<byte[]> handleModelFileRequest(final HttpServletRequest request,
			final HttpServletResponse response) {

		String requestURL = request.getRequestURL().toString();
		String pathInput = requestURL.split("/model/file/")[1];
		// Get rid of any '..' which could (theoretically) escape the model dir.
		Path normalized = Paths.get(pathInput).normalize();

		final String fileName = normalized.getFileName().toString();
		final String contentType = hasBinaryFileEnding(fileName) ? "application/octet-stream"
				: "text/plain; charset=utf-8";

		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", contentType);
		responseHeaders.add("Content-Disposition", "attachment; filename=" + fileName);
		final byte[] fileData = getFileData(normalized.toString());
		return new ResponseEntity<byte[]>(fileData, responseHeaders, HttpStatus.OK);
	}

	private ResponseEntity<String> buildMetadataResponse(Dataset dataset) {
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
		return new ResponseEntity<>(getMetaData(dataset), responseHeaders, HttpStatus.OK);
	}

	private ResponseEntity<ResourceRegion> buildPartialModelResponse(Dataset dataset, HttpRange range) {
		ResourceRegion region = range.toResourceRegion(new FileSystemResource(getModelFile(dataset)));
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/octet-stream");
		return new ResponseEntity<>(region, responseHeaders, HttpStatus.PARTIAL_CONTENT);
	}

	private ResponseEntity<byte[]> buildFullModelResponse(Dataset dataset) {
		final byte[] modelData = getModelData(dataset);
		if (modelData != null) {
			if (isBinary(dataset, modelData)) {
				LOGGER.debug("building binary response");
				return buildBinaryModelResponse(dataset, modelData);
			} else {
				LOGGER.debug("building text response");
				return buildTextModelResponse(dataset, modelData);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	private ResponseEntity<byte[]> buildBinaryModelResponse(Dataset dataset, byte[] modelData) {
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/octet-stream");
		return new ResponseEntity<byte[]>(modelData, responseHeaders, HttpStatus.OK);
	}

	private ResponseEntity<byte[]> buildTextModelResponse(Dataset dataset, byte[] modelData) {
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/plain; charset=utf-8");
		return new ResponseEntity<byte[]>(modelData, responseHeaders, HttpStatus.OK);
	}

	/**
	 * Retrieves the dataset for the give entity id.
	 * @param modelId The internal ID of the 3D model.
	 * @param response The <code>HTTPServletResponse</code>
	 * @return The dataset.
	 */
	private Dataset getDataset(final long modelId, final HttpServletResponse response) {
		final EntityId arachneId = entityIdentificationService.getId("modell3d", modelId);

    	if (arachneId == null || arachneId.isDeleted()) {
    		response.setStatus(404);
    		return null;
    	}

    	final String datasetGroupName = singleEntityDataService.getDatasetGroup(arachneId);
    	final DatasetGroup datasetGroup = new DatasetGroup(datasetGroupName);
    	if (!userRightsService.userHasDatasetGroup(datasetGroup)) {
    		response.setStatus(403);
    		return null;
    	}

    	return singleEntityDataService.getSingleEntityByArachneId(arachneId);
	}

	/**
	 * Retrieves the information needed by the Javascript 3D-Viewer from the dataset.
	 * @param dataset The dataset of interest.
	 * @return The meta data as JSON.
	 */
	private String getMetaData(final Dataset dataset) {
		final ObjectNode result = JSONUtil.getObjectNode();
		result.put("title", dataset.getFieldFromFields("modell3d.Titel"));
		result.put("format", dataset.getFieldFromFields("modell3d.Dateiformat"));
		result.put("modeller", dataset.getFieldFromFields("modell3d.Modellierer"));
		result.put("license", dataset.getFieldFromFields("modell3d.Lizenz"));
		result.put("path", dataset.getFieldFromFields("modell3d.Pfad"));
		result.put("fileName", dataset.getFieldFromFields("modell3d.Dateiname"));
		result.put("modelType", dataset.getFieldFromFields("modell3d.Modelltyp"));
		return result.toString();
	}

	/**
	 * Reads model data from disk (binary or text).
	 * @param dataset The dataset describing the model.
	 * @return The model data as <code>byte</code> array or <code>null</code> on failure.
	 */
	private byte[] getModelData(final Dataset dataset) {
		final File modelFile = getModelFile(dataset);
		if (modelFile.isFile() && modelFile.canRead()) {
			try {
				return Files.toByteArray(modelFile);
			} catch (IOException e) {
				LOGGER.error("Problem reading model file. Cause: ", e);
			}
		} else {
			LOGGER.error("Could not read 3D model file: " + modelFile.getAbsolutePath());
		}
		return null;
	}

	private File getModelFile(final Dataset dataset) {
		String modelPath = dataset.getFieldFromFields("modell3d.Pfad");
		if (!modelPath.endsWith("/")) {
			modelPath += "/";
		}
		final String filename = dataset.getFieldFromFields("modell3d.Dateiname");
		final String pathname = basePath + modelPath + filename;
		return new File(pathname);
	}

	/**
	 * Determines if a .stl file is binary or ASCII encoded by looking at the number of triangles in the file and calculating
	 * an expected binary size. If the size does not match it is a text file.
	 * @return <code>true</code> if the file is binary.
	 */
	private boolean isBinary(final Dataset dataset, final byte[] data) {
		String format = dataset.getFieldFromFields("modell3d.Dateiformat");
		if (format.equals("stl")) {
			// Get the first four bytes after the 80 character header of a binary stl file as an unsigned 32 bit integer value.
			// This is the number of triangles in the file.
			final long triangles =
					((data[80] & 0xFF) <<  0) |
					((data[81] & 0xFF) <<  8) |
					((data[82] & 0xFF) << 16) |
					((data[83] & 0xFF) << 24);

			final long face_size = 32 / 8 * 3 + 32 / 8 * 3 * 3 + 16 / 8;
			final long expectedSize = 80 + 32 / 8 + triangles * face_size;
			return expectedSize == data.length;

		} else if (Arrays.asList(this.BINARY_FORMATS).contains(format)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determine if a filename ends in an extension that makes it likely that the
	 * file is binary.
	 *
	 * @return <code>true</code> if the filename is likely that of a binary file.
	 */
	private boolean hasBinaryFileEnding(final String filename) {
		for (final String format : BINARY_FORMATS) {
			if (filename.toLowerCase().endsWith(format)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reads a mtl-file from disk.
	 * @param dataset The dataset of interest.
	 * @return The material file as String.
	 */
	private String getMaterialData(final Dataset dataset) {
		String modelPath = dataset.getFieldFromFields("modell3d.Pfad");
		if (!modelPath.endsWith("/")) {
			modelPath += "/";
		}

		String filename = dataset.getFieldFromFields("modell3d.DateinameMTL");
		final String pathname = basePath + modelPath + filename;
		final File materialFile = new File(pathname);

		if (materialFile.isFile() && materialFile.canRead()) {
			try {
				return Files.toString(materialFile, Charsets.UTF_8);
			} catch (IOException e) {
				LOGGER.error("Problem reading material file. Caused by: ", e);
			}
		} else {
			LOGGER.error("Could not read material file: " + pathname);
		}
		return null;
	}

	/**
	 * Reads an image file from disc.
	 * @param dataset The dataset of interest.
	 * @param texturePath The path of the image file.
	 * @return The image data as <code>byte</code> array or <code>null</code> on failure.
	 */
	private byte[] getTextureData(final Dataset dataset, final String texturePath) {
		String modelPath = dataset.getFieldFromFields("modell3d.Pfad");
		if (!modelPath.endsWith("/")) {
			modelPath += "/";
		}
		return getFileData(modelPath + texturePath);
	}

	private byte[] getFileData(final String filePath) {

		final String fullPath = basePath + '/' + filePath;
		final File file = new File(fullPath);

		if (file.isFile() && file.canRead()) {
			try {
				return Files.toByteArray(file);
			} catch (IOException e) {
				LOGGER.error("Problem reading file. Caused by: ", e);
			}
		} else {
			LOGGER.error("Could not read file: " + fullPath);
		}
		return null;
	}
}
