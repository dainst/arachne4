package de.uni_koeln.arachne.responseobjects;

/**
 * Class to hold a single <code>String</code>.
 */
public class Field extends Content {
	/**
	 * The value the class holds.
	 */
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
