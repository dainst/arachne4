package de.uni_koeln.arachne.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.FormattedArachneEntity;
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
	public EntityService(final @Value("#{config.profilingEntityRetrieval}") boolean profiling) {
		this.PROFILING = profiling;
	}
	
	/**
	 * This functions retrieves a <code>FromattedArachneEntity</code>.
	 * @param entityId The corresponding EntityId object.
	 * @return The requested formatted entity object or an empty <code>FromattedArachneEntity</code> object where the type 
	 * is set to "forbidden" to indicate that the user is not allowed to see this entity.
	 */
	public FormattedArachneEntity getFormattedEntityById(final EntityId entityId) {
		long startTime = 0;
		if (PROFILING) {
			startTime = System.currentTimeMillis();
		}
		
		final String datasetGroupName = singleEntityDataService.getDatasetGroup(entityId);
    	final DatasetGroup datasetGroup = new DatasetGroup(datasetGroupName);
    	
    	LOGGER.debug("Indexer(" + entityId.getArachneEntityID() + "): " + userRightsService.isDataimporter());
    	
    	if (!userRightsService.isDataimporter() && !userRightsService.userHasDatasetGroup(datasetGroup)) {
    		LOGGER.debug("Forbidden!");
    		final FormattedArachneEntity result = new FormattedArachneEntity();
    		result.setType("forbidden");
    		return result;
    	}
    	
    	final Dataset arachneDataset = singleEntityDataService.getSingleEntityByArachneId(entityId);
    	
    	LOGGER.debug(arachneDataset.toString());
    	
    	FormattedArachneEntity result = null;
    	if (PROFILING) {
    		final long fetchTime = System.currentTimeMillis() - startTime;
    		long nextTime = System.currentTimeMillis();

    		imageService.addImages(arachneDataset);

    		final long imageTime = System.currentTimeMillis() - nextTime;
    		nextTime = System.currentTimeMillis();

    		contextService.addMandatoryContexts(arachneDataset);
    		contextService.addContextImages(arachneDataset, imageService);

    		final long contextTime = System.currentTimeMillis() - nextTime;
    		nextTime = System.currentTimeMillis();

    		result = responseFactory.createFormattedArachneEntity(arachneDataset);

    		LOGGER.info("-- Fetching entity took " + fetchTime + " ms");
    		LOGGER.info("-- Adding images took " + imageTime + " ms");
    		LOGGER.info("-- Adding contexts took " + contextTime + " ms");
    		LOGGER.info("-- Creating response took " + (System.currentTimeMillis() - nextTime) + " ms");
    	} else {
    		imageService.addImages(arachneDataset);

    		contextService.addMandatoryContexts(arachneDataset);
    		contextService.addContextImages(arachneDataset, imageService);

    		result = responseFactory.createFormattedArachneEntity(arachneDataset);
    	}
    	return result;
	}
}
