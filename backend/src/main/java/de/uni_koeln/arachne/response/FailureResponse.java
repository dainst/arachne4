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
	
	public FailureResponse(String message) {
		type = "Failure";
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}	
}
