package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * This class is the standard container for 3d models. It is derived from <code>SQLResponseObject</code> so that a
 * specialized <code>RowMapper</code> can be used for retrieval from the database.
 */
@JsonInclude(Include.NON_EMPTY)
public class Model {
	/**
	 * The primary key of the model.
	 */
	protected Long modelId = null;
	
	protected String title = null;

    protected String fileName = null;

    /**
     * modell3d.PS_Modell3dID
     */
    protected Long internalId = null;	
	
	/**
	 * The entity-ID of the record, the model is assigned to
	 */
	protected Long sourceRecordId;
	
	/**
	 * The category of the record, the model is assigned to
	 */
	protected String sourceContext;
	

	public Long getModelId() {
		return modelId;
	}

	public void setModelId(final Long modelId) {
		this.modelId = modelId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getInternalId() {
        return internalId;
    }

    public void setInternalId(Long internalId) {
        this.internalId = internalId;
    }

	public Long getSourceRecordId() {
		return sourceRecordId;
	}

	public void setSourceRecordId(final Long sourceRecordId) {
		this.sourceRecordId = sourceRecordId;
	}

	public String getSourceContext() {
		return sourceContext;
	}

	public void setSourceContext(final String sourceContext) {
		this.sourceContext = sourceContext;
	}

	@Override
	public String toString() {
		return "Image [modelId=" + modelId + ", title=" + title
				+  ", sourceRecordId="
				+ sourceRecordId + ", sourceContext="
				+ sourceContext + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((modelId == null) ? 0 : modelId.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((sourceContext == null) ? 0 : sourceContext.hashCode());
		result = prime * result + ((sourceRecordId == null) ? 0 : sourceRecordId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Image other = (Image) obj;
		if (modelId == null) {
			if (other.imageId != null)
				return false;
		} else if (!modelId.equals(other.imageId))
			return false;
		if (title == null) {
			if (other.imageSubtitle != null)
				return false;
		} else if (!title.equals(other.imageSubtitle))
			return false;
		if (sourceContext == null) {
			if (other.sourceContext != null)
				return false;
		} else if (!sourceContext.equals(other.sourceContext))
			return false;
		if (sourceRecordId == null) {
			if (other.sourceRecordId != null)
				return false;
		} else if (!sourceRecordId.equals(other.sourceRecordId))
			return false;
		return true;
	}
}
