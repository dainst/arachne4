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
	 * This merges another <code>SearchResult</code> with this instance. Entities are added up to the <code>limit</code> parameter. Facets are added.
	 * The size is summed, </code>limit</code> and <code>offset</code> are set by the appended <code>SearchResult</code>.
	 * @param esSearchResult
	 */
	public void merge(final SearchResult esSearchResult) {
		if (entities == null) {
			entities = new ArrayList<SearchHit>();
		}
		
		for (final SearchHit entity: esSearchResult.entities) {
			if (entities.size() < limit) {
				entities.add(entity);
			}
		}
		
		if (facets == null) {
			facets = esSearchResult.facets;
		} else {
			for (final Map.Entry<String, Map<String, Long>> entry: esSearchResult.facets.entrySet()) {
				if (facets.containsKey(entry.getKey())) {
					for (final Map.Entry<String, Long> facetEntry: entry.getValue().entrySet()) {
						if (facets.get(entry.getKey()).get(facetEntry.getKey()) == null) {
							facets.get(entry.getKey()).put(facetEntry.getKey(), facetEntry.getValue());
						} else {
							final long newValue = facets.get(entry.getKey()).get(facetEntry.getKey()) + facetEntry.getValue();
							facets.get(entry.getKey()).put(facetEntry.getKey(), newValue);
						}
					}
				}
			}
		}
		
		size = size + esSearchResult.size;
		
		limit = esSearchResult.limit;
		offset = esSearchResult.offset;
	} 
}
