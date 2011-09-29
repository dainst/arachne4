package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.response.Link;
import de.uni_koeln.arachne.service.ArachneContextService;

/**
 * This Class is a Wrapper that holds and manages contexts of <code>ArachneDatasets</code>. The links are fetched on demand.
 * The combination of context name and parent describes the two ends of a <code>Link</code>. 
 */
public class ArachneContext {
	/**
	 * @param ctname The Name of the Contexts this class Manages
	 * @param par The ArachneDataset this Context belongs to
	 */
	public ArachneContext(String ctname, ArachneDataset parent) {
		completionState = CompletionStateEnum.EMPTY;
		contextType = ctname;
		this.parent = parent;
		contextEntities = new ArrayList<Link>();
	}
	
	/**
	 *  A specified context type like Ort, Literatur, Literaturzitat.
	 *  In most cases the context type is the name of the table used for the query.
	 *  For external contexts (not implemented yet) a different scheme is used.
	 */
	protected String contextType;

	/**
	 * The parent dataset. It describes where the contexts belongs to.
	 */
	protected ArachneDataset parent;

	/**
	 * An enumeration class representing the state of the context.
	 */
	protected enum CompletionStateEnum {
		//Full means that the Context is Completely loaded
		FULL, 
		//Limited means that the Context contains more than one context but is not Complete
		LIMITED, 
		//Means that the Context has the First Element loaded
		FIRST, 
		//The Context is Empty 
		EMPTY
	};
	
	/**
	 * The completion state of the context. Can be one of <code>FULL</code>
	 */
	protected CompletionStateEnum completionState;
		
	/**
	 * The Autowired Service which manages the Retrival etc.
	 */
	@Autowired
	protected ArachneContextService contextService;
	
	/**
	 * The depth of the context.
	 */
	protected int depthLevel;

	/**
	 * This list of <code>Link</code> contains the data of the context.
	 */
	public List<Link> contextEntities;
	
	//The Context Getter
	
	/**
	 * Return every <code>Link</code> in this context.
	 * @return The complete list of contexts.
	 */
	public List<Link> getallContexts() {
	if (completionState != CompletionStateEnum.FULL)
	    retrieveComplete();
		return contextEntities;
	}

	/**
	 * Returns the first link element of the context.
	 * @return The First <code>Link</code> of the context.
	 */
	public Link getFirstContext() {
	if (completionState != CompletionStateEnum.EMPTY)
	    retrieveFirst();
		return contextEntities.get(0);
	}
	
	/**
	 * Return a given number of contexts or the maximum number of contexts.
	 * @param number The number of contexts demanded.
	 * @return The number of contexts (more or less).
	 */
	public List<Link> getLimitContext(int number) {
	if (completionState != CompletionStateEnum.LIMITED && completionState != CompletionStateEnum.FULL)
	    retrieveLimited(number);
	    if (number > contextEntities.size())
	        completionState = CompletionStateEnum.FULL;
	    else
	        completionState = CompletionStateEnum.LIMITED;

	    return contextEntities;
	}
	
	/**
	 * Internally used convenient function to fill the context list with all available contexts.
	 * Side effect: Sets <code>completionState</code>.
	 */
	protected void retrieveComplete() {
	    retrieve(contextEntities.size(), -1);
	    completionState = CompletionStateEnum.FULL;
	}

	/**
	 * Internally used convenient function to only retrieve the first context.
	 * Side effect: Sets <code>completionState</code>
	 */
	protected void retrieveFirst() {
	    retrieve(0, 1);
	    completionState = CompletionStateEnum.FIRST;
	}

	/**
	 * Internally used convenient function to retrieve a limited number of contexts.
	 * Side effects: Sets <code>completionState</code>
	 * @param number The number of contexts to retrieve
	 */
	protected void retrieveLimited(int number) {
	    retrieve(contextEntities.size(), number - contextEntities.size());
	    completionState = CompletionStateEnum.LIMITED;
	}

	/**
	 * Internally used function to call the context service with the needed information to fill the <code>contextEntities</code>.
	 * @param offset And offset describing where to start getting context information.
	 * @param limit The maximum number of contexts to retrieve.
	 */
	protected void retrieve(int offset, int Limit) {
	    //List<Link> temporary = CS.getLinks(parent, contextName, offset, limit);
	    //contextEntities.appendAll(temporary);
	}
}