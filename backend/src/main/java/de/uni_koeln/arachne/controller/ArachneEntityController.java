package de.uni_koeln.arachne.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.BaseArachneEntity;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.FormattedArachneEntity;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.service.ContextService;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.ImageService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.util.EntityId;

/**
 * Handles http requests (currently only get) for <code>/entity<code>.
 */
@Controller
public class ArachneEntityController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ArachneEntityController.class);
	
	@Autowired
	transient private EntityIdentificationService entityIdentificationService;

	@Autowired
	transient private SingleEntityDataService singleEntityDataService;
	
	@Autowired
	transient private ContextService contextService;
	
	@Autowired
	transient private ResponseFactory responseFactory;
	
	@Autowired
	transient private ImageService imageService;
	
	/**
	 * Handles http request for /{id}
	 * @param entityId The unique entity id of the item to fetch.
     * @return A response object containing the data (this is serialized to JSON or XML depending on content negotiation).
     */
	@RequestMapping(value="/entity/{entityId}", method=RequestMethod.GET)
	public @ResponseBody BaseArachneEntity handleGetEntityIdRequest(final HttpServletRequest request
			, @PathVariable("entityId") final Long entityId, final HttpServletResponse response) {
		LOGGER.debug(request.getLocalAddr());
		LOGGER.debug(request.getPathInfo());
		LOGGER.debug(request.getRemoteAddr());
		LOGGER.debug(request.getRemoteHost());
		return getEntityRequestResponse(entityId, null, response);
	}
    
    /**
     * Handles http request for /{category}/{id}
     * @param category The database table to fetch the item from.
     * @param categoryId The internal id of the item to fetch
     * @return A response object containing the data (this is serialized to JSON or XML depending on content negotiation).
     */
    @RequestMapping(value="/entity/{category}/{categoryId}", method=RequestMethod.GET)
    public @ResponseBody BaseArachneEntity handleGetCategoryIdRequest(@PathVariable("category") final String category
    		, @PathVariable("categoryId") final Long categoryId, final HttpServletResponse response) {
    	LOGGER.debug("Request for category: " + category + " - id: " + categoryId);
    	return getEntityRequestResponse(categoryId, category, response);
    }

    /**
     * Internal function handling all http GET requests for <code>/entity/*</code>.
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it in a response object.
     * @param id The unique entity ID if no category is given else the internal ID.
     * @param category The category to query or <code>null</code>.
     * @return A response object derived from <code>BaseArachneEntity</code>.
     */
    private BaseArachneEntity getEntityRequestResponse(final Long id, final String category //NOPMD
    		, final HttpServletResponse response) { 
    	final Long startTime = System.currentTimeMillis();
        
    	EntityId arachneId;
    	
    	if (category == null) {
    		arachneId = entityIdentificationService.getId(id);
    	} else {
    		arachneId = entityIdentificationService.getId(category, id);
    	}
    	
    	if (arachneId == null) {
    		LOGGER.debug("Warning: Missing ArachneEntityID");
    		response.setStatus(404);
    		return null;
    	}
    	LOGGER.debug("Request for entity: " + arachneId.getArachneEntityID() + " - type: " + arachneId.getTableName());
    	
    	final Dataset arachneDataset = singleEntityDataService.getSingleEntityByArachneId(arachneId);
    	
    	final long fetchTime = System.currentTimeMillis() - startTime;
    	long nextTime = System.currentTimeMillis();
    	
    	imageService.addImages(arachneDataset);
    	
    	final long imageTime = System.currentTimeMillis() - nextTime;
    	nextTime = System.currentTimeMillis();
    	
    	contextService.addMandatoryContexts(arachneDataset);
    	
    	final long contextTime = System.currentTimeMillis() - nextTime;
    	nextTime = System.currentTimeMillis();
    	
    	final FormattedArachneEntity result = responseFactory.createFormattedArachneEntity(arachneDataset);
    	
    	LOGGER.debug("-- Fetching entity took " + fetchTime + " ms");
    	LOGGER.debug("-- Adding images took " + imageTime + " ms");
    	LOGGER.debug("-- Adding contexts took " + contextTime + " ms");
    	LOGGER.debug("-- Creating response took " + (System.currentTimeMillis() - nextTime) + " ms");
    	LOGGER.debug("-----------------------------------");
    	LOGGER.debug("-- Complete response took " + (System.currentTimeMillis() - startTime) + " ms");
    	return result;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Handles http request for /doc/{id}
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it 
     * in a <Code>JsonResponse</Code> object.
     * @param entityId The id of the item to fetch
     * @return a JSON object containing the data
     */
    @RequestMapping(value="/doc/{entityId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDocEntityRequest(@PathVariable("entityId") final Long entityId) {
    	// TODO implement me
    	return null;
    }

    /**
     * Handles http request for /doc/{category}/{id}
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it 
     * in a <Code>JsonResponse</Code> object.
     * @param categoryId The id of the item to fetch
     * @return a JSON object containing the data
     */
    @RequestMapping(value="doc/{category}/{categoryId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDocCategoryIdRequest(@PathVariable("category") final String category
    		, @PathVariable("categoryId") final Long categoryId) {
    	// TODO implement me
		return null;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Handles http request for /data/{id}
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it 
     * in a <Code>JsonResponse</Code> object.
     * @param entityId The id of the item to fetch
     * @return a JSON object containing the data
     */
    @RequestMapping(value="/data/{entityId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDataEntityRequest(@PathVariable("entityId") final Long entityId) {
    	// TODO implement me
		return null;
    }
    
    /**
     * Handles http request for /doc/{category}/{id}
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it 
     * in a <Code>JsonResponse</Code> object.
     * @param categoryId The id of the item to fetch
     * @return a JSON object containing the data
     */
    @RequestMapping(value="data/{category}/{categoryId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDataCategoryIdRequest(@PathVariable("category") final String category
    		, @PathVariable("categoryId") final Long categoryId) {
    	// TODO implement me
    	return null;
    }
}