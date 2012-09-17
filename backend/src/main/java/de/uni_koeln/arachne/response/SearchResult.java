package de.uni_koeln.arachne.response;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.solr.common.SolrDocumentList;

/**
 * Class representing a search result. This class is the return type of a search request.
 */
@XmlRootElement
public class SearchResult {
	private long size = 0;
	private int limit = 0;
	private int offset = 0;
	private Map<String, Map<String, Long>> facets = null;
	private SolrDocumentList entities = null;
	
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
	public SolrDocumentList getEntities() {
		return entities;
	}
	public void setEntities(final SolrDocumentList entities) {
		this.entities = entities;
	} 
}
