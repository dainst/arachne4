package de.uni_koeln.arachne.controller;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.response.search.SearchResultFacetValue;
import de.uni_koeln.arachne.service.SearchService;
import de.uni_koeln.arachne.util.search.SearchParameters;

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
	
	private transient final int defaultFacetLimit;
	
	private transient final int defaultLimit;
	
	@Autowired
	public SearchController(final @Value("#{config.esDefaultLimit}") int defaultLimit,
			final @Value("#{config.esDefaultFacetLimit}") int defaultFacetLimit) {
		
		this.defaultLimit = defaultLimit;
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
	 * @param queryString The value of the search parameter. (mandatory)
	 * @param limit The maximum number of returned entities. (optional)
	 * @param offset The offset into the list of entities (used for paging). (optional)
	 * @param filterValues The values of the elasticsearch filter query. (optional)
	 * @param facetLimit The maximum number of returned facets. (optional)
	 * @param SortField The field to sort on. Must be one listed in esSortFields in application.properties. (optional)
	 * @param desOrder If the sort order should be descending. The default order is ascending. (optional)
	 * @param boundingBox A String with comma separated coordinates representing the top left and bottom right 
	 * coordinates of a bounding box; order: lat, lon (optional)
	 * @param ghprec The geoHash precision; a value between 1 and 12. (optional)
	 * @param sortfacet The names of the facets that should be sorted alphabetically. (optional)
	 * @return A response object containing the data or a status response (this is serialized to JSON; XML is not supported).
	 */
	@RequestMapping(value="/search", method=RequestMethod.GET, produces="application/json;charset=UTF-8")
	public @ResponseBody ResponseEntity<?> handleSearchRequest(@RequestParam("q") final String queryString,
			@RequestParam(value = "limit", required = false) final Integer limit,
			@RequestParam(value = "offset", required = false) final Integer offset,
			@RequestParam(value = "fq", required = false) final String[] filterValues,
			@RequestParam(value = "fl", required = false) final Integer facetLimit,
			@RequestParam(value = "sort", required = false) final String sortField,
			@RequestParam(value = "desc", required = false) final Boolean orderDesc,
			@RequestParam(value = "bbox", required = false) final Double[] boundingBox,
			@RequestParam(value = "ghprec", required = false) final Integer geoHashPrecision,
			@RequestParam(value = "sortfacet", required = false) final String[] facetsToSort) {
		
		final int resultSize = limit == null ? defaultLimit : limit;
		final int resultFacetLimit = facetLimit == null ? defaultFacetLimit : facetLimit;
		
		final SearchParameters searchParameters = new SearchParameters() 
				.setQuery(queryString)
				.setLimit(resultSize)
				.setOffset(offset)
				.setFacetLimit(resultFacetLimit)
				.setSortField(sortField)
				.setOrderDesc(orderDesc)
				.setBoundingBox(boundingBox)
				.setGeoHashPrecision(geoHashPrecision);
		
		Multimap<String, String> filters = HashMultimap.create();
		if (filterValues != null) {
			filters = searchService.getFilters(Arrays.asList(filterValues), searchParameters.getGeoHashPrecision());
		}
		
		if (boundingBox != null && searchParameters.getBoundingBox().length > 0) {
			return ResponseEntity.badRequest().body("{ \"message\": \"Invalid bounding box coordinates.\"");
		}
				
		final SearchRequestBuilder searchRequestBuilder = searchService.buildDefaultSearchRequest(searchParameters
				, filters);
				
		final SearchResult searchResult = searchService.executeSearchRequest(searchRequestBuilder
				, resultSize, searchParameters.getOffset(), filters);
		
		if (searchResult.getStatus() != RestStatus.OK) {
			return ResponseEntity.status(searchResult.getStatus().getStatus()).build();
		} else {
			return ResponseEntity.ok().body(searchResult);
		}
	}
	
	/**
	 * Handles the HTTP request by querying the elasticsearch index for contexts of a given entity and returning the result.
	 * <br> 
	 * <br>
	 * The search result can only be serialized to JSON as JAXB cannot handle Maps.
	 * @param searchParam The value of the search parameter. (mandatory)
	 * @param limit The maximum number of returned entities. (optional)
	 * @param offset The offset into the list of entities (used for paging). (optional)
	 * @param filterValues The values of the elasticsearch filter query. (optional)
	 * @param facetLimit The maximum number of returned facets. (optional)
	 * @return A response object containing the data or a status response (this is serialized to JSON; XML is not supported).
	 */
	@RequestMapping(value="/contexts/{entityId}", method=RequestMethod.GET, produces="application/json")
	public @ResponseBody Object handleContextRequest(@PathVariable("entityId") final Long entityId,
			@RequestParam(value = "limit", required = false) final Integer limit,
			@RequestParam(value = "offset", required = false) final Integer offset,
			@RequestParam(value = "fq", required = false) final String[] filterValues,
			@RequestParam(value = "fl", required = false) final Integer facetLimit,
			  @RequestParam(value = "sort", required = false) final String sortField,
			  @RequestParam(value = "desc", required = false) final Boolean orderDesc) {

		final int resultSize = limit == null ? defaultLimit : limit;
		final int resultOffset = offset == null ? 0 : offset;
		final int resultFacetLimit = facetLimit == null ? defaultFacetLimit : facetLimit;

		Multimap<String, String> filters = HashMultimap.create();
		if (filterValues != null) {
			filters = searchService.getFilters(Arrays.asList(filterValues), 0);
		}
		
		final SearchRequestBuilder searchRequestBuilder = searchService.buildContextSearchRequest(entityId
				, resultSize, resultOffset, filters, resultFacetLimit, sortField, orderDesc);
				
		final SearchResult searchResult = searchService.executeSearchRequest(searchRequestBuilder, resultSize
				, resultOffset, filters);
		
		if (searchResult == null) {
			LOGGER.error("Search result is null!");
			return new ResponseEntity<String>(HttpStatus.SERVICE_UNAVAILABLE);
		} else {
			return searchResult;
		}
	}
	
	/**
	 * Returns a list of all distinct values of the given facet. The list is ordered alphabetically. A sublist can be 
	 * requested with the 'group' HTTP parameter. Supported values are:<br>
	 * '<' for all values with initial letters lower than numeric (actually lower than '0').<br>
	 * '$' for all values with a numeric initial letter.<br>
	 * 'a'..'z' for all values starting with the corresponding letter.<br>
	 * '>' for all values with intial letter greater than alphabetic (actually greater than 'zzz').<br>   
	 * @param facetName The name of the facet to get the values for.
	 * @param group A single char indicating which group to retrieve.
	 * @return The ordered list of values as JSON array.
	 */
	@RequestMapping(value="/index/{facetName}", method=RequestMethod.GET, produces="application/json;charset=UTF-8")
	public @ResponseBody ResponseEntity<?> handleIndexRequest(@PathVariable("facetName") final String facetName
			, @RequestParam(value = "group", required = false) Character groupMarker) {
		
		if (facetName.startsWith("facet_") || facetName.startsWith("agg_")) {
			final SearchRequestBuilder searchRequestBuilder = searchService.buildIndexSearchRequest(facetName);

			final SearchResult searchResult = searchService.executeSearchRequest(searchRequestBuilder
					, 0, 0, null);

			if (searchResult.getStatus() == RestStatus.OK) {
				if (searchResult.facetSize() != 1) {
					return ResponseEntity.badRequest().build();
				}
				List<String> result = new ArrayList<String>();
				
				final SearchResultFacet facet = searchResult.getFacets().get(0);
				final List<SearchResultFacetValue> values = facet.getValues();
				
				for (SearchResultFacetValue searchResultFacetValue : values) {
					result.add(searchResultFacetValue.getValue());
				}
				
				// sort alphabetically
				final Collator collator = Collator.getInstance();
				Collections.sort(result, collator);
				
				if (groupMarker != null) {
					result = getSubList(result, groupMarker);
				}
				
				return ResponseEntity.ok().body(result);
			} else {
				return ResponseEntity.status(searchResult.getStatus().getStatus()).build();
			}
		}
		return ResponseEntity.badRequest().build();
	}

	/**
	 * Generates a sublist from the given list. The sublist to get is defined by the marker.
	 * @param inputList The list to get a sublist from.
	 * @param marker A marker indicating which sublist to generate.
	 * @return The sublist.
	 */
	private List<String> getSubList(final List<String> inputList, final char marker) {
		final List<String> result = new ArrayList<String>();
		final Collator collator = Collator.getInstance();
		
		if (marker == '<' || marker == '$' || (Character.isLetter(marker) && Character.isLowerCase(marker))) {
			String lowerLimit = "";
			String upperLimit = "";

			switch (marker) {
			case '<':
				upperLimit = "0";
				break;

			case '$':
				lowerLimit = "0";
				upperLimit = "a";
				break;
				
			case 'z':
				lowerLimit = Character.toString(marker);
				upperLimit = "zzz";
				break;
				
			default:
				lowerLimit = Character.toString(marker);
				upperLimit = Character.toString((char)(marker + 1));
				break;
			}

			for (final String value : inputList) {
				if (collator.compare(value, upperLimit) < 0 && collator.compare(value, lowerLimit) >= 0) {
					result.add(value);
				}
			}
		} else {
			if (marker == '>') {
				for (final String value : inputList) {
					if (collator.compare(value, "zzz") > 0) {
						result.add(value);
					}
				}
			}
		}

		return result;
	}
}
