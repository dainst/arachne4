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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.uni_koeln.arachne.mapping.ArachneEntity;
import de.uni_koeln.arachne.response.BaseArachneEntity;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.util.ESClientUtil;
import de.uni_koeln.arachne.util.EntityId;

/**
 * This class implements the dataimport into elastic search. It is realized as a <code>@Service</code> so it can make use of autowiring and
 * be autowired itself (for communication). At the same time it implements <code>Runnable</code> so that the dataimport can run asynchronously
 * via a <code>TaskExecutor</code>.  
 */
@Service("DataImportService")
public class DataImportService implements Runnable { // NOPMD - Threading is used via Springs TaskExecutor so it is save 
	private static final Logger LOGGER = LoggerFactory.getLogger(DataImportService.class);
	
	@Autowired
	private transient ESClientUtil esClientUtil;
	
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
	
	private transient final AtomicLong elapsedTime;
	private transient final AtomicBoolean running;
	private transient final AtomicLong indexedDocuments;
	
	private transient final ObjectMapper mapper;
		
	private transient boolean terminate = false;
	
	public DataImportService() {
		elapsedTime = new AtomicLong(0);
		running = new AtomicBoolean(false);
		indexedDocuments = new AtomicLong(0);
		mapper = new ObjectMapper();
	}

	/**
	 * The dataimport implementation. This method retrieves a list of EntityIds from the DB and iterates over this list 
	 * constructing the associated documents and indexing them via elastic search.
	 */
	public void run() { // NOPMD - Threading is used via Springs TaskExecutor so it is save 
		class LongMapper implements RowMapper<Long> {
			public Long mapRow(final ResultSet resultSet, final int index) throws SQLException {
				return resultSet.getLong(1);
			}
		}
		
		final LongMapper longMapper = new LongMapper();
		
		// request scope hack (enabling session scope) - needed so the UserRightsService can be used
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
		terminate = false;
		running.set(true);
		indexedDocuments.set(0);		
		elapsedTime.set(0);
		final long startTime = System.currentTimeMillis();
		
		userRightsService.setUserSolr();
		
		elapsedTime.set(System.currentTimeMillis() - startTime);		
		try {
			LOGGER.info("Dataimport started.");
			
			final Client client = esClientUtil.getClient();
			final int esBulkSize = esClientUtil.getBulkSize();
			final String esName = esClientUtil.getName();
			
			boolean finished = false;
			long deltaT = 0;
			BulkRequestBuilder bulkRequest = client.prepareBulk();
			int index = 0;
			long startId = 0;
			indexing:
			while (!finished) {
				final List<Long> entityIds = jdbcTemplate.query("select `ArachneEntityID` from `arachneentityidentification` WHERE `ArachneEntityID` > "
						+ startId + " ORDER BY `ArachneEntityID` LIMIT " + esBulkSize, longMapper);
								
				long end = esBulkSize - 1;
				if (end >= entityIds.size()) {
					end = entityIds.size() - 1;
					finished = true;
				}
				
				startId = entityIds.get(0);
				final long endId = entityIds.get((int)end);
								
				final List<ArachneEntity> entityList = entityIdentificationService.getByEntityIdRange(startId, endId);
				startId = endId;
				for (ArachneEntity currentEntityId: entityList) {
					if (terminate) {
						running.set(false);
						break indexing;
					}
					final long fetch = System.currentTimeMillis();
					final EntityId entityId = new EntityId(currentEntityId.getTableName(), currentEntityId.getForeignKey()
							, currentEntityId.getId(), currentEntityId.isDeleted());
					
					BaseArachneEntity entity;
					if (entityId.isDeleted()) {
						entity = responseFactory.createResponseForDeletedEntity(entityId);
					} else {
						entity = entityService.getFormattedEntityById(entityId);
					}
										
					if (entity == null) {
						LOGGER.error("Entity " + entityId + " is null! This should never happen. Check the database immediately.");
					} else {
						bulkRequest.add(client.prepareIndex(esName,"entity",String.valueOf(entityId.getArachneEntityID()))
								.setSource(mapper.writeValueAsBytes(entity)));
					}
					final long fetchtime = System.currentTimeMillis() - fetch;
					if (fetchtime > 100) {
						LOGGER.debug("Indexing: fetching " + entityId.getArachneEntityID() + " took " + fetchtime + "ms");
					}
					// update elapsed time every second
					final long now = System.currentTimeMillis();
					if (now - deltaT > 1000) {
						deltaT = now;
						elapsedTime.set(now - startTime);
					}
				}
				bulkRequest.execute().actionGet();
				bulkRequest = client.prepareBulk();
				if (finished) {
					index += end;
				} else {
					index += esBulkSize;
				}
				indexedDocuments.set(index);
			}
			if (running.get()) {
				LOGGER.info("Import of " + index + " documents finished in " + ((System.currentTimeMillis() - startTime)/1000f/60f/60f) + " hours.");
			} else {
				LOGGER.info("Dataimport aborted.");
			}
		}
		catch (Exception e) {
			LOGGER.error("Dataimport failed with: " + e.toString());
		}
		// disable request scope hack
		((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).requestCompleted();
		RequestContextHolder.resetRequestAttributes();
		running.set(false);
	}
	
	/**
	 * Method to signal that the task shall stop.
	 */
	public void stop() {
		terminate = true;
	}
	
	public long getElapsedTime() {
		return elapsedTime.get();
	}
	
	public boolean isRunning() {
		return running.get();
	}
	
	public long getIndexedDocuments() {
		return indexedDocuments.get();
	}
}