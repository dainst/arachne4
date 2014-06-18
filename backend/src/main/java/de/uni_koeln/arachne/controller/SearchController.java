package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.SearchResult;
import de.uni_koeln.arachne.response.StatusResponse;
import de.uni_koeln.arachne.service.SearchService;


/**
 * Handles http requests (currently only get) for <code>/search<code>.
 */
@Controller
public class SearchController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
	
	@Autowired
	private transient SearchService searchService;
	
	private transient final List<String> defaultFacetList; 
	
	private transient final int defaultFacetLimit;
	
	private transient final int defaultLimit;
	
	@Autowired
	public SearchController(final @Value("#{config.esDefaultLimit}") int defaultLimit,
			final @Value("#{config.esDefaultFacetLimit}") int defaultFacetLimit,
			final @Value("#{config.esDefaultFacetList}") String defaultFacetListCS) {
		
		this.defaultLimit = defaultLimit;
		this.defaultFacetLimit = defaultFacetLimit;
		defaultFacetList = new ArrayList<String>(Arrays.asList(defaultFacetListCS.split(",")));
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
	 * @param searchParam The value of the search parameter. (mandatory)
	 * @param limit The maximum number of returned entities. (optional)
	 * @param offset The offset into the list of entities (used for paging). (optional)
	 * @param filterValues The values of the elasticsearch filter query. (optional)
	 * @param facetLimit The maximum number of returned facets. (optional)
	 * @return A response object containing the data or a status response (this is serialized to JSON; XML is not supported).
	 */
	@RequestMapping(value="/search", method=RequestMethod.GET, produces="application/json")
	public @ResponseBody Object handleSearchRequest(@RequestParam("q") final String searchParam,
													  @RequestParam(value = "limit", required = false) final Integer limit,
													  @RequestParam(value = "offset", required = false) final Integer offset,
													  @RequestParam(value = "fq", required = false) final String filterValues,
													  @RequestParam(value = "fl", required = false) final Integer facetLimit) {
		
		final int resultSize = limit == null ? defaultLimit : limit;
		final int resultOffset = offset == null ? 0 : offset;
		final int resultFacetLimit = facetLimit == null ? defaultFacetLimit : facetLimit;
		
		final List<String> facetList = new ArrayList<String>(defaultFacetList);
		final List<String> filterValueList = searchService.getFilterValueList(filterValues, facetList);
		
		final SearchRequestBuilder searchRequestBuilder = searchService.buildSearchRequest(searchParam, resultSize, resultOffset, filterValueList);
		searchService.addFacets(facetList, resultFacetLimit, searchRequestBuilder);
		
		final SearchResult searchResult = searchService.executeSearchRequest(searchRequestBuilder, resultSize, resultOffset, filterValues, facetList);
		
		if (searchResult == null) {
			LOGGER.error("Search result is null!");
			return new StatusResponse("There was a problem executing the search. Please try again. If the problem persists please contact us.");
		} else {
			return searchResult;
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
			@RequestParam(value = "fq", required = false) final String filterValues,
			@RequestParam(value = "fl", required = false) final Integer facetLimit) {
		
		return handleSearchRequest("connectedEntities:" + entityId, limit, offset, filterValues, facetLimit);
	}
	
}
