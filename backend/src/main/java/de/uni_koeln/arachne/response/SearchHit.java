package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Simple class to hold the data of one hit of the search result.
 */
@JsonInclude(Include.NON_NULL)
public class SearchHit {
	private transient final long entityId;
	private transient final String type;
	private transient final String title;
	private transient final String subtitle;
	private transient final Long thumbnailId;
	private transient final String place;
	private transient final String location;
	
	public SearchHit(final long entityId, final String type, final String title, final String subtitle, final Long thumbnailId
			, final String place, final String location) {
		this.entityId = entityId;
		this.type = type;
		this.title = title;
		this.subtitle = subtitle;
		this.thumbnailId = thumbnailId;
		this.place = place;
		this.location = location;
	}
	
	public long getEntityId() {
		return this.entityId;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getSubtitle() {
		return this.subtitle;
	}
	
	public Long getThumbnailId() {
		return this.thumbnailId;
	}
	
	public String getPlace() {
		return this.place;
	}
	
	public String getLocation() {
		return this.location;
	}
}
