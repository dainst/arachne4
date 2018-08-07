package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_koeln.arachne.response.Dataset;


/**
 * This Class is a wrapper that holds and manages contexts of <code>ArachneDatasets</code>. The links are fetched on demand.
 * The combination of context type and parent describes the two ends of a <code>Link</code>.
 * 
 * This class is in dire need of a rewrite as the design does not really fit the backend use case. Contexts are always 
 * retrieved completely anyways. And the fact that a context needs a reference to the <code>ContextService</code> shows 
 * that the service can do the retrieval and fill the contexts. 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
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
	 * This list of {@link AbstractLink} contains the data of the context.
	 */
	protected transient final List<AbstractLink> content;
	
	/**
	 * Constructor setting up all needed fields.
	 * @param contextType The type of context this class manages.
	 * @param parent The {@link Dataset} this context belongs to.
	 * @param content A list of context entities (instances of classes derived from {@link AbstractLink}).
	 */
	public Context(final String contextType, final Dataset parent, final List<AbstractLink> content) {
		this.contextType = contextType;
		this.parent = parent;
		if (content == null) {
			this.content = new ArrayList<AbstractLink>();
		} else {
			this.content = content;
		}
		
	}
	
	// The Context Getter
	
	/**
	 * Return every <code>Link</code> in this context.
	 * @return The complete list of contexts.
	 */
	@JsonProperty("entities")
	public List<AbstractLink> getAllContexts() {
		if (content.isEmpty()) {
			return null;
		} else {
			return content;
		}
	}

	/**
	 * Returns the first link element of the context.
	 * @return The First <code>Link</code> of the context.
	 */
	@JsonIgnore
	public AbstractLink getFirstContext() {
		if (content.isEmpty()) {
			return null; 
		} else {
			return content.get(0);
		}
	}
	
	/**
	 * Returns one link element of the context.
	 * @param index The index of the link element to retrieve.
	 * @return The chosen <code>Link</code> of the context.
	 */
	public AbstractLink getContext(final int index) {
		if (!content.isEmpty() && index < content.size()) {
			return content.get(index); 
		} else {
			return null;
		}
	}
	
	/**
	 * This method returns the number of context entities in this context. If not all contexts are retrieved already
	 * it retrieves them.
	 * @return The number of context entities.
	 */
	@JsonIgnore
	public int getSize() {
		return content.size();
	}
	
	/**
	 * Getter for the context type.
	 * @return The context type.
	 */
	@JsonProperty("type")
	public String getContextType() {
		return this.contextType;
	}
	
	public String toString() {
		return content.toString();
	}
		
}