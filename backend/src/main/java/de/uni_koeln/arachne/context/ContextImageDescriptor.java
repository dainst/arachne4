package de.uni_koeln.arachne.context;

/**
 * Instances of this class describe the usage / retrieval of context-images. Every instance contains the name of the 
 * context used and an additional descriptor which states, how the context-images are handled with regards to the dataset 
 * @author Patrick Gunia
 *
 */

public class ContextImageDescriptor {

	/** Name of the context */
	private transient final String contextName;
	
	/** Usage of the additionally retrieved images */
	private transient final String contextImageUsage;

	/**
	 * Constructor setting all fields.
	 * @param contextName The name of the context.
	 * @param contextImageUsage The image usage.
	 */
	public ContextImageDescriptor(final String contextName, final String contextImageUsage) {
		super();
		this.contextName = contextName;
		this.contextImageUsage = contextImageUsage;
	}

	/**
	 * Getter for the context name.
	 * @return The context name.
	 */
	public String getContextName() {
		return contextName;
	}

	/**
	 * Getter for the image usage.
	 * @return The image usage.
	 */
	public String getContextImageUsage() {
		return contextImageUsage;
	}

	@Override
	public String toString() {
		return "ContextImageDescriptor [contextName=" + contextName
				+ ", contextImageUsage=" + contextImageUsage + "]";
	}

	
	
}
