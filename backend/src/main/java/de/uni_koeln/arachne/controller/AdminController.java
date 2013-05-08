package de.uni_koeln.arachne.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.StatusResponse;
import de.uni_koeln.arachne.service.DataImportService;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * Handles http requests (currently only get) for <code>/admin<code>.
 * This includes requests for statuses (cache or dataimport) as well admin tasks (clearing the cache or starting a dataimport).
 */
@Controller
public class AdminController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	@Autowired
	private transient DataImportService dataImportService;

	@Autowired
	private transient TaskExecutor defaultTaskExecutor;
	
	/**
	 * Handles http requests for /admin
	 * This mapping should only be used by Solr for indexing. It wraps the standard entity request but disables authorization.
	 * Requests are only allowed from the same IP-address as the Solr server configured in <code>src/main/resources/config/application.properties</code>. 
	 */
	@RequestMapping(value="/admin", method=RequestMethod.GET)
	public @ResponseBody StatusResponse handleAdminRequest(final HttpServletResponse response,
			@RequestParam(value = "command", required = false) final String command) {
		
		LOGGER.debug("User GroupID: " + userRightsService.getCurrentUser().getGroupID());
		if (userRightsService.getCurrentUser().getGroupID() >= UserRightsService.MIN_ADMIN_ID) {
			if (StrUtils.isEmptyOrNull(command)) {
				return getCache();
			} else {
				if ("clear-cache".equals(command)) {
					xmlConfigUtil.clearCache();
					return new StatusResponse("Cache cleared.");
				}
				return new StatusResponse("Unsupported command.");
			}
		}
		response.setStatus(403);
		return null;
	}

	/**
	 * Elastic search data import.
	 * @param command
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/admin/dataimport", method=RequestMethod.GET)
	public @ResponseBody StatusResponse handleDataImport(@RequestParam(value = "command", required = false) final String command
			, final HttpServletRequest request, final HttpServletResponse response) {
		
		if (StrUtils.isEmptyOrNull(command)) {
			if (dataImportService.isRunning()) {
				return new StatusResponse("Dataimport status: running - Elapsed Time: " + String.format("%.2f"
						, dataImportService.getElapsedTime()/1000f/60f) + " minutes - " 
						+ "Indexed Documents: " + dataImportService.getIndexedDocuments());
			} else {
				return new StatusResponse("Dataimport status: idle");
			}
			
		} else {
			if ("start".equals(command)) {
				if (dataImportService.isRunning()) {
					return new StatusResponse("Dataimport status: already running");
				} else {
					defaultTaskExecutor.execute(dataImportService);				
					return new StatusResponse("Dataimport status: started");
				}
			} else {
				if ("stop".equals(command)) {
					if (dataImportService.isRunning()) {
						dataImportService.stop();
						return new StatusResponse("Dataimport status: stopped");
					} else {
						return new StatusResponse("Dataimport status: not running");
					}
				}
			}
			return new StatusResponse("Unsupported command.");
		}
	}
	
	/**
	 * Returns a list of cached xml config documents and include elements wrapped in a <code>StatusResponse</code>.
	 * @return A <code>StatusResponse</code>.
	 */
	private StatusResponse getCache() {
		final StringBuilder result = new StringBuilder("Cached documents:");
		final List<String> cachedDocuments = xmlConfigUtil.getXMLConfigDocumentList();
		if (cachedDocuments.isEmpty()) {
			result.append(" none");
		} else {
			for (String document: cachedDocuments) {
				result.append(" " + document + ".xml");
			}
		}
		
		result.append(" - Cached include elements:");
		final List<String> cachedElements = xmlConfigUtil.getXMLIncludeElementList();
		if (cachedElements.isEmpty()) {
			result.append(" none");
		} else {
			for (String element: cachedElements) {
				result.append(" " + element + "_inc.xml");
			}
		}
		
		return new StatusResponse(result.toString());
	}
}
