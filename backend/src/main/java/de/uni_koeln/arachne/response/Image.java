package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * This class is the standard container for images. It is derived from <code>SQLResponseObject</code> so that a
 * specialized <code>RowMapper</code> can be used for retrieval from the database.
 */
@JsonInclude(Include.NON_EMPTY)
public class Image {
	/**
	 * The primary key of the image.
	 */
	protected Long imageId = null;
	
	/**
	 * The imageSubtitle of the image.
	 */
	protected String imageSubtitle = null;
	
	
	/**
	 * The entity-ID of the record, the image is assigned to
	 */
	protected Long sourceRecordId;
	
	/**
	 * The category of the record, the image is assigned to
	 */
	protected String sourceContext;
	

	public Long getImageId() {
		return imageId;
	}

	public void setImageId(final Long imageId) {
		this.imageId = imageId;
	}

	public String getImageSubtitle() {
		return imageSubtitle;
	}

	public void setImageSubtitle(final String imageSubtitle) {
		this.imageSubtitle = imageSubtitle;
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
		return "Image [imageId=" + imageId + ", imageSubtitle=" + imageSubtitle
				+  ", sourceRecordId="
				+ sourceRecordId + ", sourceContext="
				+ sourceContext + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((imageId == null) ? 0 : imageId.hashCode());
		result = prime * result + ((imageSubtitle == null) ? 0 : imageSubtitle.hashCode());
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
		if (imageId == null) {
			if (other.imageId != null)
				return false;
		} else if (!imageId.equals(other.imageId))
			return false;
		if (imageSubtitle == null) {
			if (other.imageSubtitle != null)
				return false;
		} else if (!imageSubtitle.equals(other.imageSubtitle))
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