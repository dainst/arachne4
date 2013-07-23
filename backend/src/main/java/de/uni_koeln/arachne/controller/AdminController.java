package de.uni_koeln.arachne.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit; 

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;

import de.uni_koeln.arachne.response.StatusResponse;
import de.uni_koeln.arachne.service.DataImportService;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * Handles http requests for <code>/admin<code>.
 * This includes requests for statuses (cache or dataimport) as well admin tasks (clearing the cache or starting a dataimport).
 */
@Controller
public class AdminController implements ServletContextAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
	
	private transient ServletContext servletContext;
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	@Autowired
	private transient DataImportService dataImportService;

	@Autowired
	private transient TaskExecutor defaultTaskExecutor;
	
	private final transient String esProtocol;
	private final transient String esAddress;
	private final transient String esPort;
	private final transient String esName;
	
	@Autowired
	public AdminController(final @Value("#{config.esProtocol}") String esProtocol,
						   final @Value("#{config.esAddress}") String esAddress,
						   final @Value("#{config.esPort}") String esPort,
						   final @Value("#{config.esName}") String esName) {
		this.esProtocol = esProtocol;
		this.esAddress = esAddress;
		this.esPort = esPort;
		this.esName = esName;
	}
	
	public void setServletContext(final ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	/**
	 * Handles HTTP GET requests to /admin/cache.   
	 * @param response The outgoing HTTP response.
	 * @return A <code>StatusResponse</code> containing the status of the XML configuration document cache or <code>null</code> on error-
	 */
	@RequestMapping(value="/admin/cache", method=RequestMethod.GET)
	public @ResponseBody StatusResponse getCacheStatus(final HttpServletResponse response) {
		
		LOGGER.debug("User GroupID: " + userRightsService.getCurrentUser().getGroupID());
		if (userRightsService.getCurrentUser().getGroupID() >= UserRightsService.MIN_ADMIN_ID) {
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
		response.setStatus(403);
		return null;
	}
		
	/**
	 * Handles HTTP DELETE requests to /admin/cache.
	 * Deletes the cache.  
	 * @param response The outgoing HTTP response.
	 * @return A <code>StatusResponse</code> containing the status of the XML configuration document cache or <code>null<code> on error.
	 */
	@RequestMapping(value="/admin/cache", method=RequestMethod.DELETE)
	public @ResponseBody StatusResponse handleCache(final HttpServletResponse response) {
				
		LOGGER.debug("User GroupID: " + userRightsService.getCurrentUser().getGroupID());
		if (userRightsService.getCurrentUser().getGroupID() >= UserRightsService.MIN_ADMIN_ID) {
			xmlConfigUtil.clearCache();
			return new StatusResponse("Cache cleared.");
		}
		response.setStatus(403);
		return null;
	}

	/**
	 * Handles HTTP GET requests to /admin/dataimport.
	 * Returns the current status of the Elasticsearch data import.
	 * @return A <code>StatusResponse</code> object.
	 */
	@RequestMapping(value="/admin/dataimport", method=RequestMethod.GET)
	public @ResponseBody StatusResponse getDataImportStatus() {
		
		if (dataImportService.isRunning()) {
			final long elapsedTime = dataImportService.getElapsedTime();
			return new StatusResponse("Dataimport status: running - Elapsed Time: " + String.format("%d:%02d", 
					TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
					TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - 
					TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime))) + " minutes - " 
					+ "Indexed Documents: " + dataImportService.getIndexedDocuments());
		} else {
			return new StatusResponse("Dataimport status: idle");
		}
	}
	
	/**
	 * Handles HTTP POST requests to start or stop the Elasticsearch dataimport.
	 * For this it utilizes the <code>dataimportService</code> where the real work is done. 
	 * @param command The supported commands are "start" and "stop".
	 * @param response The outgoing HTTP response.
	 * @return A <code>StatusResponse</code> containing the current dataimport status or <code>null</code> on error.
	 */
	@RequestMapping(value="/admin/dataimport", method=RequestMethod.POST)
	public @ResponseBody StatusResponse handleDataImport(@RequestParam(value = "command", required = true) final String command
			, final HttpServletResponse response) {
		
		if (StrUtils.isEmptyOrNull(command)) {
			response.setStatus(400);
			return null;
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
						return new StatusResponse("Dataimport status: aborting");
					} else {
						return new StatusResponse("Dataimport status: not running");
					}
				}
			}
			return new StatusResponse("Unsupported command.");
		}
	}
	
	/**
	 * Puts a new elasticsearch mapping onto the index specified in the 'application.properties' file. 
	 * @return A <code>StatusResponse</code> containing the result of the operation. 
	 */
	@RequestMapping(value="/admin/mapping", method=RequestMethod.PUT)
	public @ResponseBody StatusResponse setElasticSearchMapping() {
		HttpURLConnection connection = null;
		String message = "An error occured while trying to set the elastic search mapping.";
		
		String mapping = getMappingFromFile();
		
		if ("undefined".equals(mapping)) {
			return new StatusResponse("Failed to set mapping.");
		}
        
		try {
			LOGGER.debug("Elasticsearch: " + esProtocol + "://" + esAddress + ':' + esPort + '/' + esName + "/entity/_mapping");
			final URL serverAdress = new URL(esProtocol + "://" + esAddress + ':' + esPort + '/' + esName + "/entity/_mapping");
			connection = (HttpURLConnection)serverAdress.openConnection();			
			connection.setRequestMethod("PUT");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
	        connection.setRequestProperty("Accept", "application/json");
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(mapping);
			writer.flush();
			writer.close();

			if (connection.getResponseCode() == 200) {
				message = "Elasticsearch mapping set.";
				LOGGER.info("Elasticsearch mapping set.");
			} else {
				LOGGER.error("Failed to set mapping. Elasticsearch HTTP request returned status code: " + connection.getResponseCode());
			}
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage());
		} catch (ProtocolException e) {
			LOGGER.error(e.getMessage());
		} catch (SocketTimeoutException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			connection.disconnect();
			connection = null;
		}
		return new StatusResponse(message);
	}
	
	/**
	 * Reads the elastic search mapping from "/WEB-INF/search/mapping.json".   
	 * @return The JSON mapping as <code>String</code>.
	 */
	private String getMappingFromFile() {
		String filename = "/WEB-INF/search/mapping.json";
		StringBuilder mapping = new StringBuilder(64);
		InputStream inputStream = null;
		try {
			inputStream = servletContext.getResourceAsStream(filename);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			while ((inputLine = bufferedReader.readLine()) != null) {
				mapping.append(inputLine);
				LOGGER.debug(inputLine);
			}
		} catch (IOException e) {
			LOGGER.error("Could not read '" + filename + "'. " + e.getMessage());
			mapping = new StringBuilder("undefined");
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					LOGGER.error("Could not close '" + filename + "'. " + e.getMessage());
					mapping = new StringBuilder("undefined");
				}
			}
		}
		return mapping.toString();
	}
}