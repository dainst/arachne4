package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoBoundingBoxFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.script.ScriptScoreFunctionBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import de.uni_koeln.arachne.response.Place;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.response.search.SearchResultFacetValue;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;
import de.uni_koeln.arachne.util.search.Aggregation;
import de.uni_koeln.arachne.util.search.GeoHashGridAggregation;
import de.uni_koeln.arachne.util.search.SearchFieldList;
import de.uni_koeln.arachne.util.search.SearchParameters;
import de.uni_koeln.arachne.util.search.TermsAggregation;

/**
 * This class implements all search functionality.
 * 
 * @author Reimar Grabowski
 */
@Service("SearchService")
public class SearchService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	@Autowired
	private transient ESService esService;
	
	@Autowired
	private transient Transl8Service ts;
	
	private transient final SearchFieldList searchFields;
	
	private transient final List<String> sortFields;
	
	private transient final List<String> defaultFacetList;
	
	/**
	 * Simple constructor which sets the fields to be queried. 
	 * @param textSearchFields The list of text fields.
	 * @param numericSearchFields The list of numeric fields.
	 * @param sortFields The list of fields to sort on.
	 * @param defaultFacetList The names of the default facets (these are all terms aggregations).
	 */
	@Autowired
	public SearchService(final @Value("#{'${esTextSearchFields}'.split(',')}") List<String> textSearchFields
			, final @Value("#{'${esNumericSearchFields}'.split(',')}") List<String> numericSearchFields
			, final @Value("#{'${esSortFields}'.split(',')}") List<String> sortFields
			, final @Value("#{'${esDefaultFacets}'.split(',')}") List<String> defaultFacetList) {
		
		searchFields = new SearchFieldList(textSearchFields, numericSearchFields);
		this.sortFields = sortFields;
		this.defaultFacetList = defaultFacetList;
	}
	
	/**
	 * This method builds and returns an elasticsearch search request. The query is built by the <code>buildQuery</code> method.
	 * @param searchParameters The search parameter object. 
	 * @param filters The filters of the HTTP 'fq' parameter as Map.
	 * @return A <code>SearchRequestBuilder</code> that can be passed directly to <code>executeSearchRequest</code>.
	 */
	public SearchRequestBuilder buildDefaultSearchRequest(final SearchParameters searchParameters
			, final Multimap<String, String> filters) {
		
		SearchType searchType = (searchParameters.getLimit() > 0) ? SearchType.DFS_QUERY_THEN_FETCH : SearchType.COUNT;
		
		SearchRequestBuilder result = esService.getClient().prepareSearch(esService.getSearchIndexAlias())
				.setQuery(buildQuery(searchParameters.getQuery(), filters, searchParameters.getBoundingBox()))
				.setSearchType(searchType)
				.setFrom(searchParameters.getOffset())
				.setSize(searchParameters.getLimit());
		
		addSort(searchParameters.getSortField(), searchParameters.isOrderDesc(), result);
		addFacets(getFacetList(filters, searchParameters.getFacetLimit(), searchParameters.getGeoHashPrecision()), result);
		
		return result;
	}
	
	/**
	 * This method builds and returns an elasticsearch context search request. The query is built by the 
	 * <code>buildContextQuery</code> method.
	 * @param entityId The entityId to find the contexts for..
	 * @param searchParameters The search parameter object. 
	 * @param filters The filters of the HTTP 'fq' parameter as Map.
	 * @return A <code>SearchRequestBuilder</code> that can be passed directly to <code>executeSearchRequest</code>.
	 */
	public SearchRequestBuilder buildContextSearchRequest(final long entityId, final SearchParameters searchParameters
			, Multimap<String, String> filters) {
		
		SearchRequestBuilder result = esService.getClient().prepareSearch(esService.getSearchIndexAlias())
				.setQuery(buildContextQuery(entityId))
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setFrom(searchParameters.getOffset())
				.setSize(searchParameters.getLimit());
		
		addSort(searchParameters.getSortField(), searchParameters.isOrderDesc(), result);
		addFacets(getFacetList(filters, searchParameters.getFacetLimit(), -1), result);
		
		return result;
	}
	
	/**
	 * Builds a search request with a single facet and "*" as search param to retrieve all values of the given facet.
	 * @param facetName The name of the facet of interest.
	 * @return A <code>SearchRequestBuilder</code> that can be passed directly to <code>executeSearchRequest</code>.
	 */
	public SearchRequestBuilder buildIndexSearchRequest(final String facetName) {
		
		SearchRequestBuilder result = esService.getClient().prepareSearch(esService.getSearchIndexAlias())
				.setQuery(buildQuery("*", null, new Double[0]))
				.setSearchType(SearchType.COUNT)
				.setSize(0);
		
		final Set<Aggregation> aggregations = new LinkedHashSet<Aggregation>();
		aggregations.add(new TermsAggregation(facetName, 0, TermsAggregation.Order.TERMS));
		addFacets(aggregations, result);
		
		return result;
	}
	
	/**
	 * Adds the facet fields specified in <code>facetList</code> to the search request.
	 * @param facetList A string list containing the facet names to add. 
	 * @param searchRequestBuilder The outgoing search request that gets the facets added.
	 */
	public void addFacets(final Set<Aggregation> facetList, final SearchRequestBuilder searchRequestBuilder) {
		
		for (final Aggregation aggregation: facetList) {
			searchRequestBuilder.addAggregation(aggregation.build());
		}
	}
	
	/**
	 * Adds sorting to the search request.
	 * @param sortField The elasticsearch field to sort on (this method takes care of choosing the correct sub-field if 
	 * any).
	 * @param orderDesc If the sort should be in descending order.
	 * @param searchRequestBuilder The request builder to add the sort to.
	 */
	public void addSort(final String sortField, final Boolean orderDesc, SearchRequestBuilder searchRequestBuilder) {
		
		if (!StrUtils.isEmptyOrNull(sortField) && (sortFields.contains(sortField))) {
			String field = sortField; 
			if (searchFields.containsText(sortField)) {
				field += ".sort";
			}
			if (orderDesc != null && orderDesc) {
				searchRequestBuilder.addSort(field, SortOrder.DESC);
			} else {
				searchRequestBuilder.addSort(field, SortOrder.ASC);
			}
		}
	}
	
	/**
	 * Executes a search request on the elasticsearch index. The response is processed and returned as a <code>SearchResult</code> 
	 * instance. 
	 * @param searchRequestBuilder The search request in elasticsearchs internal format.
	 * @param size Max number of results.
	 * @param offset An offset into the resultset.
	 * @param filterValues A <code>String</code> containing the filter values used in the query.
	 * @param facetList The values for facetting.
	 * @return The search result.
	 */
	@SuppressWarnings("unchecked")
	public SearchResult executeSearchRequest(final SearchRequestBuilder searchRequestBuilder, final int size,
			final int offset, final Multimap<String, String> filters) {
		
		SearchResponse searchResponse = null;
						
		try {
			searchResponse = searchRequestBuilder.execute().actionGet();
		} catch (SearchPhaseExecutionException e) {
			LOGGER.error("Problem executing search. PhaseExecutionException: " + e.getMessage());
			final SearchResult failedSearch = new SearchResult();
			failedSearch.setStatus(e.status());
			return failedSearch;
		} catch (ElasticsearchException e) {
			LOGGER.error("Problem executing search. Exception: " + e.getMessage());
			final SearchResult failedSearch = new SearchResult();
			failedSearch.setStatus(e.status());
			return failedSearch;
		}
		
		final SearchHits hits = searchResponse.getHits();
		
		final SearchResult searchResult = new SearchResult();
		searchResult.setLimit(size);
		searchResult.setOffset(offset);
		searchResult.setSize(hits.totalHits());
		
		for (final SearchHit currenthit: hits) {
			final Integer intThumbnailId = (Integer)currenthit.getSource().get("thumbnailId");
			Long thumbnailId = null;
			if (intThumbnailId != null) {
				thumbnailId = Long.valueOf(intThumbnailId);
			}
			searchResult.addSearchHit(new de.uni_koeln.arachne.response.search.SearchHit(Long.valueOf(currenthit.getId())
					, (String)(currenthit.getSource().get("type"))
					, (String)(currenthit.getSource().get("title"))
					, (String)(currenthit.getSource().get("subtitle"))
					, thumbnailId
					, (List<Place>)currenthit.getSource().get("places")));
		}
		
		// add facet search results
		final List<SearchResultFacet> facets = new ArrayList<SearchResultFacet>();
		Map<String, org.elasticsearch.search.aggregations.Aggregation> aggregations = searchResponse.getAggregations().getAsMap();
		for (final String aggregationName : aggregations.keySet()) {
			final Map<String, Long> facetMap = new LinkedHashMap<String, Long>();
			MultiBucketsAggregation aggregator = (MultiBucketsAggregation)aggregations.get(aggregationName);
			// TODO find a better way to convert facet values
			if (aggregationName.equals(GeoHashGridAggregation.GEO_HASH_GRID_NAME)) {
				for (final MultiBucketsAggregation.Bucket bucket: aggregator.getBuckets()) {
					final LatLong coord = GeoHash.decodeHash(bucket.getKey());
					facetMap.put("[" + coord.getLat() + ',' + coord.getLon() + ']', bucket.getDocCount());
				}
			} else {
				for (final MultiBucketsAggregation.Bucket bucket: aggregator.getBuckets()) {
					facetMap.put(bucket.getKey(), bucket.getDocCount());
				}
			}
			if (facetMap != null && !facetMap.isEmpty() && (filters == null || !filters.containsKey(aggregationName))) {
				facets.add(getSearchResultFacet(aggregationName, facetMap));
			}
		}
		searchResult.setFacets(facets);
		
		return searchResult;
	}
	
	/**
	 * Creates a Map of filter name value pairs from the filterValues list. Since a Mulitmap is used multiple values can 
	 * be used for one filter.
	 * @param filterValues String of filter values
	 * @param geoHashPrecision The precision used to convert latlon-values to geohashes. 
	 * @return filter values as list.
	 */
	public Multimap<String, String> getFilters(List<String> filterValues, int geoHashPrecision) {
		
		Multimap<String, String> result = LinkedHashMultimap.create();
		
		if (!StrUtils.isEmptyOrNull(filterValues)) {
			for (final String filterValue : filterValues) {
				final int splitIndex = filterValue.indexOf(':');
				final String name = filterValue.substring(0, splitIndex);
				final String value = filterValue.substring(splitIndex+1).replace("\"", "");
				
				if (filterValue.startsWith(GeoHashGridAggregation.GEO_HASH_GRID_NAME)) {
					final String[] coordsAsStringArray = value.substring(1, value.length() - 1).split(",");
					final String geoHash = GeoHash.encodeHash(
							Double.parseDouble(coordsAsStringArray[0]),
							Double.parseDouble(coordsAsStringArray[1]),
							geoHashPrecision);
					result.put(name, geoHash);
				} else {
					result.put(name, value);
				}
			}
			// keep only highest level of "facet_subkategoriebestand_level"
			int highestLevel = 0;
			String highestLevelValue = "";
			final Multimap<String, String> resultCopy = LinkedHashMultimap.create(result);
			for (final String filter : resultCopy.keySet()) {
				if (filter.startsWith("facet_subkategoriebestand_level")) {
					final int level = extractLevelFromFilter(filter);
					if (level > highestLevel) {
						highestLevel = level;
						if (!"".equals(highestLevelValue)) {
							result.removeAll(highestLevelValue);
						}
						highestLevelValue = filter;
					} else {
						result.removeAll(filter);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Creates a set of <code>Aggregations</code> from the filters and <code>defaultFacetList</code>. If the category 
	 * filter is present category specific facets will be added.
	 * <br>
	 * If the geohash variable is set the geo grid aggregation will be added.
	 * @param filters The filter map.
	 * @param limit The maximum number of distinct facet values returned.
	 * @param geoHashPrecision The length of the geohash used in the geo grid aggregation.
	 * @return A set of <code>Aggregations</code>.
	 */
	private Set<Aggregation> getFacetList(final Multimap<String, String> filters, final int limit, Integer geoHashPrecision) {
		
		final Set<Aggregation> result = new LinkedHashSet<Aggregation>();
		result.addAll(getCategorySpecificFacets(filters, limit));
		
		for (final String facetName : defaultFacetList) {
			result.add(new TermsAggregation(facetName, limit));
		}
		
		// TODO look for a more general way to handle dynamic facets
		boolean isFacetSubkategorieBestandPresent = false;
		for (final String filter : filters.keySet()) {
			if (filter.startsWith("facet_subkategoriebestand_level")) {
				isFacetSubkategorieBestandPresent = true;
				final int level = extractLevelFromFilter(filter);
				final String name = "facet_subkategoriebestand_level" + (level + 1); 
				result.add(new TermsAggregation(name, limit));
			}
		}
		
		if (filters.containsKey("facet_bestandsname") && !isFacetSubkategorieBestandPresent) {
			final String name = "facet_subkategoriebestand_level1";
			result.add(new TermsAggregation(name, limit));
		}
		
		// aggregations - if more aggregations are used this should perhaps be moved to its own method
		
		// geo grid
		if (geoHashPrecision > 0) {
			result.add(new GeoHashGridAggregation(GeoHashGridAggregation.GEO_HASH_GRID_NAME
					, GeoHashGridAggregation.GEO_HASH_GRID_FIELD, geoHashPrecision, 0));
		}
		
		return result;
	}
	
	/**
	 * Extracts the category specific facets from the corresponding xml file.
	 * @param filters The list of facets including their values. The facet Aggregation.CATEGORY_FACET must be present to get 
	 * any results.
	 * @param limit The maximum number of distinct facet values returned.
	 * @return The list of category specific facets or <code>null</code>.
	 */
	private Set<Aggregation> getCategorySpecificFacets(final Multimap<String, String> filters, final int limit) {
		
		final Set<Aggregation> result = new LinkedHashSet<Aggregation>();
		Collection<String> categories = filters.get(TermsAggregation.CATEGORY_FACET);
		
		if (!filters.isEmpty() && !categories.isEmpty()) {
			final String category = ts.categoryLookUp(categories.iterator().next());		
			final Set<String> facets = xmlConfigUtil.getFacetsFromXMLFile(category);
			for (String facet : facets) {
				facet = "facet_" + facet;
				result.add(new TermsAggregation(facet, limit));
			}
		}
		
		return result;
	}
		
	/**
	 * Builds the elasticsearch query based on the input parameters. It also adds an access control filter to the query.
	 * The final query is a function score query that modifies the score based on the boost value of a document. 
	 * Embedded is a filtered query to account for access control, facet and bounding box filters
	 * which finally uses a simple query string query with 'AND' as default operator.
	 * If the search parameter is numeric the query is performed against all configured fields else the query is only 
	 * performed against the text fields.
	 * @param searchParam The query string.
	 * @param filters The filter from the HTTP 'fq' parameter as map to create a filter query from.
	 * @param bbCoords An array representing the top left and bottom right coordinates of a bounding box (order: lat, long)
	 * @return An elasticsearch <code>QueryBuilder</code> which in essence is a complete elasticsearch query.
	 */
	private QueryBuilder buildQuery(final String searchParam
			, final Multimap<String, String> filters
			, final Double[] bbCoords) {
		
		FilterBuilder facetFilter = esService.getAccessControlFilter();
				
		if (filters != null && !filters.isEmpty()) {
			for (final Map.Entry<String, Collection<String>> filter: filters.asMap().entrySet()) {
				// TODO find a way to unify this
				if (filter.getKey().equals(GeoHashGridAggregation.GEO_HASH_GRID_NAME)) {
					final String filterValue = filter.getValue().iterator().next();
					facetFilter = FilterBuilders.boolFilter().must(facetFilter).must(
							FilterBuilders.geoHashCellFilter(GeoHashGridAggregation.GEO_HASH_GRID_FIELD, filterValue));
				} else {
					facetFilter = FilterBuilders.boolFilter().must(facetFilter).must(
							FilterBuilders.termFilter(filter.getKey(), filter.getValue()));
				}
			}
		}
		
		final QueryStringQueryBuilder innerQuery = QueryBuilders.queryStringQuery(searchParam)
				.defaultOperator(Operator.AND);
		
		for (String textField: searchFields.text()) {
			innerQuery.field(textField);
		}
		
		if (StringUtils.isNumeric(searchParam)) {
			for (final String numericField: searchFields.numeric()) {
				innerQuery.field(numericField);
			}
		}
		
		final QueryBuilder filteredQuery;
		if (bbCoords.length == 4) {
			GeoBoundingBoxFilterBuilder bBoxFilter = FilterBuilders.geoBoundingBoxFilter("places.location")
					.topLeft(bbCoords[0], bbCoords[1]).bottomRight(bbCoords[2], bbCoords[3]);
			AndFilterBuilder andFilter = FilterBuilders.andFilter(facetFilter, bBoxFilter);
			filteredQuery = QueryBuilders.filteredQuery(innerQuery, andFilter);
		} else {
			filteredQuery = QueryBuilders.filteredQuery(innerQuery, facetFilter);
		}
		
		final ScriptScoreFunctionBuilder scoreFunction = ScoreFunctionBuilders
				.scriptFunction("doc['boost'].value")
				.lang("expression");
		final QueryBuilder query = QueryBuilders.functionScoreQuery(filteredQuery, scoreFunction).boostMode("multiply");
		
		LOGGER.debug("Elastic search query: " + query.toString());
		return query;
	}
	
	/**
	 * Builds the context query.
	 * @param entityId
	 * @return The query to retrieve the connected entities.
	 */
	private QueryBuilder buildContextQuery(Long entityId) {
		
		BoolFilterBuilder accessFilter = esService.getAccessControlFilter();
		final TermQueryBuilder innerQuery = QueryBuilders.termQuery("connectedEntities", entityId);
		final QueryBuilder query = QueryBuilders.filteredQuery(innerQuery, accessFilter);
										
		LOGGER.debug("Elastic search query: " + query.toString());
		return query;
	}
		
	private SearchResultFacet getSearchResultFacet(final String facetName, final Map<String, Long> facetMap) {
		
		final SearchResultFacet result = new SearchResultFacet(facetName);
		
		for (final Map.Entry<String, Long> entry: facetMap.entrySet()) {
			final SearchResultFacetValue facetValue = new SearchResultFacetValue(entry.getKey(), entry.getKey(), entry.getValue());
			result.addValue(facetValue);
		}
		
		return result;
	}
	
	/**
	 * Extracts the level of a dynamic facet from a filter value and returns it as an <code>int</code>.
	 * @param filter A filter name as given by the "fq" HTTP parameter.
	 * @return The level of the facet or -1 in case of failure.
	 */
	private int extractLevelFromFilter(final String filter) {
		
		int result;
		
		try {
			result = Integer.parseInt(filter.substring(31, filter.length()));
		} catch (NumberFormatException e) {
			LOGGER.warn("Number Format exception when parsing filter: ", filter);
			result = -1;
		}
		
		return result;
	}	
}
