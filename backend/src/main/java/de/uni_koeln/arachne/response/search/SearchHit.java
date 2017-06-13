package de.uni_koeln.arachne.response.search;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uni_koeln.arachne.response.Place;

/**
 * Simple class to hold the data of one hit of the search result.
 */
@JsonInclude(Include.NON_EMPTY)
public class SearchHit {
	private transient final long entityId;
	private transient final String type;
	@JsonProperty("@id")
	private transient final String uri;
	private transient final String title;
	private transient final String subtitle;
	private transient final Long thumbnailId;
	private transient final List<Place> places;
	private transient final Map<String, List<String>> highlights;
	@JsonIgnore
	private transient final Map<String, Object> source;
	
		
	public SearchHit(final long entityId, final String type, final String uri, final String title
			, final String subtitle, final Long thumbnailId, final List<Place> places
			, final Map<String, List<String>> highlights, final Map<String, Object> source) {
		this.entityId = entityId;
		this.type = type;
		this.uri = uri;
		this.title = title;
		this.subtitle = subtitle;
		this.thumbnailId = thumbnailId;
		this.places = places;
		this.highlights = highlights;
		this.source = source;
	}
		
	public long getEntityId() {
		return this.entityId;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getUri() {
		return this.uri;
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
	
	public List<Place> getPlaces() {
		return places;
	}
	
	public Map<String, List<String>> getHighlights() {
		return highlights;
	}

	public Map<String, Object> getSource() { return source;	}

}
