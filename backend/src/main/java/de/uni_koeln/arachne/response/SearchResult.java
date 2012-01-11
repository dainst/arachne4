package de.uni_koeln.arachne.response;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;

// TODO implement me
/**
 * Class representing a search result. This class is the return type of a search request.
 */
@XmlRootElement
public class SearchResult {
	public NamedList<Object> header = null;
	public List<FacetField> facets = null;
	public SolrDocumentList doc = null; 
}
