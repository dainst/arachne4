package de.uni_koeln.arachne.controller;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8_VALUE;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit; 

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.StatusResponse;
import de.uni_koeln.arachne.service.DataImportService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * Handles http requests for <code>/admin<code>.
 * This includes requests for statuses (cache or dataimport) as well admin tasks (clearing the cache or starting a dataimport).
 */
@Controller
public class AdminController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
		
	@Autowired
	private transient UserRightsService userRightsService;
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	@Autowired
	private transient DataImportService dataImportService;

	/**
	 * Handles HTTP GET requests to /admin/cache.   
	 * @param response The outgoing HTTP response.
	 * @return A <code>StatusResponse</code> containing the status of the XML configuration document cache or <code>null</code> on error-
	 */
	@RequestMapping(value = "/admin/cache", method = RequestMethod.GET
			, produces = {APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody StatusResponse getCacheStatus(final HttpServletResponse response) {
		
		LOGGER.debug("User GroupID: " + userRightsService.getCurrentUser().getGroupID());
		if (userRightsService.getCurrentUser().getGroupID() >= UserRightsService.MIN_ADMIN_ID) {
			final StatusResponse result = new StatusResponse();
			final List<String> cachedDocuments = xmlConfigUtil.getXMLConfigDocumentList();
			if (cachedDocuments.isEmpty()) {
				result.setCachedDocuments(Arrays.asList("none"));
			} else {
				result.setCachedDocuments(cachedDocuments);
			}

			final List<String> cachedElements = xmlConfigUtil.getXMLIncludeElementList();
			if (cachedElements.isEmpty()) {
				result.setCachedDocuments(Arrays.asList("none"));
			} else {
				result.setCachedIncludeElements(cachedElements);
			}

			return result;
		}
		response.setStatus(403);
		return null;
	}
		
	/**
	 * Handles HTTP DELETE requests to /admin/cache.
	 * Deletes the cache.  
	 * @param response The outgoing HTTP response.
	 * @return A <code>StatusResponse</code> containing the status of the XML configuration document cache or <code>null<code> on error.
	 */
	@RequestMapping(value="/admin/cache", method=RequestMethod.DELETE
			, produces = {APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody StatusResponse handleCache(final HttpServletResponse response) {
				
		LOGGER.debug("User GroupID: " + userRightsService.getCurrentUser().getGroupID());
		if (userRightsService.getCurrentUser().getGroupID() >= UserRightsService.MIN_ADMIN_ID) {
			xmlConfigUtil.clearCache();
			return new StatusResponse("Cache", "cleared");
		}
		response.setStatus(403);
		return null;
	}

	/**
	 * Handles HTTP GET requests to /admin/dataimport.
	 * Returns the current status of the Elasticsearch data import.
	 * @return A <code>StatusResponse</code> object.
	 */
	@RequestMapping(value="/admin/dataimport", method=RequestMethod.GET
			, produces = {APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody StatusResponse getDataImportStatus() {
		
		if (dataImportService.isRunning()) {
			final long elapsedTime = dataImportService.getElapsedTime();
			final StatusResponse response = new StatusResponse("Dataimport", "running");
			response.setElapsedTime(String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
					,TimeUnit.MILLISECONDS.toSeconds(elapsedTime) 
					- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime))) + " minutes");
			
			response.setCount(dataImportService.getCount());
			response.setIndexedDocuments(dataImportService.getIndexedDocuments());
			final long etr = dataImportService.getEstimatedTimeRemaining();
			response.setEstimatedTimeRemaining(String.format("%d:%02d", TimeUnit.SECONDS.toMinutes(etr)
					,TimeUnit.SECONDS.toSeconds(etr) 
					- TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(etr))) + " minutes");
			response.setDocumentsPerSecond((float)dataImportService.getAverageDPS());
			return response;
		} else {
			return new StatusResponse("Dataimport", "idle");
		}
	}
	
	/**
	 * Handles HTTP POST requests to start or stop the Elasticsearch dataimport.
	 * For this it utilizes the <code>dataimportService</code> where the real work is done. 
	 * @param command The supported commands are "start" and "stop".
	 * @param response The outgoing HTTP response.
	 * @return A <code>StatusResponse</code> containing the current dataimport status or <code>null</code> on error.
	 */
	@RequestMapping(value="/admin/dataimport", method=RequestMethod.POST
			, produces = {APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody StatusResponse handleDataImport(@RequestParam(value = "command", required = true) final String command
			, final HttpServletResponse response) {
		
		if (StrUtils.isEmptyOrNullOrZero(command)) {
			response.setStatus(400);
			return null;
		} else {
			if ("start".equals(command)) {
				if (dataImportService.isRunning()) {
					return new StatusResponse("Dataimport", "already running");
				} else {
					dataImportService.start();				
					return new StatusResponse("Dataimport", "started");
				}
			} else {
				if ("stop".equals(command)) {
					if (dataImportService.isRunning()) {
						dataImportService.stop();
						return new StatusResponse("Dataimport", "aborting");
					} else {
						return new StatusResponse("Dataimport", "not running");
					}
				}
			}
			return new StatusResponse("Dataimport", "unsupported command");
		}
	}
}
