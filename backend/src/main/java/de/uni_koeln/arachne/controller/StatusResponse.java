package de.uni_koeln.arachne.controller;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is used as the return type of admin request.
 */
@XmlRootElement
public class StatusResponse {
	/**
	 * The message body of the response.
	 */
	private String message;
	
	/**
	 * Empty default constructor.
	 */
	public StatusResponse() {
		// just to make JAXB happy.
	}
	
	/**
	 * Convenience constructor to set the message field.
	 * @param message
	 */
	public StatusResponse(final String message) {
		this.message = message;
	}
	
	// getter/setter
	
	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}
	
}
