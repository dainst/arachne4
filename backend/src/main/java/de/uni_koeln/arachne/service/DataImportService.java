package de.uni_koeln.arachne.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.uni_koeln.arachne.response.BaseArachneEntity;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.util.EntityId;

@Service("DataImportService")
public class DataImportService implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataImportService.class);
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService;
	
	@Autowired
	private transient EntityService entityService;
	
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
	
	private AtomicLong elapsedTime;
	private AtomicBoolean running;
	private AtomicLong processedDocuments;
	
	private transient final String esName;
	private transient final int esBulkSize;
	
	@Autowired
	public DataImportService(final @Value("#{config.esName}") String esName, final @Value("#{config.esBulkSize}") int esBulkSize) {
		elapsedTime = new AtomicLong(0);
		running = new AtomicBoolean(false);
		processedDocuments = new AtomicLong(0);
		this.esName = esName;
		this.esBulkSize = esBulkSize;
	}

	public void run() {
		// enable request scope hack
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
		running.set(true);
		processedDocuments.set(0);		
		elapsedTime.set(0);
		final long startTime = System.currentTimeMillis();
		
		userRightsService.setUserSolr();
		LOGGER.info("Getting list of ArachneEntityIds.");
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
			LOGGER.info("Starting dataimport.");
			for (long currentEntityId: entityIds) {
				final EntityId entityId = entityIdentificationService.getId(currentEntityId);
				BaseArachneEntity entity;
				if (entityId.isDeleted()) {
					entity = responseFactory.createResponseForDeletedEntity(entityId);
				} else {
					entity = entityService.getFormattedEntityById(entityId);
				}
				
				if (entity == null) {
					LOGGER.error("Entity " + entityId + " is null! This should never happen. Check the database immediately.");
				} else {
					bulkRequest.add(client.prepareIndex(esName,entity.getType(),String.valueOf(entityId.getArachneEntityID()))
							.setSource(mapper.writeValueAsBytes(entity)));
					bulkDocumentCount++;
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
					elapsedTime.set(System.currentTimeMillis() - startTime);
					processedDocuments.set(documentCount);
				}
			}
			LOGGER.info("Sending last bulk of " + bulkDocumentCount + " documents.");
			if (bulkDocumentCount > 0) {
				bulkRequest.execute().actionGet();
				documentCount = documentCount + bulkDocumentCount;
				processedDocuments.set(documentCount);
			}
			node.close();
			LOGGER.info("Import of " + documentCount + " documents finished in " + ((System.currentTimeMillis() - start)/1000f/60f/60f) + " hours.");
		}
		catch (Exception e) {
			LOGGER.error("Dataimport failed with: " + e.toString());
		}
		// disable request scope hack
		((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).requestCompleted();
		RequestContextHolder.resetRequestAttributes();
		running.set(false);
	}
	
	public long getElapsedTime() {
		return elapsedTime.get();
	}
	
	public boolean isRunning() {
		return running.get();
	}
	
	public long getProcessedDocuments() {
		return processedDocuments.get();
	}
}
