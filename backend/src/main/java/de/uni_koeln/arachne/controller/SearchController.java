package de.uni_koeln.arachne.controller;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.SearchResult;
import de.uni_koeln.arachne.util.SolrUrlString;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * Handles http requests (currently only get) for <code>/search<code>.
 */
@Controller
public class SearchController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
	
	private transient final SolrServer server;
	
	@Autowired
	public SearchController(final SolrUrlString solrUrl) {
		SolrServer server = null;
		try {
			server = new CommonsHttpSolrServer(solrUrl.getSolrUrl());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOGGER.error(e.getMessage());
		}
		this.server = server;
	}
	
	/**
	 * Handles the http request by querying the Solr index and returning the search result.
	 * <br>
	 * Currently the search result can only be serialized to JSON as JAXB cannot handle Maps.
	 * @param searchParam The value of the search parameter. (mandatory)
	 * @param limit The maximum number of returned entities. (optional)
	 * @param offset The offset into the list of entities (used for paging). (optional)
	 * @param filterValues The values of the solr filter query. (optional)
	 * @param facetLimit The maximum number of facet results. (optional)
	 * @return A response object containing the data (this is serialized to XML or JSON depending on content negotiation).
	 */
	@RequestMapping(value="/search", method=RequestMethod.GET)
	public @ResponseBody SearchResult handleSearchRequest(@RequestParam("q") final String searchParam,
														  @RequestParam(value = "limit", required = false) final String limit,
														  @RequestParam(value = "offset", required = false) final String offset,
														  @RequestParam(value = "fq", required = false) final String filterValues,
														  @RequestParam(value = "fl", required = false) final String facetLimit) {
		
		final SearchResult result = new SearchResult();
		try {
			final SolrQuery query = new SolrQuery("*:*");
		    query.setQuery(searchParam);
		    // default value for limit
		    query.setRows(50);
		    query.setFacetMinCount(1);
		    // default facets to include
		    query.addFacetField("facet_kategorie");
		    query.addFacetField("facet_ort");
		    query.addFacetField("facet_datierung-epoche");
		    // TODO add category specific facets based on info from where?
		    query.setFacet(true);
		    		    		    
		    setSearchParameters(limit, offset, filterValues, facetLimit,
					result, query);
		    		    
		    final QueryResponse response = server.query(query);
		    result.setEntities(response.getResults());
		    result.setSize(response.getResults().getNumFound());
		    final Map<String, Map<String, Long>> facets = new LinkedHashMap<String, Map<String, Long>>();
		    
		    final List<FacetField> facetFields = response.getFacetFields();
		    for (FacetField facetField: facetFields) {
		    	final List<FacetField.Count> facetItems = facetField.getValues();
		    	final Map<String, Long> facetValueMap = new LinkedHashMap<String, Long>();
		    	if (facetItems != null) {
		    		for (FacetField.Count fcount: facetItems) {
		    			facetValueMap.put(fcount.getName(), fcount.getCount());
		    		}
		    		if (!facetValueMap.isEmpty()) {
		    			facets.put(facetField.getName(), facetValueMap);
		    		}
		    	}
		    }
		    
		    if (!facets.isEmpty()) {
		    	result.setFacets(facets);
		    }
		    
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			LOGGER.error(e.getMessage());
		}
			    
	    return result;
	}

	/**
	 * Helper function to set the search parameters for the Solr query. The first four parameters are the same as used
	 *  in handle search request.
	 *
	 * @param result The query result.
	 * @param query The query to set the parameters on.
	 */
	private void setSearchParameters(final String limit, final String offset,
			final String filterValues, final String facetLimit,
			final SearchResult result, final SolrQuery query) {
		
		if (!StrUtils.isEmptyOrNull(offset)) {
			final int intOffset = Integer.valueOf(offset);
			query.setStart(intOffset);
			result.setOffset(intOffset);
		}
		
		if (!StrUtils.isEmptyOrNull(limit)) {
			final int intLimit = Integer.valueOf(limit);
			query.setRows(intLimit);
			result.setLimit(intLimit);
		}
		
		if (!StrUtils.isEmptyOrNull(facetLimit)) {
			final int intFacetLimit = Integer.valueOf(facetLimit);
			query.setFacetLimit(intFacetLimit);
		}
		
		if (!StrUtils.isEmptyOrNull(filterValues)) {
			final List<String> filterValueList = filterQueryStringToStringList(filterValues); 
			if (!StrUtils.isEmptyOrNull(filterValueList)) {
				for (String filterValue: filterValueList) {
					query.addFilterQuery(filterValue);
				}
			}
		}
	}
	
	/**
	 * Converts the input string of query filter parameters to a string list of parameters.
	 * The string is split at every occurrence of ",facet_".
	 * @param filterString The filter query string to convert.
	 * @return a string list containing the separated parameters or <code>null</code> if the conversion fails.
	 */
	private List<String> filterQueryStringToStringList(final String filterString) {
		String string = filterString;
		if (string.startsWith("facet_")) {
			final List<String> result = new ArrayList<String>();
			int index = string.indexOf(",facet_");
			while (index != -1) {
				final String subString = string.substring(0, index);
				string = string.substring(index + 1);
				index = string.indexOf(",facet_");
				result.add(subString);
			}
			result.add(string);
			return result;
		} else {
			return null;
		}
	}
}
