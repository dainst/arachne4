package de.uni_koeln.arachne.controller;

import java.util.List;

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
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * Handles http requests (currently only get) for <code>/admin<code>.
 */
@Controller
public class AdminController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
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
				return getCachedDocuments();
			} else {
				if ("clear-cache".equals(command)) {
					xmlConfigUtil.clearDocumentCache();
					return new StatusResponse("Document cache cleared.");
				}
				return new StatusResponse("Unsupported command.");
			}
		}
		response.setStatus(403);
		return null;
	}

	/**
	 * Returns a list of cached xml config documents wrapped in a <code>StatusResponse</code>.
	 * @return A <code>StatusResponse</code>.
	 */
	private StatusResponse getCachedDocuments() {
		final StringBuilder result = new StringBuilder("Cached Documents:");
		final List<String> cachedDocuments = xmlConfigUtil.getXMLConfigDocumentList();
		if (cachedDocuments.isEmpty()) {
			result.append(" none");
		} else {
			for (String document: cachedDocuments) {
				result.append(" " + document + ".xml");
			}
		}
		return new StatusResponse(result.toString());
	}
}
