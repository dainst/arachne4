package de.uni_koeln.arachne.controller;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.SearchResult;

/**
 * Handles http requests (currently only get) for <code>/search<code>.
 */
@Controller
public class SearchController {
	/**
	 * Handles the http request. 
	 * <br>
	 * Currently the search result can only be serialized to JSON as JAXB cannot handle Maps.
	 * @param searchParam The value of the search parameter.
     * @return A response object containing the data (this is serialized to XML or JSON depending on content negotiation).
     */
	@RequestMapping(value="/search", method=RequestMethod.GET)
	public @ResponseBody SearchResult handleSearchRequest(@RequestParam("q") String searchParam,
														  @RequestParam(value = "limit", required = false) String limit,
														  @RequestParam(value = "offset", required = false) String offset,
														  @RequestParam(value = "fq", required = false) String facetValues) {
		//SearchResult result = new SearchResult();
		SearchResult result = new SearchResult();
		String url = "http://crazyhorse.archaeologie.uni-koeln.de:8080/solr3.4.0/";
		SolrServer server = null;
		try {
			server = new CommonsHttpSolrServer(url);
			SolrQuery query = new SolrQuery();
		    query.setQuery(searchParam);
		    query.setRows(1000);
		    query.addFacetField("facet_kategorie");
		    // TODO add more facets based on info from where?
		    query.setFacet(true);
		    QueryResponse response = server.query(query);
		    //result.header = rsp.getHeader();
		    //result.facets = rsp.getFacetFields();
		    System.out.println("Facets: " + response.getFacetFields());
		    result.setEntities(response.getResults());
		    result.setSize(result.getEntities().size());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			    
	    return result;
	}
}
