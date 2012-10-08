package de.uni_koeln.arachne.response;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *  Class that holds and ouputs a HTML formatted limk.
 */
public class LinkField extends Field {
	private transient String labelKey;
	
	/**
	 * Convenience constructor to initialize <code>labelKey</code> at construction time.
	 * @param labelKey The String to display as link text.
	 */
	public LinkField(final String labelKey) {
		this.labelKey = labelKey;
	}
	
	@JsonIgnore
	public String getLabelKey() {
		return this.labelKey;
	}
	
	public void setLabelKey(final String labelKey) {
		this.labelKey = labelKey;
	}
	
	/**
	 * Returns a HTML tag that is created from <code>value</code> and <code>labelKey</code>.
	 */
	@Override
	public String getValue() {
		return "<a href=\"" + this.value + "\">" + this.labelKey + "</a>";
	}
	
	@Override
	public String toString() {
		return getValue();
	}
}
