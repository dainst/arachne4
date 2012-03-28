package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.ContextService;

/**
 * This Class is a wrapper that holds and manages contexts of <code>ArachneDatasets</code>. The links are fetched on demand.
 * The combination of context type and parent describes the two ends of a <code>Link</code>. 
 */
public class Context {
	/**
	 * The service which manages the retrieval etc.
	 */
	protected transient ContextService contextService;
	
	/**
	 * Constructor setting up all needed fields.
	 * @param contextType The type of context this class manages.
	 * @param parent The <code>ArachneDataset</code> this context belongs to.
	 */
	public Context(final String contextType, final Dataset parent, final ContextService contextService) {
		completionState = CompletionStateEnum.EMPTY;
		this.contextType = contextType;
		this.parent = parent;
		contextEntities = new ArrayList<AbstractLink>();
		this.contextService = contextService;
	}
	
	/**
	 *  A specified context type like Ort, Literatur, Literaturzitat.
	 *  In most cases the context type is the name of the table used for the query.
	 *  For external contexts (not implemented yet) a different scheme is used.
	 */
	protected transient String contextType;

	/**
	 * The parent dataset. It describes where the contexts belongs to.
	 */
	protected transient Dataset parent;

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
	protected transient CompletionStateEnum completionState;
		
	/**
	 * The depth of the context.
	 */
	protected int depthLevel;

	/**
	 * This list of <code>Link</code> contains the data of the context.
	 */
	public transient List<AbstractLink> contextEntities;
	
	//The Context Getter
	
	/**
	 * Return every <code>Link</code> in this context.
	 * @return The complete list of contexts.
	 */
	public List<AbstractLink> getallContexts() {
		if (completionState != CompletionStateEnum.FULL) {
			retrieveComplete();
		}
		if (contextEntities.isEmpty()) {
			return  null;
		} else {
			return contextEntities;
		}
	}

	/**
	 * Returns the first link element of the context.
	 * @return The First <code>Link</code> of the context.
	 */
	public AbstractLink getFirstContext() {
		if (completionState == CompletionStateEnum.EMPTY) {
			retrieveFirst();
		}
		if (contextEntities.isEmpty()) {
			return null; 
		} else {
			return contextEntities.get(0);
		}
	}
	
	/**
	 * Returns one link element of the context.
	 * @param index The index of the link element to retrieve.
	 * @return The chosen <code>Link</code> of the context.
	 */
	public AbstractLink getContext(final int index) {
		final int avilableContexts = contextEntities.size();
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
	public List<AbstractLink> getLimitContext(final int number) {
		if (completionState != CompletionStateEnum.LIMITED && completionState != CompletionStateEnum.FULL) {
			retrieveLimited(number);
		}

		if (number > contextEntities.size()) {
			completionState = CompletionStateEnum.FULL;
		} else {
			completionState = CompletionStateEnum.LIMITED;
		}

		if (contextEntities.isEmpty()) {
			return null; 
		} else {
			return contextEntities;
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
	protected void retrieveLimited(final int limit) {
	    retrieve(contextEntities.size(), limit - contextEntities.size());
	    completionState = CompletionStateEnum.LIMITED;
	}

	/**
	 * Internally used function to call the context service with the needed information to fill the <code>contextEntities</code>.
	 * @param offset And offset describing where to start getting context information.
	 * @param limit The maximum number of contexts to retrieve.
	 */
	protected void retrieve(final int offset, final int limit) {
		final List<AbstractLink> temporary = contextService.getLinks(parent, contextType, offset, limit);
	    if (temporary != null) {
	    	contextEntities.addAll(temporary);
	    }
	}
	
	/**
	 * This method returns the number of context entities in this context. If not all contexts are retrieved already
	 * it retrieves them.
	 * @return The number of context entities.
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