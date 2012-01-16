package de.uni_koeln.arachne.response;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.solr.common.SolrDocumentList;

// TODO implement me
/**
 * Class representing a search result. This class is the return type of a search request.
 */
@XmlRootElement
public class SearchResult {
	private int size = 0;
	private int limit = 0;
	private int offset = 0;
	private Map<String, Map<String, String>> facets = null;
	private SolrDocumentList entities = null;
	
	// getter/setter
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public Map<String, Map<String, String>> getFacets() {
		return facets;
	}
	public void setFacets(Map<String, Map<String, String>> facets) {
		this.facets = facets;
	}
	public SolrDocumentList getEntities() {
		return entities;
	}
	public void setEntities(SolrDocumentList entities) {
		this.entities = entities;
	} 
}
