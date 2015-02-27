package de.uni_koeln.arachne.response.search;

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
	private transient final String findSpot;
	private transient final String findSpotLocation;
	private transient final String depository;
	private transient final String depositoryLocation;
	
	public SearchHit(final long entityId, final String type, final String title, final String subtitle, final Long thumbnailId
			, final String findSpot, final String findSpotLocation, final String depository, final String depositoryLocation) {
		this.entityId = entityId;
		this.type = type;
		this.title = title;
		this.subtitle = subtitle;
		this.thumbnailId = thumbnailId;
		this.findSpot = findSpot;
		this.findSpotLocation = findSpotLocation;
		this.depository = depository;
		this.depositoryLocation = depositoryLocation;
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
	
	public String getFindSpot() {
		return this.findSpot;
	}
	
	public String getFindSpotLocation() {
		return this.findSpotLocation;
	}
	
	public String getDepository() {
		return this.depository;
	}
	
	public String getDepositoryLocation() {
		return this.depositoryLocation;
	}
}
