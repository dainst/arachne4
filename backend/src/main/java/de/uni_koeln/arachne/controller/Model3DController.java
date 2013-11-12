package de.uni_koeln.arachne.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.util.EntityId;

@Controller
public class Model3DController implements ServletContextAware{
	private static final Logger LOGGER = LoggerFactory.getLogger(Model3DController.class);
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService;
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	@Autowired
	private transient SingleEntityDataService singleEntityDataService;
	
	private transient ServletContext servletContext;
	
	@Override
	public void setServletContext(final ServletContext servletContext) {
		this.servletContext = servletContext;		
	}
	
	@RequestMapping(value = "/model/{entityId}", method = RequestMethod.GET)
	public @ResponseBody String handleModelRequest(@PathVariable("entityId") final Long entityId
			, @RequestParam(value = "meta", required = false) final Boolean isMeta
			, final HttpServletRequest request, final HttpServletResponse response) {
		
		final Dataset dataset = getDataset(entityId, response);
    	
		if (dataset == null) {
			return null;
		}
		
    	if (isMeta != null && isMeta) {
			return getMetaData(dataset);
		} else {
			return getModelData(dataset);
		}
	}
	
	@RequestMapping(value = "/model/material/{entityId}", method = RequestMethod.GET)
	public @ResponseBody String handleModelRequest(@PathVariable("entityId") final Long entityId
			, final HttpServletRequest request, final HttpServletResponse response) {
		
		final Dataset dataset = getDataset(entityId, response);
    	
		if (dataset == null) {
			return null;
		}
		
		return getMaterialData(dataset);
	}
	
	// use regexp workaround for spring truncating at dots in parameters
	@RequestMapping(value = "/model/texture/{textureName:.+}", method = RequestMethod.GET)
	public @ResponseBody Object handleModelRequest(@PathVariable("textureName") final String textureName
			, final HttpServletRequest request, final HttpServletResponse response) {
		LOGGER.debug("Request for Texture: " + textureName);
		final ServletContextResource texture = new ServletContextResource(servletContext, "/WEB-INF/vt2.bmp");	
		return texture;
	}

	private Dataset getDataset(final long entityId, final HttpServletResponse response) {
		final EntityId arachneId = entityIdentificationService.getId(entityId);
    	
    	if (arachneId == null || arachneId.isDeleted() || !arachneId.getTableName().equals("modell3d")) {
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
	
	private String getMetaData(final Dataset dataset) {
		final JSONObject result = new JSONObject();
		try {
			result.put("title", dataset.getFieldFromFields("modell3d.Titel"));
			result.put("format", dataset.getFieldFromFields("modell3d.Dateiformat"));
			result.put("license", dataset.getFieldFromFields("modell3d.Lizenz"));
			return result.toString();
		} catch (JSONException e) {
			LOGGER.error("Failed to generate model meta data. Cause: ", e);
		}
		return null;
	}

	private String getModelData(final Dataset dataset) {
		// TODO read model data from disk ('modell3d.Pfad')
		final ServletContextResource modelData = new ServletContextResource(servletContext, "/WEB-INF/" 
					+ dataset.getFieldFromFields("modell3d.Dateiname"));
		
		if (modelData.exists()) {
			try {
				final File file = modelData.getFile();
				return Files.toString(file, Charsets.UTF_8);
			} catch (IOException e) {
				LOGGER.error("Problem reading model file. Cause: ", e);
			}
		}
		return null;
	}
	
	private String getMaterialData(final Dataset dataset) {
		// TODO read material data from disk ('modell3d.Pfad')
		final ServletContextResource materialData = new ServletContextResource(servletContext, "/WEB-INF/" 
				+ dataset.getFieldFromFields("modell3d.DateinameMTL"));
		 
		if (materialData.exists()) {
			try {
				final File file = materialData.getFile();
				return Files.toString(file, Charsets.UTF_8);
			} catch (IOException e) {
				LOGGER.error("Problem reading model file. Caused by: ", e);
			}
		}
		return null;
	}
}
