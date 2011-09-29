package de.uni_koeln.arachne.context;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.response.Link;
import de.uni_koeln.arachne.service.ArachneContextService;
/**
 * This Class is a Wrapper that Holds and Manages Contexts of Datasets. The links are fetched on Demand.
 * The Combination of Context Name and Parent describes the two ends of the Links. 
 */
public class ArachneContext {
	// A Specified Context Type like Ort, Literatur, Literaturzitat.
	private String contextName;

	//The Parent Dataset, it Describes where The Contexts belongs to.
	private ArachneDataset parent;

	//An Enumeration which Represents the State of the Context
	private enum CompletionVersionEnum {
		//Full means that the Context is Completely loaded
		FULL, 
		//Limited means that the Context contains more than one context but is not Complete
		LIMITED, 
		//Means that the Context has the First Element loaded
		FIRST, 
		//The Context is Empty 
		EMPTY
	};
	
	private CompletionVersionEnum completionVersion;
		
	//The Autowired Service which manges the Retrival etc.
	@Autowired
	private ArachneContextService cs;
	
	//Depth of The Context
	private int depthLevel;

	//List of Links
	List<Link> contextEntities;
	
	/**
	 * @param ctname The Name of the Contexts this class Manages
	 * @param par The ArachneDataset this Context belongs to
	 */
	public ArachneContext(String ctname,ArachneDataset par) {
		completionVersion = CompletionVersionEnum.EMPTY;
		contextName = ctname;
		parent = par;
	}
	
	//The Context Getter
	/**
	 * Return every Link in this Context
	 * @return The Complete List of Contexts 
	 */
	public List<Link> getallContexts() {
	if (completionVersion != CompletionVersionEnum.FULL)
	    doCompleteRetrival();
		return contextEntities;
	}

	/**
	 * Returns the First Link Element of the Context
	 * @return The First Link Element of the Context
	 */
	public Link getFirstContext() {
	if (completionVersion != CompletionVersionEnum.EMPTY)
	    doFirstRetrival();
		return contextEntities.get(0);
	}
	/**
	 * return a given number of Contexts or the Maximum Number of Contexts
	 * @param howMany The Number of Contexts Demanded
	 * @return The number of Contexts More or less
	 */
	public List<Link> getLimitContext(int howMany) {
	if (completionVersion != CompletionVersionEnum.LIMITED && completionVersion != CompletionVersionEnum.FULL)
	    doLimitRetrival(howMany);
	    if (howMany > contextEntities.size())
	        completionVersion = CompletionVersionEnum.FULL;
	    else
	        completionVersion = CompletionVersionEnum.LIMITED;

	    return contextEntities;
	}
	
	//The Retrival Functions are Wrappers for the doRetrival method
	protected void doCompleteRetrival() {
	    doRetrival(contextEntities.size(), -1);
	    completionVersion = CompletionVersionEnum.FULL;
	}

	protected void doFirstRetrival() {
	    doRetrival(0, 1);
	    completionVersion = CompletionVersionEnum.FIRST;
	}

	protected void doLimitRetrival(int howMany) {
	    doRetrival(contextEntities.size(), howMany - contextEntities.size());
	    completionVersion = CompletionVersionEnum.LIMITED;
	}

	//Do Retrival Calls the Context Service with the needed Information
	protected void doRetrival(int offset, int Limit) {
	    //List<Link> temporary = CS.getLinks(parent, contextName, offset, limit);
	    //contextEntities.appendAll(temporary);
	}
}