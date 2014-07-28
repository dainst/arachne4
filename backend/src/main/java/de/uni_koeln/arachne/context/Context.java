package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.arachne.response.Dataset;


/**
 * This Class is a wrapper that holds and manages contexts of <code>ArachneDatasets</code>. The links are fetched on demand.
 * The combination of context type and parent describes the two ends of a <code>Link</code>.
 * 
 * This class is in dire need of a rewrite as the design does not really fit the backend use case. Contexts are always 
 * retrieved completely anyways. And the fact that a context needs a reference to the <code>ContextService</code> shows 
 * that the service can do the retrieval and fill the contexts. 
 */
public class Context {
	
	/**
	 *  A specified context type like Ort, Literatur, Literaturzitat.
	 *  In most cases the context type is the name of the table used for the query.
	 *  For external contexts (not implemented yet) a different scheme is used.
	 */
	protected transient final String contextType;

	/**
	 * The parent dataset. It describes where the contexts belongs to.
	 */
	protected transient final Dataset parent;

	/**
	 * The depth of the context.
	 */
	protected int depthLevel;

	/**
	 * This list of <code>Link</code> contains the data of the context.
	 */
	protected transient final List<AbstractLink> contextEntities;
	
	/**
	 * Constructor setting up all needed fields.
	 * @param contextType The type of context this class manages.
	 * @param parent The <code>ArachneDataset</code> this context belongs to.
	 */
	public Context(final String contextType, final Dataset parent, final List<AbstractLink> contextEntities) {
		this.contextType = contextType;
		this.parent = parent;
		if (contextEntities == null) {
			this.contextEntities = new ArrayList<AbstractLink>();
		} else {
			this.contextEntities = contextEntities;
		}
		
	}
	
	// The Context Getter
	
	/**
	 * Return every <code>Link</code> in this context.
	 * @return The complete list of contexts.
	 */
	public List<AbstractLink> getAllContexts() {
		if (contextEntities.isEmpty()) {
			return null;
		} else {
			return contextEntities;
		}
	}

	/**
	 * Returns the first link element of the context.
	 * @return The First <code>Link</code> of the context.
	 */
	public AbstractLink getFirstContext() {
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
		if (!contextEntities.isEmpty() && index < contextEntities.size()) {
			return contextEntities.get(index); 
		} else {
			return null;
		}
	}
	
	/**
	 * This method returns the number of context entities in this context. If not all contexts are retrieved already
	 * it retrieves them.
	 * @return The number of context entities.
	 */
	public int getSize() {
		return contextEntities.size();
	}
	
	public String getContextType() {
		return this.contextType;
	}
	
	public String toString() {
		return contextEntities.toString();
	}
		
}