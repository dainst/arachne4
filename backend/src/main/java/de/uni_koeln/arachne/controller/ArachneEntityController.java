package de.uni_koeln.arachne.controller;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.response.BaseArachneEntity;
import de.uni_koeln.arachne.response.FormattedArachneEntity;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.service.ArachneContextService;
import de.uni_koeln.arachne.service.ArachneEntityIdentificationService;
import de.uni_koeln.arachne.service.ArachneSingleEntityDataService;
import de.uni_koeln.arachne.service.AttributeService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * Handles http requests (currently only get) for <code>/entity<code>.
 */
@Controller
public class ArachneEntityController {
	
	@Autowired
	ArachneEntityIdentificationService arachneEntityIdentificationService;

	@Autowired
	ArachneSingleEntityDataService arachneSingleEntityDataService;
	
	@Autowired
	ArachneContextService contextService;
	
	@Autowired
	UserRightsService userRightsService;
	
	@Autowired
	ResponseFactory responseFactory;
	
	@Autowired
	AttributeService attributeService;
	
	/**
	 * Handles http request for /{id}
	 * @param itemId The unique entity id of the item to fetch.
     * @return a response object containing the data (currently this a serialized to JSON by Jackson).
     */
	@RequestMapping(value="/entity/{id}", method=RequestMethod.GET)
	public @ResponseBody BaseArachneEntity handleGetEntityIdRequest(HttpServletRequest request, @PathVariable("id") Long id) {
		return getEntityRequestResponse(id, null);
	}
    
    /**
     * Handles http request for /{category}/{id}
     * @param category The database table to fetch the item from.
     * @param id The internal id of the item to fetch
     * @return a response object containing the data (currently this a serialized to JSON by Jackson).
     */
    @RequestMapping(value="/entity/{category}/{id}", method=RequestMethod.GET)
    public @ResponseBody BaseArachneEntity handleGetCategoryIdRequest(@PathVariable("category") String category, @PathVariable("id") Long id) {
    	return getEntityRequestResponse(id, category);
    }

    /**
     * Internal function handling all http GET requests for <code>/entity/*</code>.
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it in a response object.
     * @param id The unique entity ID if no category is given else the internal ID.
     * @param category The category to query or <code>null</code>.
     * @return A response object derived from <code>BaseArachneEntity</code>.
     */
    private BaseArachneEntity getEntityRequestResponse(Long id, String category) {
    	userRightsService.initializeUserData();
    	ArachneId arachneId = null;
    	
    	if (category == null) {
    		arachneId = arachneEntityIdentificationService.getId(id);
    	} else {
    		arachneId = arachneEntityIdentificationService.getId(category, id);
    	}
    	
    	ArachneDataset arachneDataset = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId);
    	//attributeService.addExternalFields(arachneDataset);
    	
    	// TODO move to the approproate request handler
    	//contextService.addContext(arachneDataset);
    	
    	FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(arachneDataset);
    	
    	return response;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Handles http request for /doc/{id}
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it 
     * in a <Code>JsonResponse</Code> object.
     * @param itemId The id of the item to fetch
     * @return a JSON object containing the data
     */
    @RequestMapping(value="/doc/{id}", method=RequestMethod.GET)
    public @ResponseBody ArachneDataset handleGetDocEntityRequest(@PathVariable("id") Long id) {
    	// TODO implement me
    	return null;
    }

    /**
     * Handles http request for /doc/{category}/{id}
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it 
     * in a <Code>JsonResponse</Code> object.
     * @param itemId The id of the item to fetch
     * @return a JSON object containing the data
     */
    @RequestMapping(value="doc/{category}/{id}", method=RequestMethod.GET)
    public @ResponseBody ArachneDataset handleGetDocCategoryIdRequest(@PathVariable("category") String category, @PathVariable("id") Long id) {
    	// TODO implement me
		return null;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Handles http request for /doc/{id}
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it 
     * in a <Code>JsonResponse</Code> object.
     * @param itemId The id of the item to fetch
     * @return a JSON object containing the data
     */
    @RequestMapping(value="/data/{id}", method=RequestMethod.GET)
    public @ResponseBody ArachneDataset handleGetDataEntityRequest(@PathVariable("id") Long id) {
    	// TODO implement me
		return null;
    }
    
    /**
     * Handles http request for /doc/{category}/{id}
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it 
     * in a <Code>JsonResponse</Code> object.
     * @param itemId The id of the item to fetch
     * @return a JSON object containing the data
     */
    @RequestMapping(value="data/{category}/{id}", method=RequestMethod.GET)
    public @ResponseBody ArachneDataset handleGetDataCategoryIdRequest(@PathVariable("category") String category, @PathVariable("id") Long id) {
    	// TODO implement me
    	return null;
    }
}