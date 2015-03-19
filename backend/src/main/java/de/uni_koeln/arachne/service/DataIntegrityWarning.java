package de.uni_koeln.arachne.service;

/**
 * Class to hold data integrity warning information.
 * A summary of the warnings is attached to every dataimport result mail.
 * The information contained in an instance of this class is meant for 'Bearbeiter' so try to make is as easy to understand 
 * as possible. Good practice is to use internal keys as 'identifiers' instead of entity ids.
 * @author Reimar Grabowski	
 *
 */
public class DataIntegrityWarning {
	/**
	 * The id that generated the warning. Most of the time this is a db table id.
	 */
	private long identifier;
	
	/**
	 * A <code>String</code> specifiying in a human readable form what kind of id this is (for example "PS_MARBilderID").
	 */
	private String identifierType;
	
	/**
	 * A message specifying the problem.
	 */
	private String message;
	
	/**
	 * Convenience constructor to create a filled instance.
	 */
	public DataIntegrityWarning(final long entityId, final String identifierType, final String message) {
		this.identifier = entityId;
		this.identifierType = identifierType;
		this.message = message;
	}

	/**
	 * @return the entityId
	 */
	public long getIdentifier() {
		return identifier;
	}

	/**
	 * @param entityId the entityId to set
	 */
	public void setIdentifier(final long identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(final String message) {
		this.message = message;
	}

	/**
	 * @return the identifierType
	 */
	public String getIdentifierType() {
		return identifierType;
	}

	/**
	 * @param identifierType the identifierType to set
	 */
	public void setIdentifierType(final String identifierType) {
		this.identifierType = identifierType;
	}
}
