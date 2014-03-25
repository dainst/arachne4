package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Class representing a search result. This class is the return type of a search request.
 */
@XmlRootElement
@JsonInclude(Include.NON_NULL)
public class SearchResult {

	private long size;
	private int limit;
	private int offset;
	private Map<String, Map<String, Long>> facets;
	private List<SearchHit> entities;

	public void addSearchHit(final SearchHit searchHit) {
		if (searchHit == null) {
			return;
		}
		if (entities == null) {
			entities = new ArrayList<SearchHit>();
		}
		entities.add(searchHit);
	}
	
	// getter/setter

	public long getSize() {
		return size;
	}
	public void setSize(final long size) {
		this.size = size;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(final int limit) {
		this.limit = limit;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(final int offset) {
		this.offset = offset;
	}
	public Map<String, Map<String, Long>> getFacets() {
		return facets;
	}
	public void setFacets(final Map<String, Map<String, Long>> facets) {
		this.facets = facets;
	}
	public List<SearchHit> getEntities() {
		return entities;
	}
	public void setEntities(final List<SearchHit> entities) {
		this.entities = entities;
	}

}
