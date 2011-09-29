package de.uni_koeln.arachne.service;

import java.util.List;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.context.Contextualizer;
import de.uni_koeln.arachne.context.Link;
import de.uni_koeln.arachne.response.ArachneDataset;


@Service("arachneContextService")
public class ArachneContextService {
	//Calls the Right Contextualizer by name.
	
	/**
	 * @param parent Instance of an ArachneDataset that will recieve the context
	 * @param contextName String that describes the context-type
	 * @param offset Starting position for context listing
	 * @param limit Quantity of contexts 
	 * @return Returns a list of <code>Links</code> 
	 */
	public List<Link> getLinks(ArachneDataset parent, String contextType, Integer offset, Integer limit) {
	    Contextualizer ctLizer = getContextByContextName(contextType);
	    //TODO ControllStructures
	    return ctLizer.retrive(parent, offset, limit);
	}

	private Contextualizer getContextByContextName(String contextName) {
		return null;
	}
}
