package de.uni_koeln.arachne.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.util.EntityId;

@Service("EntityService")
public class EntityService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityService.class);
	
	private final boolean PROFILING;
	
	@Autowired
	private transient SingleEntityDataService singleEntityDataService;
	
	@Autowired
	private transient ContextService contextService;
	
	@Autowired
	private transient ImageService imageService;
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	@Autowired
	private transient ResponseFactory responseFactory;
	
	@Autowired
	public EntityService(final @Value("${profilingEntityRetrieval}") boolean profiling) {
		this.PROFILING = profiling;
	}
	
	/**
	 * This functions retrieves a <code>FromattedArachneEntity</code> as JSON <code>String</code>.
	 * @param entityId The corresponding EntityId object.
	 * @return The requested formatted entity as JSON <code>String</code> or "forbidden" to indicate that the user is 
	 * not allowed to see this entity.
	 */
	public String getFormattedEntityByIdAsJsonString(final EntityId entityId) {
		long startTime = 0;
		if (PROFILING) {
			startTime = System.currentTimeMillis();
		}
		
		final String datasetGroupName = singleEntityDataService.getDatasetGroup(entityId);
    	final DatasetGroup datasetGroup = new DatasetGroup(datasetGroupName);
    	
    	LOGGER.debug("Indexer(" + entityId.getArachneEntityID() + "): " + userRightsService.isDataimporter());
    	
    	if (!userRightsService.isDataimporter() && !userRightsService.userHasDatasetGroup(datasetGroup)) {
    		LOGGER.debug("Forbidden!");
    		return "forbidden";
    	}
    	
    	final Dataset arachneDataset = singleEntityDataService.getSingleEntityByArachneId(entityId);
    	
    	LOGGER.debug(arachneDataset.toString());
    	
    	String result = null;
    	if (PROFILING) {
    		final long fetchTime = System.currentTimeMillis() - startTime;
    		long nextTime = System.currentTimeMillis();

    		imageService.addImages(arachneDataset);

    		final long imageTime = System.currentTimeMillis() - nextTime;
    		nextTime = System.currentTimeMillis();

    		contextService.addMandatoryContexts(arachneDataset);

    		final long contextTime = System.currentTimeMillis() - nextTime;
    		nextTime = System.currentTimeMillis();

    		result = responseFactory.createFormattedArachneEntityAsJsonString(arachneDataset);

    		LOGGER.info("-- Fetching entity took " + fetchTime + " ms");
    		LOGGER.info("-- Adding images took " + imageTime + " ms");
    		LOGGER.info("-- Adding contexts took " + contextTime + " ms");
    		LOGGER.info("-- Creating response took " + (System.currentTimeMillis() - nextTime) + " ms");
    	} else {
    		imageService.addImages(arachneDataset);

    		contextService.addMandatoryContexts(arachneDataset);

    		result = responseFactory.createFormattedArachneEntityAsJsonString(arachneDataset);
    	}
    	return result;
	}
	
	/**
	 * This functions retrieves a <code>FromattedArachneEntity</code> as JSON raw <code>byte</code> array.
	 * IMPORTANT: Do no use the raw byte representation for the live retrieval of entities. It is only meant to be used 
	 * by the dataimport. Use the <code>String</code> version instead.
	 * @param entityId The corresponding EntityId object.
	 * @return The requested formatted entity object as JSON raw <code>byte</code> array or <code>null</code> to 
	 * indicate that the user is not allowed to see this entity or any error occurs.
	 */
	public byte[] getFormattedEntityByIdAsJson(final EntityId entityId) {
		long startTime = 0;
		if (PROFILING) {
			startTime = System.currentTimeMillis();
		}
		
		final String datasetGroupName = singleEntityDataService.getDatasetGroup(entityId);
    	final DatasetGroup datasetGroup = new DatasetGroup(datasetGroupName);
    	
    	LOGGER.debug("Indexer(" + entityId.getArachneEntityID() + "): " + userRightsService.isDataimporter());
    	
    	if (!userRightsService.isDataimporter() && !userRightsService.userHasDatasetGroup(datasetGroup)) {
    		LOGGER.debug("Forbidden!");
    		return null;
    	}
    	
    	final Dataset arachneDataset = singleEntityDataService.getSingleEntityByArachneId(entityId);
    	
    	LOGGER.debug(arachneDataset.toString());
    	
    	byte[] result = null;
    	if (PROFILING) {
    		final long fetchTime = System.currentTimeMillis() - startTime;
    		long nextTime = System.currentTimeMillis();

    		imageService.addImages(arachneDataset);

    		final long imageTime = System.currentTimeMillis() - nextTime;
    		nextTime = System.currentTimeMillis();

    		contextService.addMandatoryContexts(arachneDataset);

    		final long contextTime = System.currentTimeMillis() - nextTime;
    		nextTime = System.currentTimeMillis();

    		result = responseFactory.createFormattedArachneEntityAsJson(arachneDataset);

    		LOGGER.info("-- Fetching entity took " + fetchTime + " ms");
    		LOGGER.info("-- Adding images took " + imageTime + " ms");
    		LOGGER.info("-- Adding contexts took " + contextTime + " ms");
    		LOGGER.info("-- Creating response took " + (System.currentTimeMillis() - nextTime) + " ms");
    	} else {
    		imageService.addImages(arachneDataset);

    		contextService.addMandatoryContexts(arachneDataset);

    		result = responseFactory.createFormattedArachneEntityAsJson(arachneDataset);
    	}
    	return result;
	}
}
