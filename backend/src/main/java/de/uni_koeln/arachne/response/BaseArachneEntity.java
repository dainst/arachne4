package de.uni_koeln.arachne.response;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base class for arachne entities.
 * 
 * @author Reimar Grabowski
 *
 */
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
	
	/**
	 * Constructor setting the type. This is the only way to set the type of an entity to ensure that there are no 
	 * entities created without a type.
	 * @param type The type of the entity.
	 */
	public BaseArachneEntity(final String type) {
		this.type = type;
	}
	
	/**
	 * Entity id getter.
	 * @return The entity id.
	 */
	public Long getEntityId() {
		return entityId;
	}

	/**
	 * Entity id setter.
	 * @param entiyId An entity id.
	 */
	public void setEntityId(final Long entiyId) {
		this.entityId = entiyId;
	}

	/**
	 * Type getter.
	 * @return The type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Internal id getter.
	 * @return The internal id.
	 */
	public Long getInternalId() {
		return internalId;
	}

	/**
	 * Internal id setter.
	 * @param internalId An internal id.
	 */
	public void setInternalId(final Long internalId) {
		this.internalId = internalId;
	}

	/**
	 * Dataset group getter.
	 * @return The dataset group.
	 */
	public String getDatasetGroup() {
		return datasetGroup;
	}

	/**
	 * Dataset group setter.
	 * @param datasetGroup A dataset group.
	 */
	public void setDatasetGroup(final String datasetGroup) {
		this.datasetGroup = datasetGroup;
	}
	
	/**
	 * Additonal content getter.
	 * @return The additional content.
	 */
	public AdditionalContent getAdditionalContent() {
		return additionalContent;
	}
	
	/**
	 * Additional content setter.
	 * @param additionalContent Some additional content.
	 */
	public void setAdditionalContent(final AdditionalContent additionalContent) {
		this.additionalContent = additionalContent;
	}
}
