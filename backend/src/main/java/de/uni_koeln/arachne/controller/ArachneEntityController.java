package de.uni_koeln.arachne.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.responseobjects.ArachneDataset;
import de.uni_koeln.arachne.service.ArachneEntityIdentificationService;
import de.uni_koeln.arachne.service.ArachneSingleEntityDataService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * Handles http requests (currently only get) for <code>/entity<code>.
 */
@Controller
//@RequestMapping(value="/entity", method=RequestMethod.GET)
public class ArachneEntityController {
	
	@Autowired
	ArachneEntityIdentificationService arachneEntityIdentificationService;

	@Autowired
	ArachneSingleEntityDataService arachneSingleEntityDataService;
	@Autowired
	UserRightsService userRightsService;
	
    /**
     * Handles the http request.
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it 
     * in a <Code>JsonResponse</Code> object.
     * @param itemId The id of the item to fetch
     * @return a JSON object containing the data
     */

    @RequestMapping(value="/entity/{itemId}", method=RequestMethod.GET)
    public @ResponseBody ArachneDataset handleGetItemRequest(@PathVariable("itemId") Long itemId) {
    		userRightsService.initializeUserData();
    		ArachneId temp =arachneEntityIdentificationService.getByEntityID(itemId);
            //JsonResponse response = new JsonResponse();
            //response.setItemId(temp.getInternalKey());
            ArachneDataset response=   arachneSingleEntityDataService.getSingleEntityByArachneId(temp);
    		
    		return response;
    }
}