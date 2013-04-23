package de.uni_koeln.arachne.context;

/**
 * Instances of this class describe the usage / retrieval of context-images. Every instance contains the name of the 
 * context used and an additional descriptor which states, how the context-images are handled with regards to the dataset 
 * @author Patrick Gunia
 *
 */

public class ContextImageDescriptor {

	/** Name of the context */
	private String contextName = null;
	
	/** Usage of the additionally retrieved images */
	private String contextImageUsage = null;

	public ContextImageDescriptor(final String contextName, final String contextImageUsage) {
		super();
		this.contextName = contextName;
		this.contextImageUsage = contextImageUsage;
	}

	public String getContextName() {
		return contextName;
	}

	public String getContextImageUsage() {
		return contextImageUsage;
	}

	@Override
	public String toString() {
		return "ContextImageDescriptor [contextName=" + contextName
				+ ", contextImageUsage=" + contextImageUsage + "]";
	}

	
	
}
