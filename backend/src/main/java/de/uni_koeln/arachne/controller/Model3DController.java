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

@Controller
public class Model3DController implements ServletContextAware{
	private static final Logger LOGGER = LoggerFactory.getLogger(Model3DController.class);
	
	private transient ServletContext servletContext;
	
	@Override
	public void setServletContext(final ServletContext servletContext) {
		this.servletContext = servletContext;		
	}
	
	@RequestMapping(value = "/model/{modelId}", method = RequestMethod.GET)
	public @ResponseBody String handleModelRequest(@PathVariable("modelId") final Long modelId
			, @RequestParam(value = "meta", required = false) final Boolean isMeta
			, final HttpServletRequest request, final HttpServletResponse response) {
		LOGGER.debug("Request for model: " + modelId + "(" + isMeta + ")");
				
		if (isMeta != null && isMeta) {
			return getMetaData(modelId);
		} else {
			return getModelData(modelId);
		}
	}
	
	@RequestMapping(value = "/material/{modelId}", method = RequestMethod.GET)
	public @ResponseBody String handleModelRequest(@PathVariable("modelId") final Long modelId
			, final HttpServletRequest request, final HttpServletResponse response) {
		LOGGER.debug("Request for material: " + modelId);
			
		return getMaterialData(modelId);
	}
	
	// use regexp workaround for spring truncating at dots in parameters
	@RequestMapping(value = "/model/texture/{textureName:.+}", method = RequestMethod.GET)
	public @ResponseBody Object handleModelRequest(@PathVariable("textureName") final String textureName
			, final HttpServletRequest request, final HttpServletResponse response) {
		LOGGER.debug("Request for Texture: " + textureName);
		final ServletContextResource texture = new ServletContextResource(servletContext, "/WEB-INF/vt2.bmp");	
		return texture;
	}

	private String getMetaData(final long modelId) {
		final JSONObject result = new JSONObject();
		if (modelId == 666) {
			try {
				result.put("title", "Griechische Vase (TestModel)");
				result.put("textured", true);
				result.put("license", "Public Domain");
				return result.toString();
			} catch (JSONException e) {
				// TODO: handle exception
				LOGGER.error("Failed to generate model meta data. Cause: ", e);
			}
		} else {
			try {
				result.put("title", "Baukomplex der Basilika Aemilia");
				result.put("textured", false);
				result.put("license", "CC-BY-SA 3.0");
				result.put("connectedEntity", 5640);
				return result.toString();
			} catch (JSONException e) {
				// TODO: handle exception
				LOGGER.error("Failed to generate model meta data. Cause: ", e);
			}
		}
		return null;
	}

	private String getModelData(final long modelId) {
		ServletContextResource modelData = null;
		if (modelId == 666) {
			modelData = new ServletContextResource(servletContext, "/WEB-INF/vase.obj");
		} else {
			modelData = new ServletContextResource(servletContext, "/WEB-INF/basilika.obj");
		}
		if (modelData.exists()) {
			try {
				final File file = modelData.getFile();
				final String content = Files.toString(file, Charsets.UTF_8);
				return content;
			} catch (IOException e) {
				LOGGER.error("Problem reading model file. Caused by: ", e);
			}
		}
		return null;
	}
	
	private String getMaterialData(final long modelId) {
		ServletContextResource materialData = null;
		if (modelId == 666) {
			materialData = new ServletContextResource(servletContext, "/WEB-INF/vase.mtl");
		} 
		if (materialData.exists()) {
			try {
				final File file = materialData.getFile();
				final String content = Files.toString(file, Charsets.UTF_8);
				return content;
			} catch (IOException e) {
				LOGGER.error("Problem reading model file. Caused by: ", e);
			}
		}
		return null;
	}
}
