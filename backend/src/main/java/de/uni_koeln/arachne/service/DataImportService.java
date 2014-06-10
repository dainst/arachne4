package de.uni_koeln.arachne.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.util.ESClientUtil;
import de.uni_koeln.arachne.util.EntityId;

/**
 * This class implements the dataimport into elastic search. It is realized as a <code>@Service</code> so it can make 
 * use of autowiring and be autowired itself (for communication). At the same time it implements <code>Runnable</code> 
 * so that the dataimport can run asynchronously via a <code>TaskExecutor</code>.  
 */
@Service("DataImportService")
public class DataImportService implements Runnable { // NOPMD
	private static final Logger LOGGER = LoggerFactory.getLogger(DataImportService.class);
	
	private static final long ID_LIMIT = 10000;
	
	private final boolean PROFILING;
	
	@Autowired
	private transient ESClientUtil esClientUtil;
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService;
	
	@Autowired
	private transient EntityService entityService;
	
	@Autowired
	private transient MailService mailService;
	
	@Autowired
	private transient ContextService contextService;
	
	@Autowired
	private transient ResponseFactory responseFactory;
	
	private transient JdbcTemplate jdbcTemplate;
	
	protected transient DataSource dataSource;
	
	/**
	 * Through this function the datasource is injected.
	 * @param dataSource An SQL Datasource.
	 */
	@Autowired
	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;		
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private transient final AtomicLong elapsedTime;
	private transient final AtomicBoolean running;
	private transient final AtomicLong indexedDocuments;
	private transient final AtomicLong count;
	
	private transient final ObjectMapper mapper;
	
	private transient boolean terminate = false;
	
	@Autowired
	public DataImportService(final @Value("#{config.profilingDataimport}") boolean profiling) {
		elapsedTime = new AtomicLong(0);
		running = new AtomicBoolean(false);
		indexedDocuments = new AtomicLong(0);
		count = new AtomicLong(0);
		mapper = new ObjectMapper();
		this.PROFILING = profiling;
	}

	/**
	 * The dataimport implementation. This method retrieves a list of EntityIds from the DB and iterates over this list 
	 * constructing the associated documents and indexing them via elasticsearch.
	 */
	public void run() { // NOPMD
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
		long dbgEntityId = 0;
		
		userRightsService.setDataimporter();
		
		final String indexName = esClientUtil.getDataImportIndex();
		try {
			long deltaT = 0;
			int index = 0;
						
			final Client client = esClientUtil.getClient();
			
			final BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
				
				@Override
			    public void beforeBulk(long executionId, BulkRequest request) {
					LOGGER.debug("Going to execute new bulk composed of {} actions", request.numberOfActions());
			    }

			    @Override
			    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
			        LOGGER.debug("Executed bulk composed of {} actions", request.numberOfActions());
			    }

			    @Override
			    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
			        LOGGER.error("Error executing bulk", failure);
			    }
			})
			.setBulkActions(esClientUtil.getBulkSize())
			.build();
						
			if ("NoIndex".equals(indexName)) {
				LOGGER.error("Dataimport failed. No index found.");
				running.set(false);
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			
			LOGGER.info("Dataimport started on index '" + indexName + "'");
			esClientUtil.setRefreshInterval(indexName, false);
									
			final Long entityCount = jdbcTemplate.queryForObject("select count(*) `ArachneEntityID` from `arachneentityidentification`", Long.class);
			if (entityCount != null) {
				count.set(entityCount);
			} else {
				LOGGER.error("'select count(*) `ArachneEntityID` from `arachneentityidentification`' returned 0 - Dataimport aborted.");
				throw new Exception("'select count(*) `ArachneEntityID` from `arachneentityidentification`' returned 0");
			}
			
			long startID = 0; 
			List<Long> entityIds;
			dataimport:
			do {
				LOGGER.debug("Fetching " + ID_LIMIT + " EntityIds [" + startID + "] ...");
				entityIds = jdbcTemplate.query("select `ArachneEntityID` from `arachneentityidentification`"
						+ "WHERE `ArachneEntityID` > " + startID + " ORDER BY `ArachneEntityID` LIMIT " + ID_LIMIT, longMapper);

				for (final long currentEntityId: entityIds) {
					LOGGER.debug("Starting FOR loop...");
					
					startID = currentEntityId;
					
					if (terminate) {
						running.set(false);
						break dataimport;
					}
					LOGGER.debug("Get ID: " + currentEntityId);
					final EntityId entityId = entityIdentificationService.getId(currentEntityId);
					dbgEntityId = entityId.getArachneEntityID();
					LOGGER.debug("Creating response");
					String jsonEntity;
					if (entityId.isDeleted()) {
						jsonEntity = responseFactory.createResponseForDeletedEntityAsJson(entityId);
					} else {
						jsonEntity = entityService.getFormattedEntityByIdAsJson(entityId);
					}

					if (jsonEntity == null) {
						LOGGER.error("Entity " + entityId.getArachneEntityID() + " is null! This should never happen. Check the database immediately.");
						throw new Exception();
					} else {
						bulkProcessor.add(client.prepareIndex(indexName, "entity", String.valueOf(entityId.getArachneEntityID()))
								.setSource(jsonEntity).request());
						index++;
						indexedDocuments.set(index);
					}
					LOGGER.debug("Update elapsed time");
					// update elapsed time every second
					final long now = System.currentTimeMillis();
					if (now - deltaT > 1000) {
						deltaT = now;
						elapsedTime.set(now - startTime);
					}
				}
			} while (!entityIds.isEmpty());
			if (running.get()) {
				bulkProcessor.close();
				esClientUtil.setRefreshInterval(indexName, true);
				esClientUtil.updateSearchIndex();
				final String success = "Import of " + index + " documents finished in " + ((System.currentTimeMillis()
						- startTime)/1000f/60f/60f) + " hours."; 
				LOGGER.info(success);
				mailService.sendMail("arachne4-tec-devel@uni-koeln.de", "Dataimport(" + getHostName() + ") - success", success);
				contextService.clearCache();
			} else {
				LOGGER.info("Dataimport aborted.");
				esClientUtil.deleteIndex(indexName);
				mailService.sendMail("arachne4-tec-devel@uni-koeln.de", "Dataimport(" + getHostName() + ") - abort", "Dataimport was manually aborted.");
			}
		}
		// TODO: find out if it is possible to catch less generic exceptions here
		catch (Exception e) {
			final String failure = "Dataimport failed at [" + dbgEntityId + "] with: ";
			LOGGER.error(failure, e);
			final String stacktrace = Throwables.getStackTraceAsString(e);
			mailService.sendMail("arachne4-tec-devel@uni-koeln.de", "Dataimport(" + getHostName() + ") - failure"
					, failure + e.toString() + System.getProperty("line.separator") + "StackTrace: " + stacktrace);
			esClientUtil.deleteIndex(indexName);
		}
		// disable request scope hack
		((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).requestCompleted();
		RequestContextHolder.resetRequestAttributes();
		running.set(false);
	}
	
	/**
	 * Determines the host name as <code>String</code>.
	 * @return The host name of the system or "UnknownHost" in case of failure.
	 */
	private String getHostName() {
		String result = "UnknownHost";
		try {
			final InetAddress localHost = InetAddress.getLocalHost();
			result = localHost.getHostName();
		} catch (UnknownHostException e) {
			LOGGER.warn("Could not determine local host name.");
		}
		return result;
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
	
	public long getCount() {
		return count.get();
	}
}