package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacet.Entry;
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
import de.uni_koeln.arachne.response.StatusResponse;
import de.uni_koeln.arachne.service.GenericSQLService;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.util.ESClientUtil;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * Handles http requests (currently only get) for <code>/search<code>.
 */
@Controller
public class SearchController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
	
	/**
	 * Maximum number of contexts fetched in one request from elasticsearch.
	 * Needed since URL parameters cannot be arbitrarily long and the context queries can easily get larger than
	 * 10000 characters.
	 */
	private static final int MAX_CONTEXT_QUERY_SIZE = 50;
	
	@Autowired
	private transient ESClientUtil esClientUtil;
	
	@Autowired
	private transient GenericSQLService genericSQLService; 
	
	@Autowired
	private transient IUserRightsService userRightsService; 
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	private transient final List<String> defaultFacetList = new ArrayList<String>(3); 
	
	private transient final int defaultFacetLimit;
	
	private transient final int defaultLimit;
	
	@Autowired
	public SearchController(final @Value("#{config.esDefaultLimit}") int defaultLimit,
			final @Value("#{config.esDefaultFacetLimit}") int defaultFacetLimit) {
		
		this.defaultLimit = defaultLimit;
		this.defaultFacetLimit = defaultFacetLimit;
				
		defaultFacetList.add("facet_kategorie");
		defaultFacetList.add("facet_ort");
		defaultFacetList.add("facet_datierungepoche");
	}
	
	/**
	 * Handles the http request by querying the Elasticsearch index and returning the search result.
	 * The "title" field is boosted by 2 so that documents containing the search keyword in the title are higher ranked than
	 *  documents containing the keyword in other fields.
	 * <br>
	 * The return type of this method is <code>Object</code> so that it can return either a <code>SearchResult</code> or a <code>
	 * StatusMessage</code>.
	 * <br>
	 * Currently the search result can only be serialized to JSON as JAXB cannot handle Maps.
	 * @param searchParam The value of the search parameter. (mandatory)
	 * @param limit The maximum number of returned entities. (optional)
	 * @param offset The offset into the list of entities (used for paging). (optional)
	 * @return A response object containing the data or a status response (this is serialized to XML or JSON depending on content negotiation).
	 */
	@RequestMapping(value="/search", method=RequestMethod.GET)
	public @ResponseBody Object handleSearchRequest(@RequestParam("q") final String searchParam,
													  @RequestParam(value = "limit", required = false) final Integer limit,
													  @RequestParam(value = "offset", required = false) final Integer offset,
													  @RequestParam(value = "fq", required = false) final String filterValues,
													  @RequestParam(value = "fl", required = false) final Integer facetLimit,
													  final HttpServletResponse response) {
		
		final int resultSize = limit == null ? defaultLimit : limit;
		final int resultOffset = offset == null ? 0 : offset;
		final int resultFacetLimit = facetLimit == null ? defaultFacetLimit : facetLimit;
		
		final List<String> facetList = defaultFacetList;
		final List<String> filterValueList = getFilterValueList(filterValues, facetList);
		
		final SearchRequestBuilder searchRequestBuilder = buildSearchRequest(searchParam, resultSize, resultOffset, filterValueList);
		LOGGER.info("Adding facets: " + facetList);
		addFacets(facetList, resultFacetLimit, searchRequestBuilder);
		
		final SearchResult searchResult = executeSearchRequest(searchRequestBuilder, resultSize, resultOffset, filterValues, facetList);
		
		if (searchResult == null) {
			return new StatusResponse("There was a problem executing the search. Please try again. If the problem persists please contact us.");
		} else {
			return searchResult;
		}
	}
	
	/**
	 * Handles the HTTP request by querying the elasticsearch index for contexts of a given entity and returning the result.
	 * <br>
	 * Since the queries can get quite large communication with elasticsearch may be split into multiple requests and
	 * the search result will be the sum of the responses. 
	 * <br> 
	 * Currently the search result can only be serialized to JSON as JAXB cannot handle Maps.
	 * @param entityId The id of the entity of interest. 
	 * @param limit The maximum number of returned entities. (optional)
	 * @param offset The offset into the list of entities (used for paging). (optional)
	 * @param filterValues The values of the solr filter query. (optional)
	 * @return A response object containing the data (this is serialized to XML or JSON depending on content negotiation).
	 */
	@RequestMapping(value="/contexts/{entityId}", method=RequestMethod.GET)
	public @ResponseBody SearchResult handleContextRequest(@PathVariable("entityId") final Long entityId,
			@RequestParam(value = "limit", required = false) final Integer limit,
			@RequestParam(value = "offset", required = false) final Integer offset,
			@RequestParam(value = "fq", required = false) final String filterValues,
			@RequestParam(value = "fl", required = false) final Integer facetLimit,
			final HttpServletResponse response) {
		
		final int resultSize = limit == null ? 50 : limit;
		final int resultOffset = offset == null ? 0 : offset;
		final int resultFacetLimit = facetLimit == null ? defaultFacetLimit : facetLimit;
		
		SearchResult result = new SearchResult();
		final List<Long> contextIds = genericSQLService.getConnectedEntityIds(entityId);
		final int totalHits = contextIds.size();
		
		List<String> facetList = defaultFacetList;
		LOGGER.info("FilterValues: " + filterValues);
		List<String> filterValueList = getFilterValueList(filterValues, facetList);
		
		if (contextIds != null) { 
			int lastContext = resultSize + resultOffset - 1;
			lastContext = lastContext < totalHits ? lastContext : totalHits - 1;
			final int returnedHits = lastContext - resultOffset + 1;
			if (returnedHits <= MAX_CONTEXT_QUERY_SIZE) {
				final String queryStr = getContextQueryString(resultOffset, lastContext, contextIds);
				LOGGER.debug("Context query: " + queryStr);
								
				final SearchRequestBuilder searchRequestBuilder = buildSearchRequest(queryStr, returnedHits, 0, filterValueList);
				addFacets(facetList, resultFacetLimit, searchRequestBuilder);
				result = executeSearchRequest(searchRequestBuilder, resultSize, resultOffset, filterValues, facetList);
				result.setSize(totalHits);
			} else {
				final int requests = (returnedHits - 1) / MAX_CONTEXT_QUERY_SIZE;
							
				int start = resultOffset;
				int end = MAX_CONTEXT_QUERY_SIZE + resultOffset - 1;
				for (int i = 0; i <= requests; i++) {
					final String queryStr = getContextQueryString(start, end, contextIds);
					LOGGER.debug("Context multi query: " + queryStr);
										
					final SearchRequestBuilder searchRequestBuilder = buildSearchRequest(queryStr, returnedHits, 0, null);
					result.merge(executeSearchRequest(searchRequestBuilder, resultSize, resultOffset, filterValues, null));
					
					start = end + 1;
					end = end + MAX_CONTEXT_QUERY_SIZE;
					if (i == requests - 1) {
						end = lastContext;
					}
				}
				result.setSize(totalHits);
			}
		}
		return result;
	}
	
	/**
	 * Creates a list of filter values from the filterValues <code>String</code> and sets the category specific facets in the 
	 * <code>facetList</code> if the corresponding facet is found. 
	 * in the filterValue <code>String</code>.
	 * @param filterValues String of filter values
	 * @param facetList List of facet fields.
	 * @return filter values as list.
	 */
	public List<String> getFilterValueList(final String filterValues, List<String> facetList) {
		List<String> result = null; 
		if (!StrUtils.isEmptyOrNull(filterValues)) {
			result = filterQueryStringToStringList(filterValues);
			for (final String filterValue: result) {
				if (filterValue.contains("facet_kategorie")) {
					facetList.clear();
					facetList.addAll(getCategorySpecificFacetList(result));
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Executes a search request on the elasticsearch index. The response is processed and returned as a <code>SearchResult</code> 
	 * instance. 
	 * @param searchRequestBuilder
	 * @param resultSize
	 * @param resultOffset
	 * @param filterValues
	 * @param facetList
	 * @return
	 */
	private SearchResult executeSearchRequest(final SearchRequestBuilder searchRequestBuilder, final int resultSize, final int resultOffset,
			final String filterValues, final List<String> facetList) {
		
		SearchResponse searchResponse = null;
		try {
			searchResponse = searchRequestBuilder.execute().actionGet();
		} catch (Exception e) {
			LOGGER.error("Problem executing search. Exception: " + e.getMessage());
			return null;
		}
		
		final SearchHits hits = searchResponse.getHits();
		
		final SearchResult searchResult = new SearchResult();
		searchResult.setLimit(resultSize);
		searchResult.setOffset(resultOffset);
		searchResult.setSize(hits.totalHits());
		
		for (final SearchHit currenthit: hits) {
			final Integer intThumbnailId = (Integer)currenthit.getSource().get("thumbnailId");
			Long thumbnailId = null;
			if (intThumbnailId != null) {
				thumbnailId = Long.valueOf(intThumbnailId);
			}
			searchResult.addSearchHit(new de.uni_koeln.arachne.response.SearchHit(Long.valueOf(currenthit.getId())
					, (String)(currenthit.getSource().get("type")), (String)(currenthit.getSource().get("title"))
					, (String)(currenthit.getSource().get("subtitle")), thumbnailId));
		}
		
		// add facet search results
		if (facetList != null) {
			final Map<String, Map<String, Long>> facets = new LinkedHashMap<String, Map<String, Long>>();
			for (final String facetName: facetList) {
				final Map<String, Long> facetMap = getFacetMap(facetName, searchResponse, filterValues);
				if (facetMap != null) {
					facets.put(facetName, getFacetMap(facetName, searchResponse, filterValues));
				}
			}
			searchResult.setFacets(facets);
		}
		
		return searchResult;
	}

	/**
	 * This method builds and returns an elasticsearch search request. The query is built by the <code>buildQuery</code> method.
	 * @param searchParam
	 * @param resultSize
	 * @param resultOffset
	 * @param filterValueList
	 * @param client
	 * @return
	 */
	private SearchRequestBuilder buildSearchRequest(final String searchParam, final int resultSize, final int resultOffset,
			final List<String> filterValueList) {
		
		return esClientUtil.getClient().prepareSearch(esClientUtil.getSearchIndexAlias())
				.setQuery(buildQuery(searchParam, filterValueList))
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setFrom(resultOffset)
				.setSize(resultSize);
	}
	
	/**
	 * Builds the elasticsearch context query string by specifying the connected Ids.
	 * @param start
	 * @param end
	 * @param contextIds
	 * @return
	 */
	private String getContextQueryString(final int start, final int end, final List<Long> contextIds) {
		
		final StringBuffer queryStr = new StringBuffer(16);
		queryStr.append("entityId:(");
		for (int i = start; i <= end; i++) {
			queryStr.append(contextIds.get(i));
			if (i < end) {
				queryStr.append(" OR ");
			} else {
				queryStr.append(')');
			}
		}
		return queryStr.toString();
	}
	
	/**
	 * Extracts the category specific facets from the corresponding xml file.
	 * @param filterValueList
	 * @return
	 */
	private List<String> getCategorySpecificFacetList(final	List<String> filterValueList) {
		LOGGER.info("Changing facets: " + filterValueList);
		final List<String> result = new ArrayList<String>();
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
							for (final String facet: facets) {
								final String facetName = "facet_" + facet;
								if (!result.contains(facetName)) {
									result.add("facet_" + facet);
								}
							}
						}
					}
				}
				// no need to process more than one parameter
				return result;
		    }
		}
		return null;
	}

	/**
	 * Adds the facet fields specified in <code>facetList</code> to the search request.
	 * @param facetList A string list containing the facet names to add. 
	 * @param searchRequestBuilder The outgoing search request that gets the facets added.
	 */
	private void addFacets(final List<String> facetList, final int facetSize, final SearchRequestBuilder searchRequestBuilder) {
		for (final String facetName: facetList) {
			// return the top 100 facets
			searchRequestBuilder.addFacet(FacetBuilders.termsFacet(facetName).field(facetName).size(facetSize));
		}
	}
			
	/**
	 * This method extracts the facet search results from the response and works around a problem of elasticsearch returning 
	 * too many facets for terms that are queried.
	 * @param name
	 * @param searchResponse
	 * @return
	 */
	Map<String, Long> getFacetMap(final String name, final SearchResponse searchResponse, final String filterValues) {
		final TermsFacet facet = (TermsFacet) searchResponse.getFacets().facet(name);
		final Map<String, Long> facetMap = new LinkedHashMap<String, Long>();
		// workaround for elasticsearch reporting too many facets entries as there should only be one
		if (facet.getEntries().isEmpty()) {
			return null;
		} else {
			if (filterValues != null && filterValues.contains(name)) {
				facetMap.put(facet.getEntries().get(0).getTerm().toString(), Long.valueOf(facet.getEntries().get(0).getCount()));
			} else {
				for (final Entry entry: facet.getEntries()) {
					facetMap.put(entry.getTerm().toString(), Long.valueOf(entry.getCount()));
				}
			}
			return facetMap;
		}
	}
	
	/**
	 * Builds the elasticsearch query based on the input parameters. It also adds an access control filter to the query.
	 * @param searchParam
	 * @param limit
	 * @param offset
	 * @param filterValues
	 * @return
	 */
	QueryBuilder buildQuery(final String searchParam, final List<String> filterValues) {
		FilterBuilder facetFilter = FilterBuilders.boolFilter().must(getAccessControlFilter());
				
		if (!StrUtils.isEmptyOrNull(filterValues)) {
			for (final String filterValue: filterValues) {
				final int splitIndex = filterValue.indexOf(':');
				final String name = filterValue.substring(0, splitIndex);
				final String value = filterValue.substring(splitIndex+1).replace("\"", ""); 
				facetFilter = FilterBuilders.boolFilter().must(facetFilter).must(FilterBuilders.termFilter(name, value));
			}
		}
		
		final QueryBuilder query = QueryBuilders.filteredQuery(QueryBuilders.queryString(searchParam), facetFilter);
						
		LOGGER.debug("Elastic search query: " + query.toString());
		return query;
	}
	
	/**
	 * This method constructs a access control query filter for Elasticsearch using the <code>UserRightsService</code>.
	 * @return The constructed query filter.
	 */
	private QueryFilterBuilder getAccessControlFilter() {
		final StringBuffer datasetGroups = new StringBuffer(16);
		boolean first = true;
		for (final DatasetGroup datasetGroup: userRightsService.getCurrentUser().getDatasetGroups()) {
			if (first) {
				first = false;
			} else {
				datasetGroups.append(" OR ");
			}
			datasetGroups.append(datasetGroup.getName());
		}
		return FilterBuilders.queryFilter(QueryBuilders.fieldQuery("datasetGroup", datasetGroups.toString()));
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
