package de.uni_koeln.arachne.util;

import org.springframework.http.HttpStatus;

/**
 * Simple class that holds a String and a status. Used to allow methods to return a string and an HTTP status code; 
 * 
 * @author Reimar Grabowski
 */
public class StringWithHTTPStatus {

	private String value;
	
	private HttpStatus status;
	
	/**
	 * Convenience constructor to only set the value setting the status code to 200.
	 * @param value The string value.
	 */
	public StringWithHTTPStatus(final String value) {
		this.value = value;
		status = HttpStatus.OK;
	}

	/**
	 * Convenience constructor to only set the status code setting the value to <code>null</code>.
	 * @param value The string value.
	 */
	public StringWithHTTPStatus(final HttpStatus status) {
		this.value = null;
		this.status = status;
	}
	
	/**
	 * Convenience constructor initializing the instance with a value and a status code.
	 * @param value The string value.
	 */
	public StringWithHTTPStatus(final String value, final HttpStatus status) {
		this.value = value;
		this.status = status;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the status
	 */
	public HttpStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(HttpStatus status) {
		this.status = status;
	}
}
