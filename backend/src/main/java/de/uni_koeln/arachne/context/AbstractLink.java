package de.uni_koeln.arachne.context;

import java.util.Map;

// TODO reevaluate this class regarding the fields map
/**
 * Base class for holding links between URIs and additional information.
 *
 */
public abstract class AbstractLink {
	// TODO add correct documentation
	/**
	 * Holds the linkType as defined in ???.
	 */
	protected String linkType;
	
	/**
	 * The map holding the additional information.
	 */
	protected Map<String, String> fields;
	
	/**
	 * Function to retrieve the URI of the left side of the link.
	 */
	abstract String getUri1();	
	
	/**
	 * Function to retrieve the URI of the right side of the link.
	 */
	abstract String getUri2();
	
	public String getLinkType() {
		return linkType;
	}
	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}
	public Map<String, String> getFields() {
		return fields;
	}
	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}
}