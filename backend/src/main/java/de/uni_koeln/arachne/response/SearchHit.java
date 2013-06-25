package de.uni_koeln.arachne.response;

/**
 * Simple class to hold the data of one hit of the search result.
 */
public class SearchHit {
	private transient final long entityId;
	private transient final String type;
	private transient final String title;
	private transient final String subtitle;
	private transient final long thumbnailId;
	
	public SearchHit(final long entityId, final String type, final String title, final String subtitle, final long thumbnailId) {
		this.entityId = entityId;
		this.type = type;
		this.title = title;
		this.subtitle = subtitle;
		this.thumbnailId = thumbnailId;
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
	
	public long getThumbnailId() {
		return this.thumbnailId;
	}
}
