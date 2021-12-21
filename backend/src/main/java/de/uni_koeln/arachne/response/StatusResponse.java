package de.uni_koeln.arachne.response;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * This class is used as the return type of admin request.
 * 
 * @author Reimar Grabowski
 */
@XmlRootElement
@JsonInclude(value=Include.NON_EMPTY)
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
	 * @param message The status response message.
	 */
	public StatusResponse(final String message) {
		this.message = message;
	}
	
	/**
	 * Convenience constructor to set the message field.
	 * @param message The status response message.
	 * @param status A status desciption.
	 */
	public StatusResponse(final String message, final String status) {
		this.message = message;
		this.status = status;
	}
	
	/**
	 * Getter for the message field.	
	 * @return The message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Setter for the message field.
	 * @param message The message.
	 */
	public void setMessage(final String message) {
		this.message = message;
	}

	/**
	 * Getter for the status field.
	 * @return The status.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Setter for the status field.
	 * @param status The status.
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Getter for the elapsed time.
	 * @return The elapsed time
	 */
	public String getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * Setter for the elapsed time.
	 * @param elapsedTime The elapsed time
	 */
	public void setElapsedTime(String elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	/**
	 * Getter for the indexed documents.
	 * @return The number of indexed documents.
	 */
	public Long getIndexedDocuments() {
		return indexedDocuments;
	}

	/**
	 * Setter for the indexed documents.
	 * @param indexedDocuments The number of indexed documents.
	 */
	public void setIndexedDocuments(Long indexedDocuments) {
		this.indexedDocuments = indexedDocuments;
	}

	/**
	 * Getter for documents per second.
	 * @return The documents per second.
	 */
	public Float getDocumentsPerSecond() {
		return documentsPerSecond;
	}

	/**
	 * Setter for the documents per second.
	 * @param documentsPerSecond The documents per second.
	 */
	public void setDocumentsPerSecond(Float documentsPerSecond) {
		this.documentsPerSecond = documentsPerSecond;
	}

	/**
	 * Getter for the estimated time remining.
	 * @return The estimated time remaining.
	 */
	public String getEstimatedTimeRemaining() {
		return estimatedTimeRemaining;
	}

	/**
	 * Setter for the estimated time remaining.
	 * @param estimatedTimeRemaining The estimated time remaining.
	 */
	public void setEstimatedTimeRemaining(String estimatedTimeRemaining) {
		this.estimatedTimeRemaining = estimatedTimeRemaining;
	}

	/**
	 * Getter for the document count.
	 * @return The document count.
	 */
	public Long getCount() {
		return count;
	}

	/**
	 * Setter for the document count.
	 * @param count The document count.
	 */
	public void setCount(Long count) {
		this.count = count;
	}

	/**
	 * Getter for the cached documents.
	 * @return A list of cached document names.
	 */
	public List<String> getCachedDocuments() {
		return cachedDocuments;
	}

	/**
	 * Setter for the cached documents.
	 * @param cachedDocuments A list of document names.
	 */
	public void setCachedDocuments(List<String> cachedDocuments) {
		this.cachedDocuments = cachedDocuments;
	}

	/**
	 * Getter for the cached include elements.
	 * @return A list of cached include document names.
	 */
	public List<String> getCachedIncludeElements() {
		return cachedIncludeElements;
	}

	/**
	 * Setter for the cached include documents
	 * @param cachedIncludeElements A list of include document names. 
	 */
	public void setCachedIncludeElements(List<String> cachedIncludeElements) {
		this.cachedIncludeElements = cachedIncludeElements;
	}	
}
