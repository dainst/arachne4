package de.uni_koeln.arachne.response;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BaseArachneEntity {
	/**
	 * Identification of the Dataset
	 */
	protected Long entityId = -1L;
	
	/**
	 * The tablename field of the ArachneEntity table
	 */
	protected String type = null;
	
	/**
	 * The foreignKey field of the ArachneEntity table
	 */
	protected Long internalId = -1L;

	/**
	 * The group of the dataset used for rights management 
	 */
	protected String datasetGroup = null;
	
	/**
	 * Field for additional content of any type.
	 */
	protected AdditionalContent additionalContent = null;
	
	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(final Long entiyId) {
		this.entityId = entiyId;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Long getInternalId() {
		return internalId;
	}

	public void setInternalId(final Long internalId) {
		this.internalId = internalId;
	}

	public String getDatasetGroup() {
		return datasetGroup;
	}

	public void setDatasetGroup(final String datasetGroup) {
		this.datasetGroup = datasetGroup;
	}
	
	public AdditionalContent getAdditionalContent() {
		return additionalContent;
	}
	
	public void setAdditionalContent(final AdditionalContent additionalContent) {
		this.additionalContent = additionalContent;
	}
}
