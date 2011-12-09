package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.service.ContextService;

/**
 * This Class is a wrapper that holds and manages contexts of <code>ArachneDatasets</code>. The links are fetched on demand.
 * The combination of context type and parent describes the two ends of a <code>Link</code>. 
 */
public class ArachneContext {
	/**
	 * The service which manages the retrieval etc.
	 */
	protected ContextService contextService;
	
	/**
	 * Constructor setting up all needed fields.
	 * @param contextType The type of context this class manages.
	 * @param parent The <code>ArachneDataset</code> this context belongs to.
	 */
	public ArachneContext(String contextType, ArachneDataset parent, ContextService contextService) {
		completionState = CompletionStateEnum.EMPTY;
		this.contextType = contextType;
		this.parent = parent;
		contextEntities = new ArrayList<Link>();
		this.contextService = contextService;
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
		// the context is completely loaded
		FULL, 
		// the context contains more than one context but is not complete
		LIMITED,
		// only the first context element is loaded
		FIRST, 
		// the context is empty
		EMPTY
	};
	
	/**
	 * The completion state of the context. Can be one of <code>FULL</code>, <code>LIMITED</code>, <code>FIRST</code> or <code>EMPTY</code>.
	 */
	protected CompletionStateEnum completionState;
		
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
		if (completionState != CompletionStateEnum.FULL) {
			retrieveComplete();
		}
		if (!contextEntities.isEmpty()) {
			return contextEntities; 
		} else {
			return null;
		}
	}

	/**
	 * Returns the first link element of the context.
	 * @return The First <code>Link</code> of the context.
	 */
	public Link getFirstContext() {
		if (completionState == CompletionStateEnum.EMPTY) {
			retrieveFirst();
		}
		if (!contextEntities.isEmpty()) {
			return contextEntities.get(0); 
		} else {
			return null;
		}
	}
	
	/**
	 * Returns one link element of the context.
	 * @param index The index of the link element to retrieve.
	 * @return The chosen <code>Link</code> of the context.
	 */
	public Link getContext(int index) {
		int avilableContexts = contextEntities.size();
		if (completionState != CompletionStateEnum.FULL) {
			if (index >= avilableContexts) {
				retrieve(avilableContexts, avilableContexts + index);
			}
		}
		if (!contextEntities.isEmpty() && index<avilableContexts) {
			return contextEntities.get(index); 
		} else {
			return null;
		}
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

	    if (!contextEntities.isEmpty()) {
			return contextEntities; 
		} else {
			return null;
		}
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
	 * Side effect: Sets <code>completionState</code>
	 * @param limit The number of contexts to retrieve
	 */
	protected void retrieveLimited(int limit) {
	    retrieve(contextEntities.size(), limit - contextEntities.size());
	    completionState = CompletionStateEnum.LIMITED;
	}

	/**
	 * Internally used function to call the context service with the needed information to fill the <code>contextEntities</code>.
	 * @param offset And offset describing where to start getting context information.
	 * @param limit The maximum number of contexts to retrieve.
	 */
	protected void retrieve(int offset, int limit) {
	    List<Link> temporary = contextService.getLinks(parent, contextType, offset, limit);
	    if (temporary != null) {
	    	contextEntities.addAll(temporary);
	    }
	}
	
	/**
	 * This method returns the number of context entities in this context. If not all contexts are retrieved already
	 * it retrieves them.
	 * @return
	 */
	public int getContextSize() {
		if (completionState != CompletionStateEnum.FULL) {
			retrieveComplete();
		}
		return contextEntities.size();
	}
	
	public String getContextType() {
		return this.contextType;
	}
	
	public String toString() {
		return contextEntities.toString();
	}
}