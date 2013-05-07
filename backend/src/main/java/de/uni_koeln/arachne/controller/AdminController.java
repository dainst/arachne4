package de.uni_koeln.arachne.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.BaseArachneEntity;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.response.StatusResponse;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.EntityId;
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
	private transient EntityIdentificationService entityIdentificationService;
	
	@Autowired
	private transient EntityService entityService;
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	@Autowired
	private transient ResponseFactory responseFactory;
	
	private transient JdbcTemplate jdbcTemplate;
	
	protected transient DataSource dataSource;

	/**
	 * Through this function the datasource is injected
	 * @param dataSource An SQL Datasource
	 */
	@Autowired
	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;		
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
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
	 */
	@RequestMapping(value="/admin/dataimport", method=RequestMethod.GET)
	public void handleDataImport(final HttpServletRequest request, final HttpServletResponse response
			, final @Value("#{config.esName}") String esName, final @Value("#{config.esBulkSize}") int esBulkSize) {
		
		LOGGER.info("Starting dataimport.");
		userRightsService.setUserSolr();
		final List<Long> entityIds = jdbcTemplate.query("select `ArachneEntityID` from `arachneentityidentification`", new RowMapper<Long>() {
			public Long mapRow(final ResultSet resultSet, final int index) throws SQLException {
				return resultSet.getLong(1);
			}
		});
				
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final Node node = NodeBuilder.nodeBuilder().client(true).clusterName(esName).node();
			final Client client = node.client();

			long documentCount = 0;
			long bulkDocumentCount = 0;
			BulkRequestBuilder bulkRequest = client.prepareBulk();
			long now = System.currentTimeMillis();
			final long start = now;
			LOGGER.info("Starting real dataimport.");
			for (long currentEntityId: entityIds) {
				//long entityTime = System.currentTimeMillis();
				final EntityId entityId = entityIdentificationService.getId(currentEntityId);
				BaseArachneEntity entity;
				if (entityId.isDeleted()) {
		    		entity = responseFactory.createResponseForDeletedEntity(entityId);
		    	} else {
		    		entity = entityService.getFormattedEntityById(entityId);
		    	}
				//LOGGER.info("GetEntity " + entityId + ": " + (System.currentTimeMillis() - entityTime) + " ms");
				
				if (entity != null) {
					bulkRequest.add(client.prepareIndex(esName,entity.getType(),String.valueOf(entityId)).setSource(mapper.writeValueAsBytes(entity)));
					bulkDocumentCount++;
				} else {
					LOGGER.warn("Entity " + entityId + " is null!");
				}
				
				if (bulkDocumentCount >= esBulkSize) {
					documentCount = documentCount + bulkDocumentCount;
					LOGGER.info("SQL query time(" + documentCount + "): " + ((System.currentTimeMillis() - now)/1000f) + " s");
					now = System.currentTimeMillis();
					bulkRequest.execute().actionGet();
					LOGGER.info("ES bulk execute time(" + documentCount + "): " + ((System.currentTimeMillis() - now)/1000f) + " s");
					now = System.currentTimeMillis();
					bulkRequest = client.prepareBulk();
					bulkDocumentCount = 0;
				}
			}
			// send last bulk
			LOGGER.info("Sending last bulk of " + bulkDocumentCount + " documents.");
			if (bulkDocumentCount > 0) {
				bulkRequest.execute().actionGet();
				documentCount = documentCount + bulkDocumentCount;
			}
			node.close();
			LOGGER.info("Import of " + documentCount + " documents finished in " + ((System.currentTimeMillis() - start)/1000f/60f/60f) + " hours.");
			response.setStatus(200);
		}
		catch (Exception e) {
			LOGGER.error("Message: " + e.getMessage());
			response.setStatus(500);
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
