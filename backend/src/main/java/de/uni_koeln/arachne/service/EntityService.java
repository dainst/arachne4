package de.uni_koeln.arachne.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.FormattedArachneEntity;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.util.EntityId;

@Service("EntityService")
public class EntityService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityService.class);
	
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
	
	/**
	 * This functions retrieves a <code>FromattedArachneEntity</code>.
	 * @param entityId The corresponding EntityId object.
	 * @return The requested formatted entity object or an empty <code>FromattedArachneEntity</code> object where the type 
	 * is set to "forbidden" to indicate that the user is not allowed to see this entity.
	 */
	public FormattedArachneEntity getFormattedEntityById(final EntityId entityId) {
		final long startTime = System.currentTimeMillis();
		final String datasetGroupName = singleEntityDataService.getDatasetGroup(entityId);
    	final DatasetGroup datasetGroup = new DatasetGroup(datasetGroupName);
    	
    	LOGGER.debug("Indexer(" + entityId.getArachneEntityID() + "): " + userRightsService.isUserSolr());
    	
    	if ((!userRightsService.isUserSolr()) && (!userRightsService.userHasDatasetGroup(datasetGroup))) {
    		LOGGER.debug("Forbidden!");
    		final FormattedArachneEntity result = new FormattedArachneEntity();
    		result.setType("forbidden");
    		return result;
    	}
    	
    	final Dataset arachneDataset = singleEntityDataService.getSingleEntityByArachneId(entityId);
    	
    	LOGGER.debug(arachneDataset.toString());
    	final long fetchTime = System.currentTimeMillis() - startTime;
    	long nextTime = System.currentTimeMillis();
    	
    	imageService.addImages(arachneDataset);
    	
    	final long imageTime = System.currentTimeMillis() - nextTime;
    	nextTime = System.currentTimeMillis();
    	
    	contextService.addMandatoryContexts(arachneDataset);
    	contextService.addContextImages(arachneDataset, imageService);
    	
    	final long contextTime = System.currentTimeMillis() - nextTime;
    	nextTime = System.currentTimeMillis();
    	
    	final FormattedArachneEntity result = responseFactory.createFormattedArachneEntity(arachneDataset);
    	
    	LOGGER.debug("-- Fetching entity took " + fetchTime + " ms");
    	LOGGER.debug("-- Adding images took " + imageTime + " ms");
    	LOGGER.debug("-- Adding contexts took " + contextTime + " ms");
    	LOGGER.debug("-- Creating response took " + (System.currentTimeMillis() - nextTime) + " ms");
    	
    	return result;
	}
}
