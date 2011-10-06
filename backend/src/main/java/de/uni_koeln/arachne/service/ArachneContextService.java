package de.uni_koeln.arachne.service;

import java.util.Iterator;
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
	 * Method to append all context objects to the given dataset.
	 * Some context objects are universal like 'literatur' or 'ort'. They get included
	 * for every dataset type. Other context objects are looked up based on the 'Verknuepfungen'
	 * table.  
	 * 
	 * @param parent ArachneDataset that will gain the added context
	 */
	public void addContext(ArachneDataset parent) {
		if (parent.getArachneId().getTableName().equals("bauwerk")) {
			List<String> connectionList = arachneConnectionService.getConnectionList(parent.getArachneId().getTableName());
			Iterator<String> i = connectionList.iterator();
			while (i.hasNext()) {
				ArachneContext context = new ArachneContext(i.next(), parent, this);
				//context.getLimitContext(10);
				//parent.addContext(context);
			}
			
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
	 * Method creating an appropriate contextualizer. The class type is constructed from the <code>contextType</code>.
	 * Then reflection is used to create the corresponding class instance.
	 * 
	 * @param contextType Name of a context of interest  
	 * @return an appropriate contextualizer serving the specific context indicated by the <code>given contextType</code>
	 */
	private IContextualizer getContextByContextType(String contextType) {
		Class [] classParam = null;
		Object [] objectParam = null;
		IContextualizer contextualizer = null;
		try {
			String upperCaseContextType = contextType.substring(0, 1).toUpperCase() + contextType.substring(1).toLowerCase();
			String className = "de.uni_koeln.arachne.context." + upperCaseContextType + "Contextualizer";
			Class aClass = Class.forName(className);
			java.lang.reflect.Constructor classConstructor = aClass.getConstructor(classParam);
			return (IContextualizer)classConstructor.newInstance(objectParam);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
}
