package de.uni_koeln.arachne.controller;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.response.SearchResult;
import de.uni_koeln.arachne.service.GenericSQLService;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * Handles http requests (currently only get) for <code>/search<code>.
 */
@Controller
public class SearchController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
	
	@Autowired
	private transient GenericSQLService genericSQLService; 
	
	@Autowired
	private transient IUserRightsService userRightsService; 
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	private transient final SolrServer server;
	
	@Autowired
	public SearchController(final @Value("#{config.solrProtocol}") String solrPotocol, final @Value("#{config.solrIp}") String solrIp,
			final @Value("#{config.solrPort}") int solrPort, final @Value("#{config.solrName}") String solrName) {
		
		SolrServer server = null;
		try {
			final String solrUrl = solrPotocol+"://"+solrIp+':'+solrPort+'/'+solrName;
			LOGGER.info("SolrUrl: " + solrUrl);
			if (StrUtils.isValidIPAddress(solrIp)) {
				server = new HttpSolrServer(solrUrl);
			} else {
				throw new MalformedURLException("solrIp " + solrIp + " is not a valid IP address.");
			}
		} catch (MalformedURLException e) {
			LOGGER.error("Setting up SolrServer: " + e.getMessage());
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
			final StringBuffer fullSearchParam = new StringBuffer(64);
			fullSearchParam.append('(');
			fullSearchParam.append(searchParam);
			appendAccessControl(fullSearchParam);
			LOGGER.debug("fullSearchParam: " + fullSearchParam);
			
			final SolrQuery query = getQueryWithDefaults(fullSearchParam.toString());
			
			setSearchParameters(limit, offset, filterValues, facetLimit, result, query);
		    
		    executeAndProcessQuery(result, query, METHOD.POST);
		    
		} catch (SolrServerException e) {
			LOGGER.error(e.getMessage());
		}
			    
	    return result;
	}

	/**
	 * Handles the http request by querying the Solr index for contexts of a given entity and returning the result.
	 * <br>
	 * Since the queries can get quite large the HTTP POST method is used instead of GET to submit the query to Solr.
	 * Nonetheless the query may fail with <code>Bad request</code>. This indicates that the maximum number of boolean
	 * clauses is reached (although Solr should throw a <code>maxBoolean</code> exception it does not). The only way to 
	 * solve this problem is to increase the number of allowed boolean clauses in <code>solrconfig.xml</code>.
	 * <br> 
	 * Currently the search result can only be serialized to JSON as JAXB cannot handle Maps.
	 * @param entityId The id of the entity of interest. 
	 * @param limit The maximum number of returned entities. (optional)
	 * @param offset The offset into the list of entities (used for paging). (optional)
	 * @param filterValues The values of the solr filter query. (optional)
	 * @param facetLimit The maximum number of facet results. (optional)
	 * @return A response object containing the data (this is serialized to XML or JSON depending on content negotiation).
	 */
	@RequestMapping(value="/context/{entityId}", method=RequestMethod.GET)
	public @ResponseBody SearchResult handleContextRequest(@PathVariable("entityId") final Long entityId,
			@RequestParam(value = "limit", required = false) final String limit,
			@RequestParam(value = "offset", required = false) final String offset,
			@RequestParam(value = "fq", required = false) final String filterValues,
			@RequestParam(value = "fl", required = false) final String facetLimit) {
		
		final SearchResult result = new SearchResult();
		final List<Long> contextIds = genericSQLService.getConnectedEntityIds(entityId);
		
		if (contextIds == null) { 
			return new SearchResult();
		}
		
		try {
			final StringBuffer queryStr = new StringBuffer(64);
			queryStr.append("(id:(");
			for (int i = 0; i < contextIds.size() ; i++) {
				queryStr.append(contextIds.get(i));
				if (i < contextIds.size() - 1) {
					queryStr.append(" OR ");
				} else {
					queryStr.append(')');
				}
			}
			appendAccessControl(queryStr);
			
			final SolrQuery query = getQueryWithDefaults(queryStr.toString());
						
			setSearchParameters(limit, offset, filterValues, facetLimit, result, query);

			executeAndProcessQuery(result, query, METHOD.POST);
		} catch (SolrServerException e) {
			LOGGER.error(e.getMessage());
		}
		
		return result;
	}

	/**
	 * Constructs a new <code>SolrQuery</code> with default values from the given query string. 
	 * @param queryString The <code>String</code> to construct the query from.
	 * @return The new <code>SolrQuery</code>
	 */
	private SolrQuery getQueryWithDefaults(final String queryString) {
		final SolrQuery query = new SolrQuery("*:*");
		query.setQuery(queryString);
	    
		// default value for limit
	    query.setRows(50);
	    query.setFacetMinCount(1);
	    
	    // default facets to include
	    query.addFacetField("facet_kategorie");
	    query.addFacetField("facet_ort");
	    query.addFacetField("facet_datierung-epoche");
	    		    
	    query.setFacet(true);
	    
	    return query;
	}
	
	/**
	 * This method sends a query to the Solr server and fills the <code>SearchResult</code> instance. 
	 * @param result A <code>SearchResult</code> object to fill.
	 * @param query The query that shall be executed.
	 * @throws SolrServerException
	 */
	private void executeAndProcessQuery(final SearchResult result, final SolrQuery query, final METHOD method) throws SolrServerException {
		final QueryResponse response = server.query(query, method);
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
	}
	
	/**
	 * Method to append access control based on the dataset groups of the current user.
	 * The <code>StringBuffer</code> given to this method must start with a <code>(</code>. 
	 * @param queryStr The query string to append the access control to.
	 */
	private void appendAccessControl(final StringBuffer queryStr) {
		queryStr.append(" AND (");
		boolean first = true;
		for (DatasetGroup datasetGroup: userRightsService.getCurrentUser().getDatasetGroups()) {
			if (first) {
				queryStr.append("datasetGroup:");
				first = false;
			} else {
				queryStr.append(" OR datasetGroup:");
			}
			queryStr.append(datasetGroup.getName());
		}			
		queryStr.append("))");
	}
	
	/**
	 * Helper function to set the search parameters for the Solr query. The first four parameters are the same as used
	 *  in handle search request.
	 * Side effect: If the search request contains the filter query parameter "category" the category specific facets are added to the query.
	 *  See <code>addCategorySpecificFacets</code>. 
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
				addCategorySpecificFacets(filterValueList, query);
				for (String filterValue: filterValueList) {
					query.addFilterQuery(filterValue);
				}
			}
		}
	}
	
	/**
	 * This method adds the facets to the search query that are defined in the XML file of a category. It looks for the key 
	 * <code>"facet_kategorie"</code> and parses its value to try to open the corresponding XML file(s). 
	 * @param filterValueList The filter query parameter string as list.
	 * @param query The outgoing Solr search query.
	 */
	private void addCategorySpecificFacets(final List<String> filterValueList, final SolrQuery query) {
		for (String filterValue: filterValueList) {
			if (filterValue.startsWith("facet_kategorie")) {
				filterValue = filterValue.substring(16);
				// the only multicategory query that makes sense is "OR" combined 
				filterValue = filterValue.replace("OR", "");
				filterValue = filterValue.replace("(", "");
				filterValue = filterValue.replace(")", "");
				filterValue = filterValue.trim();
				filterValue = filterValue.replaceAll("\"", "");
				filterValue = filterValue.replaceAll("\\s+", " ");
				
				final String[] categories = filterValue.split("\\s");
				if (categories.length > 0) {
					for (int i = 0; i < categories.length; i++) {
						final List<String> facets = xmlConfigUtil.getFacetsFromXMLFile(categories[i]);
						if (!StrUtils.isEmptyOrNull(facets)) {
							for (String facet: facets) {
								query.addFacetField("facet_" + facet);
							}
						}
					}
				}
				// no need to process more than one parameter
				return;
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
