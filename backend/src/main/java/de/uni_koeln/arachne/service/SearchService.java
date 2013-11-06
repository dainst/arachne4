package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacet.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.controller.SearchController;
import de.uni_koeln.arachne.response.SearchResult;
import de.uni_koeln.arachne.util.ESClientUtil;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * This class implements all search functionality.
 */
@Service("SearchService")
public class SearchService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	@Autowired
	private transient ESClientUtil esClientUtil;
	
	/**
	 * This method builds and returns an elasticsearch search request. The query is built by the <code>buildQuery</code> method.
	 * @param searchParam The query string.
	 * @param resultSize Max number of results.
	 * @param resultOffset An offset into the result set.
	 * @param filterValueList A list of values to use for building an elasticsearch fuilter query.
	 * @return A <code>SearchRequestBuilder</code> that can be passed directly to <code>executeSearchRequest</code>.
	 */
	public SearchRequestBuilder buildSearchRequest(final String searchParam, final int resultSize, final int resultOffset,
			final List<String> filterValueList) {
		
		return esClientUtil.getClient().prepareSearch(esClientUtil.getSearchIndexAlias())
				.setQuery(buildQuery(searchParam, filterValueList))
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setFrom(resultOffset)
				.setSize(resultSize);
	}
	
	/**
	 * Adds the facet fields specified in <code>facetList</code> to the search request.
	 * @param facetList A string list containing the facet names to add. 
	 * @param searchRequestBuilder The outgoing search request that gets the facets added.
	 */
	public void addFacets(final List<String> facetList, final int facetSize, final SearchRequestBuilder searchRequestBuilder) {
		for (final String facetName: facetList) {
			// return the top 100 facets
			searchRequestBuilder.addFacet(FacetBuilders.termsFacet(facetName).field(facetName).size(facetSize));
		}
	}
	
	/**
	 * Executes a search request on the elasticsearch index. The response is processed and returned as a <code>SearchResult</code> 
	 * instance. 
	 * @param searchRequestBuilder The search request in elasticsearchs internal format.
	 * @param resultSize Max number of results.
	 * @param resultOffset An offset into the resultset.
	 * @param filterValues A <code>String</code> containing the filter values used in the query.
	 * @param facetList The values for facetting.
	 * @return The search result.
	 */
	public SearchResult executeSearchRequest(final SearchRequestBuilder searchRequestBuilder, final int resultSize, final int resultOffset,
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
					, (String)(currenthit.getSource().get("subtitle")), thumbnailId, (String)(currenthit.getSource().get("place"))
					, (String)(currenthit.getSource().get("location"))));
		}
		
		// add facet search results
		final List<String> filterValueList = filterQueryStringToStringList(filterValues);
		if (filterValueList != null) {
			for (int i=0; i<filterValueList.size(); i++) {
				final int colon = filterValueList.get(i).indexOf(':');
				filterValueList.set(i, filterValueList.get(i).substring(0, colon));
			}
		}
		
		if (facetList != null) {
			final Map<String, Map<String, Long>> facets = new LinkedHashMap<String, Map<String, Long>>();
			for (final String facetName: facetList) {
				final Map<String, Long> facetMap = getFacetMap(facetName, searchResponse, filterValues);
				if (facetMap != null && (filterValueList == null || !filterValueList.contains(facetName))) {
					facets.put(facetName, facetMap);
				}
			}
			searchResult.setFacets(facets);
		}
		
		return searchResult;
	}
	
	/**
	 * Creates a list of filter values from the filterValues <code>String</code> and sets the category specific facets in the 
	 * <code>facetList</code> if the corresponding facet is found. 
	 * in the filterValue <code>String</code>.
	 * @param filterValues String of filter values
	 * @param facetList List of facet fields.
	 * @return filter values as list.
	 */
	public List<String> getFilterValueList(final String filterValues, final List<String> facetList) {
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
	 * Extracts the category specific facets from the corresponding xml file.
	 * @param filterValueList The list of facets including their values. The facet "facet_kategorie" must be present to get 
	 * any results.
	 * @return The list of category specific facets or <code>null</code>.
	 */
	public List<String> getCategorySpecificFacetList(final	List<String> filterValueList) {
		final List<String> result = new ArrayList<String>();
		// add geo facet
		result.add("facet_geo");
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
						addFacetsToResult(result, facets);
					}
				}
				// no need to process more than one parameter
				return result;
		    }
		}
		return null;
	}

	/**
	 * Adds the unique facet names found in facets to result.
	 * </br>
	 * Side effect: The input variable result is changed directly. 
	 * @param result List of facet names.
	 * @param facets Unique list of facet names.
	 */
	private void addFacetsToResult(final List<String> result, final List<String> facets) {
		if (!StrUtils.isEmptyOrNull(facets)) {
			for (final String facet: facets) {
				final String facetName = "facet_" + facet;
				if (!result.contains(facetName)) {
					result.add("facet_" + facet);
				}
			}
		}
	}
	
	/**
	 * This method extracts the facet search results from the response and works around a problem of elasticsearch returning 
	 * too many facets for terms that are queried.
	 * @param name A facet name.
	 * @param searchResponse The search response to work on.
	 * @return A map containing the facet value as key and the facet count as value.
	 */
	private Map<String, Long> getFacetMap(final String name, final SearchResponse searchResponse, final String filterValues) {
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
	 * @param searchParam The query string.
	 * @param limit Max number of results.
	 * @param offset An offset into the result set.
	 * @param filterValues A list of values to create a filter query from.
	 * @return An elasticsearch <code>QueryBuilder</code> which in essence is a complete elasticsearch query.
	 */
	private QueryBuilder buildQuery(final String searchParam, final List<String> filterValues) {
		FilterBuilder facetFilter = FilterBuilders.boolFilter().must(esClientUtil.getAccessControlFilter());
				
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
	 * Converts the input string of query filter parameters to a string list of parameters.
	 * The string is split at every occurrence of ",facet_".
	 * @param filterString The filter query string to convert.
	 * @return a string list containing the separated parameters or <code>null</code> if the conversion fails.
	 */
	private List<String> filterQueryStringToStringList(final String filterString) {
		String string = filterString;
		if (string != null && string.startsWith("facet_")) {
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
