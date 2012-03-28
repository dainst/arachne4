package de.uni_koeln.arachne.response;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class to hold a single <code>String</code> value.
 */
@XmlRootElement
public class Field extends AbstractContent {
	/**
	 * The <code>String</code> value the class holds.
	 */
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}
	
	public String toString() {
		return getValue();
	}	
}
