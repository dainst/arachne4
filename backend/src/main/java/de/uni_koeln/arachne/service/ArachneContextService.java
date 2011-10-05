package de.uni_koeln.arachne.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.context.*;
import de.uni_koeln.arachne.response.ArachneDataset;

/**
 * This class handles creation and retrieval of contexts and adds them to datasets.
 * Internally it uses <code>Contextualizers</code> to abstract the data access and allow to fetch contexts not only from
 * the Arachne database but from any other datasource (even external ones).  
 */
@Service("arachneContextService")
public class ArachneContextService {
	
	/**
	 * Service to access the 'Verknuepfungen' table. The information stored in that table is used
	 * to determine which contexts the <code>addContext</code> method adds to a given dataset.
	 */	
	@Autowired
	private ArachneConnectionService arachneConnectionService;
	
	/**
	 * Method to append all context objects to the given dataset 
	 * 
	 * @param parent ArachneDataset that will gain the added context
	 */
	public void addContext(ArachneDataset parent) {
		if (parent.getArachneId().getTableName().equals("bauwerk")) {
			arachneConnectionService.getConnectionList(parent.getArachneId().getTableName());
			
			ArachneContext litContext = new ArachneContext("literatur", parent, this);
			litContext.getLimitContext(10);
			parent.addContext(litContext);
		}
	}
	
	/**
	 * This function retrieves the contexts according to the given criteria.
	 * It uses a context specific contextualizer to fetch the data.
	 * @param parent Instance of an <code>ArachneDataset</code> that will receive the context
	 * @param contextName String that describes the context-type
	 * @param offset Starting position for context listing
	 * @param limit Quantity of contexts 
	 * @return Returns a list of <code>Links</code> 
	 */ 
	public List<Link> getLinks(ArachneDataset parent, String contextType, Integer offset, Integer limit) {
	    IContextualizer contextualizer = getContextByContextType(contextType);
	    return contextualizer.retrieve(parent, offset, limit);
	}
	
	/**
	 * Method creating an appropriate contextualizer.
	 * 
	 * @param contextType Name of a context of interest  
	 * @return an appropriate contextualizer serving the specific context indicated by the given contextName
	 */
	private IContextualizer getContextByContextType(String contextType) {
		IContextualizer contextualizer = null;
		if (contextType.equals("literatur")) {
			contextualizer = new LiteratureContextualizer(); 
		}
		return contextualizer;
	}
}
