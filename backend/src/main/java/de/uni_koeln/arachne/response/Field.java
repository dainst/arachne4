package de.uni_koeln.arachne.response;

/**
 * Class to hold a single <code>String</code> value.
 */
public class Field extends Content {
	/**
	 * The <code>String</code> value the class holds.
	 */
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString() {
		return getValue();
	}
	
}
