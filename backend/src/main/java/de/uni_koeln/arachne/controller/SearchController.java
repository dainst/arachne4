package de.uni_koeln.arachne.controller;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uni_koeln.arachne.response.search.IndexResult;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.response.search.SearchResultFacetValue;
import de.uni_koeln.arachne.service.SearchService;
import de.uni_koeln.arachne.service.Transl8Service.Transl8Exception;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.search.SearchParameters;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * Handles HTTP search requests.
 *
 * @author Reimar Grabowski
 */
@Controller
public class SearchController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
	
	@Autowired
	private transient SearchService searchService;
	
	@Autowired
	private transient UserRightsService userRightsService;
	
	private transient final int defaultFacetLimit;
	
	private transient final int defaultLimit;
	
	/**
	 * Constructor setting the default limit and facet limit values.
	 ** @param defaultLimit The default search limit.
	 * @param defaultFacetLimit the default facet limit.
	 */
	@Autowired
	public SearchController(final @Value("${esDefaultLimit}") int defaultLimit,
			final @Value("${esDefaultFacetLimit}") int defaultFacetLimit) {

		this.defaultLimit = defaultLimit ;
		this.defaultFacetLimit = defaultFacetLimit;
	}

	/**
	 * Fix for a Spring problem to bind the correct array values to a String array if only one array assignment is
	 * present in the URL and the value includes commas.
	 * <br>
	 * For example:
	 * <br>
	 * URL: .../search?fq="facet_aufbewahrungsort:"Westgriechenland, Griechenland"
	 * <br>
	 * will be bound to the array like this:
	 * <br>
	 * filterValues[0] = facet_aufbewahrungsort:"Westgriechenland"
	 * filterValues[1] = Griechendland
	 * <br>
	 * instead of the correct way
	 * <br>
	 * filterValues[0] = facet_aufbewahrungsort:"Westgriechenland, Griechendland"
	 *
	 * @param binder A Spring <code>WebDataBinder</code> to register a custom editor on.
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	    binder.registerCustomEditor(String[].class, new StringArrayPropertyEditor(null));
	}

	/**
	 * Handles the http request by querying the Elasticsearch index and returning the search result.
	 * The "title" field is boosted by 2 so that documents containing the search keyword in the title are higher ranked than
	 *  documents containing the keyword in other fields.
	 * <br>
	 * The return type of this method is <code>Object</code> so that it can return either a <code>SearchResult</code> or a <code>
	 * StatusMessage</code>.
	 * <br>
	 * The search result can only be serialized to JSON as JAXB cannot handle Maps.
	 ** @param queryString The value of the search parameter. (mandatory)
	 * @param limit The maximum number of returned entities. (optional)
	 * @param offset The offset into the list of entities (used for paging). (optional)
	 * @param filterValues The values of the elasticsearch filter query. (optional)
	 * @param facetLimit The maximum number of returned facets. (optional)
	 * @param facetOffset An offset for the returned facets. (optional)
	 * @param sortField The field to sort on. Must be one listed in esSortFields in application.properties. (optional)
	 * @param orderDesc If the sort order should be descending. The default order is ascending. (optional)
	 * @param boundingBox A String with comma separated coordinates representing the top left and bottom right
	 * coordinates of a bounding box; order: lat, lon (optional)
	 * @param geoHashPrecision The geoHash precision; a value between 1 and 12. (optional)
	 * @param facetsToSort The names of the facets that should be sorted alphabetically. (optional)
	 * @param scrollMode If the ES scroll API should be used for the query (user must be logged in to allow this)
	 * (optional)
	 * @param facet If set only the values for this facet will be returned instead of a full search result. (optional)
	 * @param editorFields Whether the editor-only fields should be searched and highlighted (optional,
	 * default is <code>true</code>).
	 * @return A response object containing the data or a status response (this is serialized to JSON; XML is not supported).
	 */
	@RequestMapping(value="/search",
			method=RequestMethod.GET)

	public @ResponseBody ResponseEntity<?> handleSearchRequest(@RequestParam("q") final String queryString,
			@RequestParam(value = "limit", required = false) final Integer limit,
			@RequestParam(value = "offset", required = false) final Integer offset,
			@RequestParam(value = "fq", required = false) final String[] filterValues,
			@RequestParam(value = "fl", required = false) final Integer facetLimit,
			@RequestParam(value = "fo", required = false) final Integer facetOffset,
			@RequestParam(value = "sort", required = false) final String sortField,
			@RequestParam(value = "desc", required = false) final Boolean orderDesc,
			@RequestParam(value = "bbox", required = false) final Double[] boundingBox,
			@RequestParam(value = "ghprec", required = false) final Integer geoHashPrecision,
			@RequestParam(value = "sf", required = false) final String[] facetsToSort,
			@RequestParam(value = "scroll", required = false) final Boolean scrollMode,
			@RequestParam(value = "facet", required = false) final String facet,
			@RequestParam(value = "editorfields", required = false) Boolean editorFields,
		@RequestParam(value = "lang", required = false) final String lang,
            @RequestHeader(value = "Accept-Language", defaultValue = "de") String headerLanguage) {
		if (scrollMode != null && scrollMode && !userRightsService.isSignedInUser()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		editorFields = (editorFields == null) ? true : editorFields;

		final SearchParameters searchParameters = new SearchParameters(defaultLimit, defaultFacetLimit)
				.setQuery(queryString)
				.setLimit(limit)
				.setOffset(offset)
				.setFacetLimit(facetLimit)
				.setFacetOffset(facetOffset)
				.setSortField(sortField)
				.setOrderDesc(orderDesc)
				.setBoundingBox(boundingBox)
				.setGeoHashPrecision(geoHashPrecision)
				.setFacetsToSort(facetsToSort)
				.setFacet(facet)
				.setScrollMode(scrollMode)
				.setSearchEditorFields(editorFields &&
						userRightsService.userHasAtLeastGroupID(UserRightsService.MIN_ADMIN_ID));

		if (!searchParameters.isValid()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		Multimap<String, String> filters = HashMultimap.create();
		if (filterValues != null) {
			filters = searchService.getFilters(Arrays.asList(filterValues), searchParameters.getGeoHashPrecision());
		}

		int bbLength = searchParameters.getBoundingBox().length;
		if (boundingBox != null && bbLength != 4) {
			return ResponseEntity.badRequest().body("{ \"message\": \"Invalid bounding box coordinates.\"}");
		}

		SearchRequestBuilder searchRequestBuilder;
		try {
			searchRequestBuilder = searchService.buildDefaultSearchRequest(searchParameters
					, filters, "de");
			final SearchResult searchResult = searchService.executeSearchRequest(searchRequestBuilder
					, searchParameters.getLimit(), searchParameters.getOffset(), filters, searchParameters.getFacetOffset());

			if (searchResult.getStatus() != RestStatus.OK) {
				return ResponseEntity.status(searchResult.getStatus().getStatus()).build();
			} else {
				// scroll request cannot be fulfilled due to too many open scroll requests
				if (searchParameters.isScrollMode() && searchResult.getScrollId() == null) {
					HttpHeaders headers = new HttpHeaders();
					headers.set("Retry-after", "60");
					return new ResponseEntity<>("", headers, HttpStatus.TOO_MANY_REQUESTS);
				}
				return ResponseEntity.ok().body(searchResult);
			}
		} catch (Transl8Exception e) {
			LOGGER.error("Could not reach tranl8. Cause: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Handles the HTTP request by executing a scroll search request with the given <code>scrollId</code>. This means it
	 * pages the results of the original scroll search requests.
	 ** @param scrollId The scroll id of the original search.
	 * @return The next search result of the scroll search.
	 */
	@RequestMapping(value="/search/scroll/{scrollId}",
			method=RequestMethod.GET,
			produces={APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody ResponseEntity<?> handleSearchScrollRequest(@PathVariable("scrollId") final String scrollId) {
		final SearchResult searchResult = searchService.executeSearchScrollRequest(scrollId);
		
		if (searchResult.getStatus() != RestStatus.OK) {
			return ResponseEntity.status(searchResult.getStatus().getStatus()).build();
		} else {
			return ResponseEntity.ok().body(searchResult);
		}
	}

    /**
     * Handles the HTTP request by executing a completion suggest request on the elasticsearch index.
     *
     * @param queryString The prefix to find suggestions for.
     * @return A list of suggestions.
     */
    @RequestMapping(value = "/suggest",
            method = RequestMethod.GET,
            produces = {APPLICATION_JSON_UTF8_VALUE})
    public @ResponseBody
    ResponseEntity<?> handleSuggestRequest(@RequestParam("q") final String queryString) {
        return ResponseEntity.ok().body(searchService.executeSuggestRequest(queryString));
    }

    /**
     * Handles the HTTP request by querying the elasticsearch index for contexts of a given entity and returning the result.
     * <br>
     * <br>
     * The search result can only be serialized to JSON as JAXB cannot handle Maps.
     *
     * @param entityId     The entity id to retrieve contexts for. (mandatory)
     * @param limit        The maximum number of returned entities. (optional)
     * @param offset       The offset into the list of entities (used for paging). (optional)
     * @param filterValues The values of the elasticsearch filter query. (optional)
     * @param facetLimit   The maximum number of returned facets. (optional)
     * @param sortField    The field to sort results on.
     * @param orderDesc    Whether the result should be in descending (<code>true</code>) or ascending (<code>false</code>)
     *                     order.
     * @return A response object containing the data or a status response (this is serialized to JSON; XML is not supported).
     */
    @RequestMapping(value = "/contexts/{entityId}",
            method = RequestMethod.GET,
            produces = {APPLICATION_JSON_UTF8_VALUE})
    public @ResponseBody
    Object handleContextRequest(@PathVariable("entityId") final Long entityId,
                                @RequestParam(value = "limit", required = false) final Integer limit,
                                @RequestParam(value = "offset", required = false) final Integer offset,
                                @RequestParam(value = "fq", required = false) final String[] filterValues,
                                @RequestParam(value = "fl", required = false) final Integer facetLimit,
                                @RequestParam(value = "sort", required = false) final String sortField,
                                @RequestParam(value = "desc", required = false) final Boolean orderDesc,
			@RequestParam(value = "lang", required = false) final String lang,
            @RequestHeader(value = "Accept-Language", defaultValue = "de") String headerLanguage) {

        final int resultFacetLimit = facetLimit == null ? defaultFacetLimit : facetLimit;

        final SearchParameters searchParameters = new SearchParameters(defaultLimit, defaultFacetLimit)
                .setLimit(limit)
                .setOffset(offset)
                .setFacetLimit(resultFacetLimit)
                .setSortField(sortField)
                .setOrderDesc(orderDesc);

		Multimap<String, String> filters = HashMultimap.create();
		if (filterValues != null) {
			filters = searchService.getFilters(Arrays.asList(filterValues), 0);
		}
		
		SearchRequestBuilder searchRequestBuilder;
		try {
			searchRequestBuilder = searchService.buildContextSearchRequest(entityId
					, searchParameters, filters, (lang==null) ? headerLanguage : lang);
			final SearchResult searchResult = searchService.executeSearchRequest(searchRequestBuilder
					, searchParameters.getLimit(), searchParameters.getOffset(), filters, 0);

			if (searchResult == null) {
				LOGGER.error("Search result is null!");
				return new ResponseEntity<String>(HttpStatus.SERVICE_UNAVAILABLE);
			} else {
				return searchResult;
			}
		} catch (Transl8Exception e) {
			LOGGER.error("Could not reach transl8. Cause: ");
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}

	/**
	 * Returns a list of all distinct values of the given facet. The list is ordered alphabetically. A sublist can be
	 * requested with the 'group' HTTP parameter. Supported values are:<br>
	 * '<' for all values with initial letters lower than numeric (actually lower than '0').<br>
	 * '$' for all values with a numeric initial letter.<br>
	 * 'a'..'z' for all values starting with the corresponding letter.<br>
	 * '>' for all values with intial letter greater than alphabetic (actually greater than 'zzz').<br>
	 ** @param facetName The name of the facet to get the values for.
	 * @param groupMarker A single char indicating which group to retrieve.
	 * @return The ordered list of values as JSON array.
	 */
	@RequestMapping(value="/index/{categoryName}/{facetName}",
			method=RequestMethod.GET, 
			produces={APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody ResponseEntity<IndexResult> handleIndexRequest(@PathVariable("facetName") final String facetName, @PathVariable("categoryName") final String categoryName, @RequestParam(value = "group", required = false) Character groupMarker) {
		
		if (facetName.startsWith("facet_") || facetName.startsWith("agg_")) {

            Multimap<String, String> filters = HashMultimap.create();
            final String filterValue = "facet_kategorie:\"" + categoryName + "\"";
            final int splitIndex = filterValue.indexOf(':');
            final String name = filterValue.substring(0, splitIndex);
            final String value = filterValue.substring(splitIndex + 1).replace("\"", "");
            filters.put(name, value);

            final SearchRequestBuilder searchRequestBuilder = searchService.buildIndexSearchRequest(facetName, filters);

            final SearchResult searchResult = searchService.executeSearchRequest(searchRequestBuilder, 0, 0, filters, 0);

            if (searchResult.getStatus() == RestStatus.OK) {
                if (searchResult.facetSize() != 1) {
                    return new ResponseEntity<IndexResult>(HttpStatus.BAD_REQUEST);
                }
                IndexResult result = new IndexResult();

                final SearchResultFacet facet = searchResult.getFacets().get(0);
                final List<SearchResultFacetValue> values = facet.getValues();

                for (SearchResultFacetValue searchResultFacetValue : values) {
                    result.addValue(searchResultFacetValue.getValue());
                }

                if (groupMarker != null) {
                    result.reduce(groupMarker);
                }

                return ResponseEntity.ok().body(result);
            } else {
                return new ResponseEntity<IndexResult>(HttpStatus.valueOf(searchResult.getStatus().getStatus()));
            }
        }
        return new ResponseEntity<IndexResult>(HttpStatus.BAD_REQUEST);
    }
}
