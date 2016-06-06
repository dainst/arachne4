package de.uni_koeln.arachne.service;

import java.util.List;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.base.Throwables;

import de.uni_koeln.arachne.mapping.hibernate.ArachneEntity;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.network.BasicNetwork;
//import de.uni_koeln.arachne.util.system.ExternalProcess;

/**
 * This class implements the dataimport into elastic search. It is realized as a <code>@Service</code> so it can make 
 * use of autowiring and be autowired itself (for communication). At the same time it implements the 
 * <code>ApplicationListener</code>-Interface to receive the <code>ContextClosed</code> so that the <code>TaskExecutor</code> 
 * and <code>TaskScheduler</code> which are use to run the <code>startImport()</code> method asynchronously can be shut 
 *   
 */
@Service("DataImportService")
public class DataImportService { // NOPMD
	private static final Logger LOGGER = LoggerFactory.getLogger(DataImportService.class);
	
	private static final Logger LOGGER_PROF = LoggerFactory.getLogger("Profiling");
	
	private final int ID_LIMIT;
	
	private final boolean PROFILING;
	
	private final boolean checkIndexOnDataImport;
	
	@Autowired
	private transient ESService esService;
	
	@Autowired
	private transient UserRightsService userRightsService;
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService;
	
	@Autowired
	private transient EntityService entityService;
	
	@Autowired
	private transient MailService mailService;
	
	@Autowired
	private transient ContextService contextService;
	
	@Autowired
	private transient EntityCompareService entityCompareService;
	
	@Autowired
	private transient DataIntegrityLogService dataIntegrityLogService;
	
	private transient JdbcTemplate jdbcTemplate;
	
	/**
	 * Through this function the datasource is injected.
	 * @param dataSource An SQL Datasource.
	 */
	@Autowired
	public void setDataSource(final DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private transient long elapsedTime = 0;
	private transient boolean running = false;
	private transient long indexedDocuments = 0;
	private transient long count = 0;
	
	private transient boolean terminate = false;
	private transient long etr = 0;
	// choose some conservative value
	private transient double lastDPS = 100;
	private transient double averageDPS = 100;
	private transient double smoothingFactor = 0.005d;
	
	@Autowired
	public DataImportService(final @Value("${profilingDataimport}") boolean profiling
			, final @Value("${checkIndexOnDataImport}") boolean checkIndexOnDataImport
			, final @Value("${esBulkActions}") int esBulkActions) {
		
		this.PROFILING = profiling;
		this.checkIndexOnDataImport = checkIndexOnDataImport;
		this.ID_LIMIT = esBulkActions;
	}

	/**
	 * The dataimport implementation. This method retrieves a list of EntityIds from the DB and iterates over this list 
	 * constructing the associated documents and indexing them via elasticsearch.
	 */
	@Async
	public void start() { 
		// request scope hack (enabling session scope) - needed so the UserRightsService can be used
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
		
		// run scripts
		//createSemanticConnectionTable();
		
		terminate = false;
		running = true;
		indexedDocuments = 0;		
		elapsedTime = 0;
		long dbgEntityId = 0;
		
		userRightsService.setDataimporter();
		
		final String indexName = esService.getDataImportIndex();
		try {
			long deltaT = 0;
			int index = 0;
						
			final Client client = esService.getClient();
			
			class BulkProcessorListener implements BulkProcessor.Listener {
				// used to check if last bulk request has finished
				private int openRequests = 0;
				private boolean error = false;
				
				public int getOpenRequests() {
					return openRequests;
				}
				
				public boolean hasFailed() {
					return error;
				}
				
				@Override
			    public void beforeBulk(long executionId, BulkRequest request) {
					LOGGER.debug(String.format("ExecutionID %s: about to execute new bulk insert composed of %s actions."
							, executionId, request.numberOfActions()));
					openRequests++;
			    }

			    @Override
			    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
			        LOGGER.info(String.format("ExecutionID %s: bulk insert composed of %s actions completed in %s ms"
			        		, executionId, request.numberOfActions(), response.getTookInMillis()));
			        openRequests--;
			    }

			    @Override
			    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
			        LOGGER.error(String.format("Error executing bulk %s", executionId), failure);
			        openRequests--;
			        error = true;
			    }				
			}
			final BulkProcessorListener listener = new BulkProcessorListener();
			final BulkProcessor bulkProcessor = BulkProcessor.builder(client, listener)
					.setBulkActions(esService.getBulkActions())
					.setBulkSize(new ByteSizeValue(esService.getBulkSize(), ByteSizeUnit.MB))
					.build();
						
			if ("NoIndex".equals(indexName)) {
				LOGGER.error("Dataimport failed. No index found.");
				running = false;
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			
			LOGGER.info("Dataimport started on index '" + indexName + "'");
			esService.setRefreshInterval(indexName, false);
									
			final Long entityCount = jdbcTemplate.queryForObject("select count(*) `ArachneEntityID` from `arachneentityidentification` where `isDeleted` = 0", Long.class);
			if (entityCount != null) {
				count = entityCount;
			} else {
				LOGGER.error("'select count(*) `ArachneEntityID` from `arachneentityidentification` where `isDeleted` = 0' returned 0 - Dataimport aborted.");
				throw new Exception("'select count(*) `ArachneEntityID` from `arachneentityidentification` where `isDeleted` = 0' returned 0");
			}
			
			long startId = 0; 
			long lastDocuments = 0;
			List<ArachneEntity> entityIds;
			dataimport:
			do {
				LOGGER.debug("Fetching " + ID_LIMIT + " EntityIds [" + startId + "] ...");
				entityIds = entityIdentificationService.getByLimitedEntityIdRange(startId, ID_LIMIT);
				
				LOGGER.debug("Starting FOR loop...");
				for (final ArachneEntity currentEntityId: entityIds) {
										
					startId = currentEntityId.getEntityId();
					
					if (terminate) {
						running = false;
						break dataimport;
					}
					
					LOGGER.debug("Get ID: " + currentEntityId.getEntityId());
					//final EntityId entityId = entityIdentificationService.getId(currentEntityId);
					final EntityId entityId = new EntityId(currentEntityId);
					dbgEntityId = entityId.getArachneEntityID();
					
					LOGGER.debug("Creating response");
					byte[] jsonEntity;
					if (!entityId.isDeleted()) {
						jsonEntity = entityService.getFormattedEntityByIdAsJson(entityId);
						if (jsonEntity == null) {
							LOGGER.error("Entity " + dbgEntityId + " is null! This should never happen. Check the database immediately.");
							throw new RuntimeException("Entity " + dbgEntityId + " is null!");
						} else {
							if (checkIndexOnDataImport) {
								entityCompareService.compareToIndex(dbgEntityId, jsonEntity.toString());
							}
							LOGGER.debug("Adding entity " + dbgEntityId + " to bulk.");
							bulkProcessor.add(client.prepareIndex(indexName, "entity", String.valueOf(dbgEntityId))
									.setSource(jsonEntity).request());
							index++;
							indexedDocuments = index;
						}
					}
					
					LOGGER.debug("Update elapsed time");
					// update elapsed time every second
					final long now = System.currentTimeMillis();
					final long lastStep = now - deltaT;
					if (lastStep > 1000) {
						deltaT = now;
						elapsedTime = now - startTime;
						
						lastDPS = (double)(index - lastDocuments) / lastStep * 1000d;
						lastDocuments = index;
						calculateAverageDPSAndETR();
					}
				}
			} while (!entityIds.isEmpty());
			bulkProcessor.close();
			if (running) {
				// wait a little bit to let the bulk request finish as bulkprocessoor.close() is non-blocking
				int retries = 0;
				while (listener.getOpenRequests() > 0 && !listener.hasFailed() && retries < 60) {
					Thread.sleep(1000);
					retries++;
				}
				if (retries > 59) {
					throw new RuntimeException("Bulk request did not finish in 1 minute.");
				}
				esService.setMaxResultWindow(indexName, index);
				esService.setRefreshInterval(indexName, true);
				esService.updateSearchIndex();
				final long elapsedTime = (System.currentTimeMillis() - startTime);
				final String success = "Import of " + index + " documents finished in " + elapsedTime/1000f/60f/60f + " hours ("
						+ index/((float)elapsedTime/1000) + " documents per second)." + System.lineSeparator()
						+ System.lineSeparator() + dataIntegrityLogService.getSummary();
				LOGGER.info(success);
				mailService.sendMail("arachne4-tec-devel@uni-koeln.de", "Dataimport(" + BasicNetwork.getHostName() + ") - success", success);
				contextService.clearCache();
			} else {
				LOGGER.info("Dataimport aborted.");
				esService.deleteIndex(indexName);
				mailService.sendMail("arachne4-tec-devel@uni-koeln.de", "Dataimport(" + BasicNetwork.getHostName() + ") - abort", "Dataimport was manually aborted.");
			}
		}
		// TODO: find out if it is possible to catch less generic exceptions here
		catch (Exception e) {
			final String failure = "Dataimport failed at [" + dbgEntityId + "] with: ";
			LOGGER.error(failure, e);
			final String stacktrace = Throwables.getStackTraceAsString(e);
			mailService.sendMail("arachne4-tec-devel@uni-koeln.de", "Dataimport(" + BasicNetwork.getHostName() + ") - failure"
					, failure + e.toString() + System.getProperty("line.separator") + "StackTrace: " + stacktrace);
			esService.deleteIndex(indexName);
		}
		// disable request scope hack
		((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).requestCompleted();
		RequestContextHolder.resetRequestAttributes();
		running = false;
	}
	
	// re-enable when appropriate
	/*private void createSemanticConnectionTable() {
		String relativePath = "/WEB-INF/scripts/FillEntityConnectionTable.php";
		String absolutePath = servletContext.getRealPath(relativePath);
		ExternalProcess.runBlocking(new String[] {"php", absolutePath});
	}*/	

	private float calculateAverageDPSAndETR() {
		if (running && elapsedTime > 0 && indexedDocuments > 0) {
			averageDPS = smoothingFactor * lastDPS + (1 - smoothingFactor) * averageDPS;
			etr = (long)((double)(count - indexedDocuments) / averageDPS);
			if (PROFILING) {
				LOGGER_PROF.info(indexedDocuments + " - " + averageDPS);
			}
			return (float)averageDPS;
		}
		return 0.0f;
	}
	
	/**
	 * Method to signal that the task shall stop.
	 */
	@PreDestroy
	public void stop() {
		LOGGER.info("Stopping dataimport...");
		terminate = true;
	}
	
	public long getElapsedTime() {
		return elapsedTime;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public long getIndexedDocuments() {
		return indexedDocuments;
	}
	
	public long getCount() {
		return count;
	}
	
	public double getAverageDPS() {
		return averageDPS;
	}

	public long getEstimatedTimeRemaining() {
		return etr;
	}
}