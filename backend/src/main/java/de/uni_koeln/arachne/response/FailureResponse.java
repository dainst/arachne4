package de.uni_koeln.arachne.response;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response class to report failures.
 */
@XmlRootElement
public class FailureResponse extends BaseArachneEntity {
	/**
	 * The message to display.
	 */
	private String message = "undefined failure"; 
	
	public FailureResponse() {
		type = "Failure";	
	}
	
	public FailureResponse(final String message) {
		type = "Failure";
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}	
}
