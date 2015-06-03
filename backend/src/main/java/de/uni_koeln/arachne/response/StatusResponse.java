package de.uni_koeln.arachne.response;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * This class is used as the return type of admin request.
 */
@XmlRootElement
@JsonInclude(value=Include.NON_NULL)
public class StatusResponse {
	/**
	 * The message body of the response.
	 */
	private String message;
	
	private String status;
	
	private String elapsedTime;
	
	private String estimatedTimeRemaining;
	
	private Long count;
	
	private Long indexedDocuments;
	
	private Float documentsPerSecond;
	
	private List<String> cachedDocuments;
	
	private List<String> cachedIncludeElements;
	
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
	
	/**
	 * Convenience constructor to set the message field.
	 * @param message
	 */
	public StatusResponse(final String message, final String status) {
		this.message = message;
		this.status = status;
	}
	
	// getter/setter
	
	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(String elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public Long getIndexedDocuments() {
		return indexedDocuments;
	}

	public void setIndexedDocuments(Long indexedDocuments) {
		this.indexedDocuments = indexedDocuments;
	}

	public Float getDocumentsPerSecond() {
		return documentsPerSecond;
	}

	public void setDocumentsPerSecond(Float documentsPerSecond) {
		this.documentsPerSecond = documentsPerSecond;
	}

	public String getEstimatedTimeRemaining() {
		return estimatedTimeRemaining;
	}

	public void setEstimatedTimeRemaining(String estimatedTimeRemaining) {
		this.estimatedTimeRemaining = estimatedTimeRemaining;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	/**
	 * @return the cachedDocuments
	 */
	public List<String> getCachedDocuments() {
		return cachedDocuments;
	}

	/**
	 * @param cachedDocuments the cachedDocuments to set
	 */
	public void setCachedDocuments(List<String> cachedDocuments) {
		this.cachedDocuments = cachedDocuments;
	}

	/**
	 * @return the cachedIncludeElements
	 */
	public List<String> getCachedIncludeElements() {
		return cachedIncludeElements;
	}

	/**
	 * @param cachedIncludeElements the cachedIncludeElements to set
	 */
	public void setCachedIncludeElements(List<String> cachedIncludeElements) {
		this.cachedIncludeElements = cachedIncludeElements;
	}	
}
