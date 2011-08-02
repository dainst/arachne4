package de.uni_koeln.arachne.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.arachne.responseobjects.JsonResponse;
import de.uni_koeln.arachne.service.ArachneEntityIdentificationService;
import de.uni_koeln.arachne.util.ArachneId;

@Controller
public class ArachneEntityController {
	@Autowired
	ArachneEntityIdentificationService arachneEntityIdentificationService;
    /**
     * Handles the http request.
     * It uses the <Code>ItemService</Code> class to fetch the data and wraps it 
     * in a <Code>JsonResponse</Code> object.
     * @param itemId The id of the item to fetch
     * @return a JSON object containing the data
     */
	
    @RequestMapping(value="/entity/{itemId}", method=RequestMethod.GET)
    public @ResponseBody JsonResponse handleGetItemRequest(@PathVariable("itemId") Long itemId) {
    	
    		ArachneId temp =arachneEntityIdentificationService.getByEntityID(itemId);
            JsonResponse response = new JsonResponse();
            response.setItemId(temp.getInternalKey());
            return response;
    }


	
}
