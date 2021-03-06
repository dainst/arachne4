package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.InternalSingleBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoHashGrid;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
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
import de.uni_koeln.arachne.response.search.SuggestResult;
import de.uni_koeln.arachne.service.Transl8Service.Transl8Exception;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;
import de.uni_koeln.arachne.util.search.Aggregation;
import de.uni_koeln.arachne.util.search.GeoHashGridAggregation;
import de.uni_koeln.arachne.util.search.NestedGeoHashGridAggregation;
import de.uni_koeln.arachne.util.search.NestedTermsAggregation;
import de.uni_koeln.arachne.util.search.SearchFieldList;
import de.uni_koeln.arachne.util.search.SearchParameters;
import de.uni_koeln.arachne.util.search.TermsAggregation;
import de.uni_koeln.arachne.util.search.TermsAggregation.Order;


/**
 * This class implements all search functionality.
 *
 * @author Reimar Grabowski
 */
@Service("SearchService")
public class SearchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

	private static final int MAX_OPEN_SCROLL_REQUESTS = 10;

	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;

	@Autowired
	private transient ESService esService;

	@Autowired
	private transient Transl8Service ts;

	@Autowired
	private transient UserRightsService userRightsService;

	private transient final SearchFieldList searchFields;

	private transient final List<String> sortFields;


	/**
	 * Simple constructor which sets the fields to be queried.
	 * @param textSearchFields The list of text fields.
	 * @param numericSearchFields The list of numeric fields.
	 * @param sortFields The list of fields to sort on.
	 */
	@Autowired
	public SearchService(final @Value("#{'${esTextSearchFields}'.split(',')}") List<String> textSearchFields
			, final @Value("#{'${esNumericSearchFields}'.split(',')}") List<String> numericSearchFields
			, final @Value("#{'${esSortFields}'.split(',')}") List<String> sortFields
			//, final @Value("#{'${esDefaultFacets}'.split(',')}") List<String> getDefaultFacetList()
	) {


		searchFields = new SearchFieldList(textSearchFields, numericSearchFields);
		this.sortFields = sortFields;

	}

	public List<String> getDefaultFacetList() {
		final List<String> defaultFacetsAsList = new ArrayList<String>();
		defaultFacetsAsList.addAll(xmlConfigUtil.getFacetsFromXMLFile("_default_facets"));
		return defaultFacetsAsList;

	}

	/**
	 * This method builds and returns an elasticsearch search request. The query is built by the
	 * <code>buildQuery</code> method.
	 * @param searchParameters The search parameter object.
	 * @param filters The filters of the HTTP 'fq' parameter as Map.
	 * @param lang The language.
	 * @return A <code>SearchRequestBuilder</code> that can be passed directly to <code>executeSearchRequest</code>.
	 * @throws Transl8Exception if transl8 cannot be reached.
	 */
	public SearchRequestBuilder buildDefaultSearchRequest(final SearchParameters searchParameters
			, final Multimap<String, String> filters, final String lang) throws Transl8Exception {

		SearchRequestBuilder result = esService.getClient().prepareSearch(esService.getSearchIndexAlias())
				.setQuery(buildQuery(searchParameters.getQuery(), filters, searchParameters.getBoundingBox(), false))
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setSize(searchParameters.getLimit())
				.setFrom(searchParameters.getOffset())
				.setTrackTotalHits(true);

		if (!searchParameters.isFacetMode()) {
			if (searchParameters.isScrollMode()
					&& userRightsService.isSignedInUser()
					&& getOpenScrollRequests() < MAX_OPEN_SCROLL_REQUESTS) {
				result.setScroll("1m");
			} else {
				// enable highlighting for all search fields (text fields only)
				HighlightBuilder highlightBuilder = new HighlightBuilder()
					.field("title", 100, 0)
					.field("subtitle", 100, 0)
					.field("searchableContent", 100, 5)
					.field("datasetGroup", 100, 0)
					.field("searchableEditorContent", 100, 0);
				result.highlighter(highlightBuilder);
			}
		}

		addSort(searchParameters.getSortField(), searchParameters.isOrderDesc(), result);
		addFacets(getFacetList(filters, searchParameters.getFacetLimit() + searchParameters.getFacetOffset()
				, searchParameters.getGeoHashPrecision(), searchParameters.getFacet(), lang)
				, searchParameters.getFacetsToSort(), result, searchParameters.isLexical());

		LOGGER.debug("Complete Elasticsearch search request: " + result.toString());

		return result;
	}

	private long getOpenScrollRequests() {
		NodesStatsResponse nodesStatsResponse = esService.getClient().admin().cluster().prepareNodesStats()
				.execute().actionGet();

		long scrollCurrent = 0;

		for (NodeStats nodeStats : nodesStatsResponse.getNodes()) {
			scrollCurrent += nodeStats.getIndices().getSearch().getTotal().getScrollCurrent();
		}

		// strangely getScrollCurrent() increases by 5 for every request
		return scrollCurrent / 5;
	}

	/**
	 * This method builds and returns an elasticsearch context search request. The query is built by the
	 * <code>buildContextQuery</code> method.
	 * @param entityId The entityId to find the contexts for..
	 * @param searchParameters The search parameter object.
	 * @param filters The filters of the HTTP 'fq' parameter as Map.
	 * @param lang The language.
	 * @return A <code>SearchRequestBuilder</code> that can be passed directly to <code>executeSearchRequest</code>.
	 * @throws Transl8Exception if transl8 cannot be reached.
	 */
	public SearchRequestBuilder buildContextSearchRequest(final long entityId, final SearchParameters searchParameters
			, Multimap<String, String> filters, final String lang) throws Transl8Exception {

		SearchRequestBuilder result = esService.getClient().prepareSearch(esService.getSearchIndexAlias())
				.setQuery(buildContextQuery(entityId))
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setFrom(searchParameters.getOffset())
				.setSize(searchParameters.getLimit());

		addSort(searchParameters.getSortField(), searchParameters.isOrderDesc(), result);
		addFacets(getFacetList(filters, searchParameters.getFacetLimit(), -1, null, lang), searchParameters.getFacetsToSort()
				, result, searchParameters.isLexical());

		return result;
	}

	/**
	 * Builds a search request with a single facet and "*" as search param to retrieve all values of the given facet.
	 * @param query The query string.
	 * @param facetName The name of the facet of interest.
	 * @param filters The filters to build a filter query from.
	 * @return A <code>SearchRequestBuilder</code> that can be passed directly to <code>executeSearchRequest</code>.
	 */
	public SearchRequestBuilder buildIndexSearchRequest(final String query, final String facetName, final Multimap<String, String> filters) {

		SearchRequestBuilder result = esService.getClient().prepareSearch(esService.getSearchIndexAlias())
				.setQuery(buildQuery(query, filters, null, false))
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setSize(0);

		final Set<Aggregation> aggregations = new LinkedHashSet<Aggregation>();
		aggregations.add(new TermsAggregation(facetName, 0, TermsAggregation.Order.TERMS));
		addFacets(aggregations, new ArrayList<String>(), result, true);

		return result;
	}

	/**
	 * Adds the facet fields specified in <code>facetList</code> to the search request.
	 * @param facetList A string list containing the facet names to add.
	 * @param facetsToSort A list of facet names. Facets in this list are sorted lexically.
	 * @param searchRequestBuilder The outgoing search request that gets the facets added.
	 * @param lexically A boolean indicating if the {@code facetsToSort} should be sorted lexically.
	 */
	public void addFacets(final Set<Aggregation> facetList, final List<String> facetsToSort
			, final SearchRequestBuilder searchRequestBuilder, final Boolean lexically) {

		for (final Aggregation aggregation: facetList) {
			if (lexically || (aggregation instanceof TermsAggregation && facetsToSort.contains(aggregation.getName()))) {
				((TermsAggregation)aggregation).setOrder(Order.TERMS);
			}
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
	 * @param filters A <code>String</code> containing the filter values used in the query.
	 * @param facetOffset An offset into the facet lists.
	 * @param searchParameters The search parameters.
	 * @return The search result.
	 * @throws Transl8Exception if transl8 cannot be reached
	 */
	@SuppressWarnings("unchecked")
	public SearchResult executeSearchRequest(final SearchRequestBuilder searchRequestBuilder, final int size,
			final int offset, final Multimap<String, String> filters, final int facetOffset,
                                             final SearchParameters searchParameters) throws Transl8Exception {

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
		searchResult.setSize(hits.getTotalHits().value);
		searchResult.setScrollId(searchResponse.getScrollId());
        searchResult.setSearchParameters(searchParameters);
        searchResult.setFilters(filters);

		for (final SearchHit currenthit: hits) {
			Map<String, Object> source = currenthit.getSourceAsMap();
			final Integer intThumbnailId = (Integer) source.get("thumbnailId");
			Long thumbnailId = null;
			if (intThumbnailId != null) {
				thumbnailId = Long.valueOf(intThumbnailId);
			}

			Map<String, List<String>> highlights = currenthit.getHighlightFields().entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.stream(entry.getValue().fragments())
							.map(fragment -> fragment.toString())
							.collect(Collectors.toList())));

			searchResult.addSearchHit(new de.uni_koeln.arachne.response.search.SearchHit(Long.valueOf(currenthit.getId())
					, (String)(source.get("type"))
					, (String)(source.get("@id"))
					, (String)(source.get("title"))
					, (String)(source.get("subtitle"))
					, thumbnailId
					, (List<Place>) source.get("places")
					, highlights
					, source));
		}

		// add facet search results
		final List<SearchResultFacet> facets = new ArrayList<SearchResultFacet>();
		Map<String, org.elasticsearch.search.aggregations.Aggregation> aggregations = searchResponse.getAggregations().getAsMap();

		// get category (categories) which was searched for
		final Collection<String> categories = filters.get(TermsAggregation.CATEGORY_FACET);
		final String category = categories.size() > 0 ? ts.categoryLookUp(categories.iterator().next(), "de") : "_default_facets";

		boolean placeFacetSelected = false;
		String[] placeFacets = NestedTermsAggregation.PLACE_FACETS;
		for (String facetName : placeFacets) {
		    placeFacetSelected = filters.containsKey(facetName) | placeFacetSelected;
		}

		for (final String aggregationName : aggregations.keySet()) {
			final Map<String, Long> facetMap = new LinkedHashMap<String, Long>();
//			 TODO find a better way to convert facet values

		    if (aggregationName.equals(GeoHashGridAggregation.GEO_HASH_GRID_NAME)) {
			    InternalSingleBucketAggregation nestedAggregator = (InternalSingleBucketAggregation) aggregations.get(aggregationName);
			    InternalGeoHashGrid aggregator = (InternalGeoHashGrid) nestedAggregator.getAggregations().asList().get(0);

				for (int i = facetOffset; i < aggregator.getBuckets().size(); i++) {
					final MultiBucketsAggregation.Bucket bucket = aggregator.getBuckets().get(i);
					final LatLong coord = GeoHash.decodeHash(bucket.getKeyAsString());
					facetMap.put("[" + coord.getLat() + ',' + coord.getLon() + ']', bucket.getDocCount());
				}
			} else if (placeFacetSelected && Arrays.asList(placeFacets).contains(aggregationName)
					&& aggregations.get(aggregationName) instanceof InternalSingleBucketAggregation) {

			    InternalSingleBucketAggregation internalAggregator = (InternalSingleBucketAggregation) aggregations.get(aggregationName);
			    InternalFilter aggregationFilter = (InternalFilter) internalAggregator.getAggregations().asList().get(0);
			    MultiBucketsAggregation aggregator = (MultiBucketsAggregation) aggregationFilter.getAggregations().asList().get(0);

	            for (int i = facetOffset; i < aggregator.getBuckets().size(); i++) {
	                final MultiBucketsAggregation.Bucket bucket = aggregator.getBuckets().get(i);
	                facetMap.put(bucket.getKeyAsString(), bucket.getDocCount());
	            }
			} else {
			    MultiBucketsAggregation aggregator = (MultiBucketsAggregation) aggregations.get(aggregationName);

				for (int i = facetOffset; i < aggregator.getBuckets().size(); i++) {
					final MultiBucketsAggregation.Bucket bucket = aggregator.getBuckets().get(i);
					facetMap.put(bucket.getKeyAsString(), bucket.getDocCount());
				}
			}
			if (!facetMap.isEmpty() && !filters.containsKey(aggregationName)) {
				facets.add(getSearchResultFacet(aggregationName, facetMap, category));
			}
		}
		searchResult.setFacets(facets);
		return searchResult;
	}

	/**
	 * Executes a search scroll request on the elasticsearch index to get the next batch of results. User must be
	 * signed in.
	 * @param scrollId The scrollId of the initial search request.
	 * @return The search result.
	 */
	@SuppressWarnings("unchecked")
	public SearchResult executeSearchScrollRequest(final String scrollId) {
		SearchResponse searchResponse = null;

		try {
			searchResponse = esService.getClient().prepareSearchScroll(scrollId).setScroll("1m").execute().actionGet();
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
		} catch (ArrayIndexOutOfBoundsException e) {
			// workaround for ES crashing on some (timed out,invalid ???) scroll ids
			final SearchResult failedSearch = new SearchResult();
			failedSearch.setStatus(RestStatus.NOT_FOUND);
			return failedSearch;
		}

		final SearchHits hits = searchResponse.getHits();

		// clear scroll request when a page after the last is requested
		if (hits.getHits().length == 0) {
			ClearScrollResponse clearScrollResponse = esService.getClient().prepareClearScroll()
					.addScrollId(searchResponse.getScrollId()).execute().actionGet();
			if (!clearScrollResponse.isSucceeded()) {
				LOGGER.warn("Failed to clear scroll with id " + searchResponse.getScrollId() + ". Status: "
						+ clearScrollResponse.status());
			}
		}

		final SearchResult searchResult = new SearchResult();
		searchResult.setLimit(hits.getHits().length);
		searchResult.setSize(hits.getTotalHits().value);
		searchResult.setScrollId(searchResponse.getScrollId());

		for (final SearchHit currenthit: hits) {
			Map<String, Object> source = currenthit.getSourceAsMap();
			final Integer intThumbnailId = (Integer) source.get("thumbnailId");
			Long thumbnailId = null;
			if (intThumbnailId != null) {
				thumbnailId = Long.valueOf(intThumbnailId);
			}
			searchResult.addSearchHit(new de.uni_koeln.arachne.response.search.SearchHit(Long.valueOf(currenthit.getId())
					, (String)(source.get("type"))
					, (String)(source.get("@id"))
					, (String)(source.get("title"))
					, (String)(source.get("subtitle"))
					, thumbnailId
					, (List<Place>) source.get("places"),
					null
					, source));
		}

		return searchResult;
	}

	/**
	 * Executes a completion suggest request on the elastic search index.
	 * @param queryString The term to get suggestion for.
	 * @return The suggestions.
	 */
	public SuggestResult executeSuggestRequest(String queryString) {
		SuggestResult suggestResult = new SuggestResult();

		CompletionSuggestionBuilder suggestionBuilder = SuggestBuilders
			.completionSuggestion("suggest")
			.text(queryString)
			.size(10);

		SearchResponse searchResponse = esService.getClient()
			.prepareSearch(esService.getSearchIndexAlias())
			.suggest(new SuggestBuilder().addSuggestion("suggest", suggestionBuilder))
			.execute()
			.actionGet();

		searchResponse.getSuggest().getSuggestion("suggest").getEntries().forEach(entry -> 
			entry.getOptions().forEach(option -> 
				suggestResult.addSuggestion(option.getText().string())
			)
		);

		return suggestResult;
	}

	/**
	 * Creates a Map of filter name value pairs from the filterValues list. Since a Multimap is used multiple values can
	 * be used for one filter.
	 * @param filterValues String of filter values
	 * @param geoHashPrecision The precision used to convert latlon-values to geohashes.
	 * @return filter values as multimap.
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
		}

		return result;
	}

	/**
	 * Creates a set of <code>Aggregations</code> from the filters and <code>getDefaultFacetList()</code>. If the category
	 * filter is present category specific facets will be added.
	 * <br>
	 * If the geohash variable is set the geo grid aggregation will be added.
	 * @param filters The filter map.
	 * @param limit The maximum number of distinct facet values returned.
	 * @param geoHashPrecision The length of the geohash used in the geo grid aggregation.
	 * @param facet A single facet. If not null only an aggregation for this facet will be added.
	 * @param lang The language.
	 * @return A set of <code>Aggregations</code>.
	 * @throws Transl8Exception if transl8 cannot be reached.
	 */
	public Set<Aggregation> getFacetList(final Multimap<String, String> filters, final int limit
			, final Integer geoHashPrecision, final String facet, final String lang) throws Transl8Exception {

		final Set<Aggregation> result = new LinkedHashSet<Aggregation>();
		if (facet == null || facet.isEmpty()) {
			result.addAll(getCategorySpecificFacets(filters, limit, lang));

			boolean placeFacetSelected = false;
			String[] placeFacets = NestedTermsAggregation.PLACE_FACETS;
		    for (String facetName : placeFacets) {
		        placeFacetSelected = filters.containsKey(facetName) | placeFacetSelected;
		    }

			for (final String facetName : getDefaultFacetList()) {
			    if (facetName.equals("facet_geo")) {
			        result.add(new TermsAggregation(facetName, 1000));
			    } else if (placeFacetSelected && Arrays.asList(placeFacets).contains(facetName)) {
		            Map<String, String> selectedPlaceFacets = new HashMap<String, String>();
		            for (String filter : filters.keySet()) {

		                if (Arrays.asList(placeFacets).contains(filter)) {
		                    selectedPlaceFacets.put(filter, (String) filters.get(filter).toArray()[0]); //TODO
		                }
		            }
		            result.add(new NestedTermsAggregation(facetName, limit, selectedPlaceFacets));
                } else {
			        result.add(new TermsAggregation(facetName, limit));
			    }
			}

			// TODO look for a more general way to handle dynamic facets
			int highestLevel = 0;
			boolean isFacetSubkategorieBestandPresent = false;
			for (final String filter : filters.keySet()) {
				if (filter.startsWith("facet_subkategoriebestand_level")) {
					isFacetSubkategorieBestandPresent = true;
					final int level = extractLevelFromFilter(filter);
					highestLevel = (level >= highestLevel) ? level : highestLevel;
				}
			}

			if (highestLevel > 0) {
				final String name = "facet_subkategoriebestand_level" + (highestLevel + 1);
				result.add(new TermsAggregation(name, limit));
			}

			if (filters.containsKey("facet_bestandsname") && !isFacetSubkategorieBestandPresent) {
				final String name = "facet_subkategoriebestand_level1";
				result.add(new TermsAggregation(name, limit));
			}

			// aggregations - if more aggregation types are used this should perhaps be moved to its own method

			// geo grid
			if (geoHashPrecision > 0) {
			    result.add(new NestedGeoHashGridAggregation(GeoHashGridAggregation.GEO_HASH_GRID_NAME
			            , GeoHashGridAggregation.GEO_HASH_GRID_FIELD, geoHashPrecision, 0));
			}
		} else {
			result.add(new TermsAggregation(facet, limit));
		}

		return result;
	}

	/**
	 * Extracts the category specific facets from the corresponding xml file.
	 * @param filters The list of facets including their values. The facet Aggregation.CATEGORY_FACET must be present to get
	 * any results.
	 * @param limit The maximum number of distinct facet values returned.
	 * @return The list of category specific facets or <code>null</code>.
	 * @throws Transl8Exception if transl8 cannot be reached.
	 */
	private Set<Aggregation> getCategorySpecificFacets(final Multimap<String, String> filters, final int limit, final String lang) throws Transl8Exception {

		final Set<Aggregation> result = new LinkedHashSet<Aggregation>();
		Collection<String> categories = filters.get(TermsAggregation.CATEGORY_FACET);

		if (!filters.isEmpty() && !categories.isEmpty()) {
			final String category = ts.categoryLookUp(categories.iterator().next(), lang);
			final Set<String> facets = xmlConfigUtil.getFacetsFromXMLFile(category);
			for (String facet : facets) {
			    facet = "facet_" + facet;
			    //Skip place facets, they will be added later because they may have to be nested
	            boolean placeFacetSelected = false;
	            String[] placeFacets = NestedTermsAggregation.PLACE_FACETS;
	            for (String facetName : placeFacets) {
	                placeFacetSelected = filters.containsKey(facetName) | placeFacetSelected;
	            }
			    if (placeFacetSelected && Arrays.asList(placeFacets).contains(facet)) {
			        continue;
			    } else {
			        result.add(new TermsAggregation(facet, limit));
			    }
			}
		}

		return result;
	}

	/**
	 * Builds the elasticsearch query based on the input parameters. It also adds an access control filter to the query.
	 * The final query is a function score query that modifies the score based on the boost value of a document.
	 * Embedded is a filtered query to account for access control, facet and bounding box filters
	 * which finally uses a simple query string query with 'AND' as default operator.<br>
	 * If the search parameter is numeric the query is performed against all configured fields else the query is only
	 * performed against the text fields.<br>
	 * If the user is an editor the editorSection and datasetGroup fields are searched, too.
	 * @param searchParam The query string.
	 * @param filters The filters to create a filter query from.
	 * @param bbCoords An array representing the top left and bottom right coordinates of a bounding box (order: lat, long)
	 * @param disableAccessControl If the access control query shall be replaced with a match all query
	 * @return An elasticsearch <code>QueryBuilder</code> which in essence is a complete elasticsearch query.
	 */
	private QueryBuilder buildQuery(String searchParam
			, final Multimap<String, String> filters
			, final Double[] bbCoords
			, final boolean disableAccessControl) {

		QueryBuilder facetFilter;
		if (!disableAccessControl) {
			facetFilter = esService.getAccessControlFilter();
		} else {
			facetFilter = QueryBuilders.matchAllQuery();
		}

		QueryBuilder placeFilter = QueryBuilders.matchAllQuery();

		if (filters != null && !filters.isEmpty()) {
		    boolean nestedFilterInserted = false;
            for (final Map.Entry<String, Collection<String>> filter: filters.asMap().entrySet()) {
                // TODO find a way to unify this
                if (filter.getKey().equals(GeoHashGridAggregation.GEO_HASH_GRID_NAME)) {
					QueryBuilder geoFilter = QueryBuilders
						.geoBoundingBoxQuery(GeoHashGridAggregation.GEO_HASH_GRID_FIELD)
						.setCorners(filter.getValue().iterator().next());
					QueryBuilder nestedGeoFilter = QueryBuilders.nestedQuery("places", geoFilter, ScoreMode.Avg);
					facetFilter = QueryBuilders.boolQuery().must(facetFilter).must(nestedGeoFilter);
                } else {
                    if (filter.getKey().equals("facet_ortsangabe") &&
                            (!filters.get("facet_ort").isEmpty() ||
                            !filters.get("facet_land").isEmpty() ||
                            !filters.get("facet_antikeroemprovinz").isEmpty() ||
                            !filters.get("facet_antikegriechlandschaft").isEmpty()) &&
                            !nestedFilterInserted
                    ) {
                        String searchTerm = "";
                        try {
                            searchTerm = filters.get("facet_ort").toArray()[0].toString();
                        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                            try {
                                searchTerm = filters.get("facet_antikeroemprovinz").toArray()[0].toString();
                            } catch (java.lang.ArrayIndexOutOfBoundsException e2) {}
                            try {
                                searchTerm = filters.get("facet_antikegriechlandschaft").toArray()[0].toString();
                            } catch (java.lang.ArrayIndexOutOfBoundsException e2) {}
                            try {
                                searchTerm = filters.get("facet_land").toArray()[0].toString();
                            } catch (java.lang.ArrayIndexOutOfBoundsException e2) {}
                        }

                        String relation = filters.get("facet_ortsangabe").toArray()[0].toString();

                        placeFilter = QueryBuilders.boolQuery()
                                .must(QueryBuilders.matchPhraseQuery("places.name", searchTerm))
                                .must(QueryBuilders.matchPhraseQuery("places.relation", relation));

                        facetFilter = QueryBuilders.boolQuery().must(facetFilter).must(
                                QueryBuilders.termsQuery(filter.getKey(), filter.getValue())).must(
                                        QueryBuilders.nestedQuery("places", placeFilter, ScoreMode.Avg));
                        nestedFilterInserted = true;
                    } else {
                        facetFilter = QueryBuilders.boolQuery().must(facetFilter).must(
                                QueryBuilders.termsQuery(filter.getKey(), filter.getValue()));
                    }
                }
            }
        }

		final QueryStringQueryBuilder innerQuery;
		QueryBuilder nestedQuery = null;

		if (searchParam.contains("places.gazetteerId")) {
		    String newSearchParam = "";
		    String gazetteerID = searchParam.substring(searchParam.indexOf(":")+1);
		    if (searchParam.contains("AND places.gazetteerId")) {
		        newSearchParam = searchParam.substring(0, searchParam.indexOf(" AND places.gazetteerId"));
		    }
		    searchParam = newSearchParam;
		    if (searchParam.equals("")) {
		        searchParam = "*";
		    }

		    QueryBuilder matchQuery = QueryBuilders.matchQuery("places.gazetteerId", gazetteerID);
		    nestedQuery = QueryBuilders.nestedQuery("places", QueryBuilders.boolQuery().must(matchQuery), ScoreMode.Avg);
		}

        innerQuery = QueryBuilders.queryStringQuery(searchParam)
                .defaultOperator(Operator.AND)
                .analyzeWildcard(true);

		innerQuery.field("searchableEditorContent", 0.5f);
		innerQuery.field("datasetGroup", 0.5f);

		for (Map.Entry<String, Float> entry : searchFields.text().entrySet()) {
			innerQuery.field(entry.getKey(), entry.getValue());
		}

		if (StringUtils.isNumeric(searchParam)) {
			for (Map.Entry<String, Float> entry : searchFields.numeric().entrySet()) {
				innerQuery.field(entry.getKey(), entry.getValue());
			}
		}

		final QueryBuilder filteredQuery;
		if (bbCoords != null && bbCoords.length == 4) {
            GeoBoundingBoxQueryBuilder bBoxFilter =
		            QueryBuilders.geoBoundingBoxQuery("places.location")
						.setCorners(bbCoords[0], bbCoords[1], bbCoords[2], bbCoords[3]);
		    QueryBuilder nestedFilter = QueryBuilders.nestedQuery("places", bBoxFilter, ScoreMode.Avg);
			BoolQueryBuilder andFilter = QueryBuilders.boolQuery().must(facetFilter).must(nestedFilter);
			if (nestedQuery != null) {
			    filteredQuery = QueryBuilders.boolQuery().must(innerQuery).must(nestedQuery).filter(andFilter);
			} else {
			    filteredQuery = QueryBuilders.boolQuery().must(innerQuery).filter(andFilter);
			}
		} else {
		    if (nestedQuery != null) {
		        filteredQuery = QueryBuilders.boolQuery().must(innerQuery).must(nestedQuery).filter(facetFilter);
		    } else {
		        filteredQuery = QueryBuilders.boolQuery().must(innerQuery).filter(facetFilter);
		    }
		}

		final FieldValueFactorFunctionBuilder scoreFunction = ScoreFunctionBuilders
				.fieldValueFactorFunction("boost")
				.missing(1);
		final QueryBuilder query = QueryBuilders
			.functionScoreQuery(filteredQuery, scoreFunction)
			.boostMode(CombineFunction.MULTIPLY);

		LOGGER.debug("Elastic search query part: " + query.toString());
		return query;
	}

	/**
	 * Builds the context query.
	 * @param entityId
	 * @return The query to retrieve the connected entities.
	 */
	private QueryBuilder buildContextQuery(Long entityId) {

		BoolQueryBuilder accessFilter = esService.getAccessControlFilter();
		final TermQueryBuilder innerQuery = QueryBuilders.termQuery("connectedEntities", entityId);
		final QueryBuilder query = QueryBuilders.boolQuery().must(innerQuery).filter(accessFilter);

		LOGGER.debug("Elastic search query context: " + query.toString());
		return query;
	}

	private SearchResultFacet getSearchResultFacet(final String facetName, final Map<String, Long> facetMap,
												   final String category) {


		final SearchResultFacet facetInfo = xmlConfigUtil.getFacetInfo(category, facetName);

		// @ TODO instead maybe just clone facetInfo, since it already has the right type and so on
		final SearchResultFacet result = new SearchResultFacet(
			facetName,
			(facetInfo != null) ? facetInfo.getGroup() : null,
			(facetInfo != null) ? facetInfo.getDependsOn() : null
		);

		for (final Map.Entry<String, Long> entry: facetMap.entrySet()) {
			final SearchResultFacetValue facetValue =
					new SearchResultFacetValue(entry.getKey(), entry.getKey(), entry.getValue());
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
