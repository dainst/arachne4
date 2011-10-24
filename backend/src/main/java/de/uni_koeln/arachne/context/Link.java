package de.uni_koeln.arachne.context;

import java.util.Map;

/**
 * Base class for holding links between URIs and additional information.
 *
 */
public abstract class Link {
	// TODO add correct documentation
	/**
	 * Holds the linkType as defined in ???.
	 */
	protected String linkType;
	
	/**
	 * The map holding the additional information.
	 */
	protected Map<String, String> fields;
	
	// TODO add missing documentation
	/**
	 * 
	 */
	abstract String getUri1();	
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
