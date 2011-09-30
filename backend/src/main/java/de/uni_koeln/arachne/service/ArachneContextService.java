package de.uni_koeln.arachne.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.context.*;
import de.uni_koeln.arachne.response.ArachneDataset;



@Service("arachneContextService")
public class ArachneContextService {
	//Calls the Right Contextualizer by name.
	
	/**
	 * Method to append all context objects to the given ADataSet 
	 * 
	 * @param parent ArachneDataset that will gain the added context
	 */
	public void addContext(ArachneDataset parent) {
		if (parent.getArachneId().getTableName().equals("bauwerk")) {
			ArachneContext litContext = new ArachneContext("literatur", parent, this);
			litContext.getLimitContext(10);
			parent.addContext(litContext);
		}
	}
	
	/**
	 * @param parent Instance of an ArachneDataset that will recieve the context
	 * @param contextName String that describes the context-type
	 * @param offset Starting position for context listing
	 * @param limit Quantity of contexts 
	 * @return Returns a list of <code>Links</code> 
	 */ 
	public List<Link> getLinks(ArachneDataset parent, String contextType, Integer offset, Integer limit) {
	    Contextualizer ctLizer = getContextByContextName(contextType);
	    return ctLizer.retrive(parent, offset, limit);
	}
	
	
	/**
	 * Method creating an appropriate contextualizer, which gets 
	 * a specific context indicated by the given contextName
	 * 
	 * @param contextName Name of a context of interest  
	 * @return an appropriate contextualizer serving a specific context indicated by the given contextName
	 */
	private Contextualizer getContextByContextName(String contextName) {
		Contextualizer ct = null;
		if (contextName.equals("literatur")) {
			ct = new LiteratureContextualizer(); 
		}
		return ct;
	}
}
