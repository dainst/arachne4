package de.uni_koeln.arachne.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

/**
 * Service to retrieve entities either from the elasticsearch index or the db.
 * 
 * @author Reimar Grabowski
 */
@Service
public class EntityService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityService.class);
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService;
	
	@Autowired
	private transient SingleEntityDataService singleEntityDataService;
	
	@Autowired
	private transient ContextService contextService;
	
	@Autowired
	private transient ImageService imageService;
	
	@Autowired
	private transient UserRightsService userRightsService;
	
	@Autowired
	private transient ESService esService;
	
	@Autowired
	private transient ResponseFactory responseFactory;
	
	private transient final boolean PROFILING;
	private transient final String[] internalFields;
		
	@Autowired
	public EntityService(final @Value("${profilingEntityRetrieval}") boolean profiling
			, final @Value("#{'${internalFields}'.split(',')}") String[] internalFields) {
		this.PROFILING = profiling;
		this.internalFields = internalFields;
	}
	
	public TypeWithHTTPStatus<String> getEntityFromIndex(final Long id, final String category) {
		
		String[] excludedFields;
		if (userRightsService.userHasAtLeastGroupID(UserRightsService.MIN_EDITOR_ID)) {
			excludedFields = internalFields;
		} else {
			excludedFields = new String[internalFields.length + 1];
			System.arraycopy(internalFields, 0, excludedFields, 0, internalFields.length);
			excludedFields[internalFields.length] = "editorSection";
		}
		
    	final TypeWithHTTPStatus<String> result = esService.getDocumentFromCurrentIndex(id, category, excludedFields);
    	    	
    	if (result.getStatus() == HttpStatus.NOT_FOUND ) {
    		// if the entity is not found in the ES index it may have been deleted, so we try to retrieve it from 
    		// the DB to get a nice deleted message without duplicating code or burdening the dataimport with the 
    		// task of keeping track of deleted entities or adding them to the index
    		return getEntityFromDB(id, category);
    	}
    	
    	return result;
	}
	
	/**
     * Internal function handling all http GET requests for <code>/entity/*</code>.
     * It fetches the data for a given entity and returns it as a response object.
     * <br>
     * If the entity is not found a HTTP 404 error message is returned.
     * <br>
     * If the user does not have permission to see an entity a HTTP 403 status message is returned.
     * @param id The unique entity ID if no category is given else the internal ID.
     * @param category The category to query or <code>null</code>.
     * @return The response body as <code>String</code>.
     */
    public TypeWithHTTPStatus<String> getEntityFromDB(final Long id, final String category) { 
    	final EntityId entityId;
    	if (category == null) {
    		entityId = entityIdentificationService.getId(id);
    	} else {
    		entityId = entityIdentificationService.getId(category, id);
    	}
    	
    	if (entityId == null) {
    		return new TypeWithHTTPStatus<String>(HttpStatus.NOT_FOUND);
    	}
    	
    	LOGGER.debug("Request for entity: " + entityId.getArachneEntityID() + " - type: " + entityId.getTableName());
    	
    	if (entityId.isDeleted()) {
    		return new TypeWithHTTPStatus<String>(responseFactory.createResponseForDeletedEntityAsJsonString(entityId));
    	}
    	
    	final String result = getFormattedEntityByIdAsJsonString(entityId);
    	
    	if ("forbidden".equals(result)) {
    		return new TypeWithHTTPStatus<String>(HttpStatus.FORBIDDEN);
    	}
    	
    	if (result != null) {
    		return new TypeWithHTTPStatus<String>(result);
    	}
    	
    	return new TypeWithHTTPStatus<String>(HttpStatus.NOT_FOUND);
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
