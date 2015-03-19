package de.uni_koeln.arachne.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.JSONUtil;
import de.uni_koeln.arachne.util.image.ImageMimeUtil;

/**
 * This class serves 3D model data and texture data as well as the JSON meta data needed by the Javascript 3D viewer.
 */
@Controller
public class Model3DController {
	private static final Logger LOGGER = LoggerFactory.getLogger(Model3DController.class);
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService;
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	@Autowired
	private transient SingleEntityDataService singleEntityDataService;
	
	@Autowired
	private transient JSONUtil jsonUtil;
	
	private transient final String basePath;
	
	@Autowired
	public Model3DController(final @Value("#{config.model3dBasePath}") String basePath) {
		this.basePath = basePath;
	}
	
	/**
	 * Sends either model data or meta data if requested. Supported formats for model data are '.obj' and '.stl' ASCII or binary encoded.
	 * @param modelId The internal ID of the 3D model.
	 * @param isMeta Flag to indicate wether meta data or the actual model should be served.
	 * @param response <code>The HTTPServeletResponse</code>
	 * @return Either the meta data as JSON or model data in one of the supported formats.
	 */
	@RequestMapping(value = "/model/{modelId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> handleModelRequest(@PathVariable("modelId") final Long modelId
			, @RequestParam(value = "meta", required = false) final Boolean isMeta
			, final HttpServletResponse response) {
		
		final Dataset dataset = getDataset(modelId, response);
    	
		if (dataset == null) {
			return null;
		}
		
    	if (isMeta != null && isMeta) {
    		final HttpHeaders responseHeaders = new HttpHeaders();
    	    responseHeaders.add("Content-Type", "application/json; charset=utf-8");
			return new ResponseEntity<String>(getMetaData(dataset), responseHeaders, HttpStatus.OK);
		} else {
			final HttpHeaders responseHeaders = new HttpHeaders();
			final byte[] modelData = getModelData(dataset);
			if (modelData != null) {
				if (isBinaryStl(modelData)) {
					responseHeaders.add("Content-Type", "application/octet-stream");
					return new ResponseEntity<byte[]>(modelData, responseHeaders, HttpStatus.OK);
				} else {
					responseHeaders.add("Content-Type", "text/plain; charset=utf-8");
					return new ResponseEntity<String>(new String(modelData, Charsets.UTF_8), responseHeaders, HttpStatus.OK);
				}
			} else {
				return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
			}
		}
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
	 * @param textureName The filename of the texture.
	 * @param response The <code>HTTPServletResponse</code>
	 * @return The image file.
	 */
	// use regexp workaround for spring truncating at dots in parameters
	@RequestMapping(value = "/model/material/{modelId}/texture/{textureName:.+}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<byte[]> handleModelRequest(@PathVariable("modelId") final Long modelId
			, @PathVariable("textureName") final String textureName
			, final HttpServletResponse response) {
		
		final Dataset dataset = getDataset(modelId, response);
    	
		if (dataset == null) {
			return null;
		}
		
		final byte[] textureData = getTextureData(dataset, textureName);
		final String mimeType = ImageMimeUtil.getImageType(textureData);
		if (mimeType == null) {
			return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
		}
		
		final HttpHeaders responseHeaders = new HttpHeaders();
	    responseHeaders.add("Content-Type", mimeType + "; charset=utf-8");
		return new ResponseEntity<byte[]>(textureData, responseHeaders, HttpStatus.OK);
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
	 * Retrieves the information needed by the Jvascript 3D-Viewer from the dataset.
	 * @param dataset The dataset of interest.
	 * @return The meta data as JSON. 
	 */
	private String getMetaData(final Dataset dataset) {
		final ObjectNode result = jsonUtil.getObjectNode();
		result.put("title", dataset.getFieldFromFields("modell3d.Titel"));
		result.put("format", dataset.getFieldFromFields("modell3d.Dateiformat"));
		result.put("modeller", dataset.getFieldFromFields("modell3d.Modellierer"));
		result.put("license", dataset.getFieldFromFields("modell3d.Lizenz"));
		return result.toString();
	}

	/**
	 * Reads model data from disk (binary or text). 
	 * @param dataset The dataset describing the model.
	 * @return The model data as <code>byte</code> array or <code>null</code> on failure.
	 */
	private byte[] getModelData(final Dataset dataset) {
		String modelPath = dataset.getFieldFromFields("modell3d.Pfad");
		if (!modelPath.endsWith("/")) {
			modelPath += "/"; 
		}
		final String filename = dataset.getFieldFromFields("modell3d.Dateiname");
				
		final String pathname = basePath + modelPath + filename;
		
		final File modelFile = new File(pathname);  
		
		if (modelFile.isFile() && modelFile.canRead()) {
			try {
				return Files.toByteArray(modelFile);
			} catch (IOException e) {
				LOGGER.error("Problem reading model file. Cause: ", e);
			}
		} else {
			LOGGER.error("Could not read 3D model file: " + pathname);
		}
		return null;
	}
	
	/**
	 * Determines if a .stl file is binary or ASCII encoded by looking at the number of triangles in the file and calculating
	 * an expected binary size. If the size does not match it is a text file.
	 * @return <code>true</code> if the file is binary.
	 */
	private boolean isBinaryStl(final byte[] data) {
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
		
		String filename = dataset.getFieldFromFields("modell3d.Dateiname");
		final int dotIndex=filename.lastIndexOf('.');
		if (dotIndex >= 0) { // prevent exception if there is no dot
		  filename = filename.substring(0, dotIndex) + ".mtl";
		}
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
	 * @param textureName The filename of the image file.
	 * @return The image data as <code>byte</code> array.
	 */
	private byte[] getTextureData(final Dataset dataset, final String textureName) {
		String modelPath = dataset.getFieldFromFields("modell3d.Pfad");
		if (!modelPath.endsWith("/")) {
			modelPath += "/"; 
		}
		
		final String pathname = basePath + modelPath + "texture/" + textureName;
		final File textureFile = new File(pathname);
		
		if (textureFile.isFile() && textureFile.canRead()) {
			try {
				return Files.toByteArray(textureFile);
			} catch (IOException e) {
				LOGGER.error("Problem reading material file. Caused by: ", e);
			}
		} else {
			LOGGER.error("Could not read material file: " + pathname);
		}
		return null;
	}
}