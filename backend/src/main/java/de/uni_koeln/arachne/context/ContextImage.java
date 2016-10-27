package de.uni_koeln.arachne.context;

import de.uni_koeln.arachne.response.Dataset;

/**
 * This class is used to retrieve images from contexts. These may be added to a dataset additionally or only if the record
 * itself doesn´t contain any images at all. 
 * @author Patrick Gunia
 *
 */

public class ContextImage extends Context {

	/** Describes the usage of images found in the context of the current record */
	private transient final String usage;
	
	/**
	 * Constructor to set all fields.
	 * @param contextType The type of the context.
	 * @param usage The usage.
	 * @param parent The parent {@link Dataset} 
	 */
	public ContextImage(final String contextType, final String usage, final Dataset parent) {
		super(contextType, parent, null);
		this.usage = usage;
	}

	/**
	 * Getter for usage.
	 * @return The usage.
	 */
	public String getUsage() {
		return usage;
	}
	
	
}
