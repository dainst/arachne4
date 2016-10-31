package de.uni_koeln.arachne.util;

import org.springframework.http.HttpStatus;

/**
 * Simple class that holds a class instance of any type and an HTTP status. Used to allow methods to return any value 
 * and an HTTP status code; 
 * @param <T> The type of the value. 
 * @author Reimar Grabowski
 */
public class TypeWithHTTPStatus<T> {

	private T value;
	
	private HttpStatus status;
	
	/**
	 * Convenience constructor to only set the value setting the status code to 200.
	 * @param value The string value.
	 */
	public TypeWithHTTPStatus(final T value) {
		this.value = value;
		status = HttpStatus.OK;
	}

	/**
	 * Convenience constructor to only set the status code setting the value to <code>null</code>.
	 * @param status The HTTP status.
	 */
	public TypeWithHTTPStatus(final HttpStatus status) {
		this.value = null;
		this.status = status;
	}
	
	/**
	 * Convenience constructor initializing the instance with a value and a status code.
	 * @param value The string value.
	 * @param status The HTTP status.
	 */
	public TypeWithHTTPStatus(final T value, final HttpStatus status) {
		this.value = value;
		this.status = status;
	}

	/**
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(T value) {
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
