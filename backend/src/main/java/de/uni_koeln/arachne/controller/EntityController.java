package de.uni_koeln.arachne.controller;


import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import de.uni_koeln.arachne.service.ESService;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.util.StringWithHTTPStatus;

/**
 * Handles http requests (currently only get) for <code>/entity<code> and <code>/data</code>.
 */
@Controller
public class EntityController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityController.class);
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService;
	
	@Autowired
	private transient EntityService entityService;

	@Autowired
	private transient ESService esService;
	
	@RequestMapping(value="/entity/count", method=RequestMethod.GET, produces="application/json;charset=UTF-8")
	public @ResponseBody ResponseEntity<String> handleGetEntityCountRequest() {
		final long count = esService.getCount();
		if (count > -1) {
			return ResponseEntity.ok("{\"entityCount\":" + count + "}");
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"entityCount\":" + count + "}");
		}
	}
	
	/**
	 * Handles http request for /{entityId}.
	 * Requests for /entity/* return formatted data. This will be sent out either as JSON or as XML. The response format is set 
	 * by Springs content negotiation mechanism.
	 * @param entityId The unique entity id of the item to fetch.
     * @return A response object containing the data (this is serialized to JSON).
     */
	@RequestMapping(value="/entity/{entityId}", method=RequestMethod.GET, produces="application/json;charset=UTF-8")
	public @ResponseBody ResponseEntity<String> handleGetEntityIdRequest(
			@PathVariable("entityId") final Long entityId,
			@RequestParam(value = "live", required = false) final Boolean isLive,
			final HttpServletResponse response) {
		
		final StringWithHTTPStatus result;
		if (isLive != null && isLive) {
			result = entityService.getEntityFromDB(entityId, null);
		} else {
			result = entityService.getEntityFromIndex(entityId, null);
		}
		return ResponseEntity.status(result.getStatus()).body(result.getValue());
	}
    
    /**
     * Handles http request for /{category}/{id}
     * Requests for /entity/* return formatted data. This will be sent out either as JSON or as XML. The response format is set 
	 * by Springs content negotiation mechanism.
     * @param category The database table to fetch the item from.
     * @param categoryId The internal id of the item to fetch
     * @return A response object containing the data (this is serialized to JSON).
     */
    @RequestMapping(value="/entity/{category}/{categoryId}", method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> handleGetCategoryIdRequest(
    		@PathVariable("category") final String category,
    		@PathVariable("categoryId") final Long categoryId,
    		@RequestParam(value = "live", required = false) final Boolean isLive,
    		final HttpServletResponse response) {
    	
    	LOGGER.debug("Request for category: " + category + " - id: " + categoryId);
    	final StringWithHTTPStatus result;
		if (isLive != null && isLive) {
			result = entityService.getEntityFromDB(categoryId, category);
		} else {
			result = entityService.getEntityFromIndex(categoryId, category);
		}
		return ResponseEntity.status(result.getStatus()).body(result.getValue());
    }
        
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Handles http request for /doc/{id}
     * Not implemented!
     */
    /*@RequestMapping(value="/doc/{entityId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDocEntityRequest(@PathVariable("entityId") final Long entityId) {
    	// TODO implement me
    	return null;
    }
	*/
    /**
     * Handles http request for /doc/{category}/{id}
     * Not implemented!
     */
    /*@RequestMapping(value="doc/{category}/{categoryId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDocCategoryIdRequest(@PathVariable("category") final String category
    		, @PathVariable("categoryId") final Long categoryId) {
    	// TODO implement me
		return null;
    }
    */
    
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Handles http request for /data/{id}.
     * Requests for /data/* return the raw data.
     * Depending on content negotiation either JSON or XML is returned.
     * @param entityId The unique entityID.
     * @param response The outgoing HTTP response.
     * @return The <code>Dataset</code> of the requested entity.
     */
    /*@RequestMapping(value="/data/{entityId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDataEntityRequest(@PathVariable("entityId") final Long entityId, final HttpServletResponse response) {
    	return getDataRequestResponse(entityId, null, response);
    }
    */
    /**
     * Handles http request for /data/{category}/{id}.
     * Requests for /data/* return the raw data.
     * Depending on content negotiation either JSON or XML is returned.
     * @param categoryId The internal ID of the requested entity.
     * @param category The category to query.
     * @param response The outgoing HTTP response.
     * @return The <code>Dataset</code> of the requested entity
     */
    /*@RequestMapping(value="data/{category}/{categoryId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDataCategoryIdRequest(@PathVariable("category") final String category
    		, @PathVariable("categoryId") final Long categoryId, final HttpServletResponse response) {
    	return getDataRequestResponse(categoryId, category, response);
    }
    */
    /**
     * Internal function handling all http GET requests for <code>/data/*</code>.
     * It fetches the data for a given entity and returns the <code>Dataset</code>.
     * <br>
     * If the entity is not found a HTTP 404 error message is returned.
     * <br>
     * If the user does not have permission to see an entity a HTTP 403 status message is returned.
     * @param id The unique entity ID if no category is given else the internal ID.
     * @param category The category to query or <code>null</code>.
     * @param response The <code>HttpServeletRsponse</code> object.
     * @return The <code>Dataset</code> of the requested entity.
     */
    /*private Dataset getDataRequestResponse(final Long id, final String category, final HttpServletResponse response) { // NOPMD
    	final Long startTime = System.currentTimeMillis();
        
    	EntityId arachneId;
    	
    	if (category == null) {
    		arachneId = entityIdentificationService.getId(id);
    	} else {
    		arachneId = entityIdentificationService.getId(category, id);
    	}
    	
    	if (arachneId == null) {
    		response.setStatus(404);
    		return null;
    	}
    	
    	LOGGER.debug("Request for entity: " + arachneId.getArachneEntityID() + " - type: " + arachneId.getTableName());
    	
    	final String datasetGroupName = singleEntityDataService.getDatasetGroup(arachneId);
    	final DatasetGroup datasetGroup = new DatasetGroup(datasetGroupName);
    	
    	if (!userRightsService.userHasDatasetGroup(datasetGroup) && !(userRightsService.getCurrentUser().getGroupID()>=500)) {
    		response.setStatus(403);
    		return null;
    	}
    	
    	final Dataset arachneDataset = singleEntityDataService.getSingleEntityByArachneId(arachneId);
    	
    	final long fetchTime = System.currentTimeMillis() - startTime;
    	long nextTime = System.currentTimeMillis();
    	
    	imageService.addImages(arachneDataset);
    	
    	final long imageTime = System.currentTimeMillis() - nextTime;
    	nextTime = System.currentTimeMillis();
    	
    	// TODO find a way to handle contexts or discuss if we want to add them at all
    	//contextService.addMandatoryContexts(arachneDataset);
    	    	
    	final long contextTime = System.currentTimeMillis() - nextTime;
    	nextTime = System.currentTimeMillis();
    	
    	LOGGER.debug("-- Fetching entity took " + fetchTime + " ms");
    	LOGGER.debug("-- Adding images took " + imageTime + " ms");
    	LOGGER.debug("-- Adding contexts took " + contextTime + " ms");
    	LOGGER.debug("-----------------------------------");
    	LOGGER.debug("-- Complete response took " + (System.currentTimeMillis() - startTime) + " ms");
    	LOGGER.debug("Dataset: " + arachneDataset);    	
    	
    	return arachneDataset;
    }*/
}
