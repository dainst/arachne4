package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import de.uni_koeln.arachne.service.SQLResponseObject;

/**
 * This class is the standard container for images. It is derived from <code>SQLResponseObject</code> so that a
 * specialized <code>RowMapper</code> can be used for retrieval from the database.
 */
@JsonInclude(Include.NON_EMPTY)
public class Image extends SQLResponseObject {
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

	
	
}