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

	private long size = 0;
	private int limit = 0;
	private int offset = 0;
	private Map<String, Map<String, Long>> facets = null;
	private List<SearchHit> entities = null;

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

	/**
	 * This merges another <code>SearchResult</code> with this instance. Entities are added. Facets are ignored.
	 * The size is summed, </code>limit</code> and <code>offset</code> are set by the imported <code>SearchResult</code>.
	 * @param esSearchResult
	 */
	public void merge(final SearchResult esSearchResult) {
		if (this.entities == null) {
			this.entities = new ArrayList<SearchHit>();
		}
		this.entities.addAll(esSearchResult.entities);
		
		this.size = this.size + esSearchResult.size;
		
		this.limit = esSearchResult.limit;
		this.offset = esSearchResult.offset;
	} 
}
