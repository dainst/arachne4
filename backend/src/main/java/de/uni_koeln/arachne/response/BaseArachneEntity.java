package de.uni_koeln.arachne.response;

public class BaseArachneEntity {
	/**
	 * Identification of the Dataset
	 */
	protected Long id;
	
	/**
	 * The tablename field of the ArachneEntity table
	 */
	protected String type;
	
	/**
	 * The foreignKey field of the ArachneEntity table
	 */
	protected Long internalId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getInternalId() {
		return internalId;
	}

	public void setInternalId(Long internalId) {
		this.internalId = internalId;
	}
}
