package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.arachne.dao.ArachneEntityDao;
import de.uni_koeln.arachne.dao.DataMapDao;
import de.uni_koeln.arachne.mapping.ArachneEntity;
import de.uni_koeln.arachne.response.QuantificationContent;
import de.uni_koeln.arachne.response.SearchHit;
import de.uni_koeln.arachne.response.SearchResult;
import de.uni_koeln.arachne.response.StatusResponse;
import de.uni_koeln.arachne.service.SearchService;
import de.uni_koeln.arachne.util.EntityId;

/**
 * 
 * Project-specific Controller, handles Ceramalex-specific requests
 * @author Patrick Gunia
 *
 */

@Controller
public class CeramalexController  {

	private static final Logger LOGGER = LoggerFactory.getLogger(CeramalexController.class);
	
	@Autowired
	private transient ArachneEntityDao arachneEntityDao; 
	
	@Autowired
	private transient DataMapDao dataMapDao;
	
	@Autowired
	private transient SearchService searchService;
	
	private transient Integer defaultLimit;
	
	private transient Integer defaultFacetLimit;
	
	private transient String foreignKeyLabel = "FS_QuantitiesID";
		
	@Autowired
	public CeramalexController(final @Value("#{config.esDefaultLimit}") int defaultLimit,
			final @Value("#{config.esDefaultFacetLimit}") int defaultFacetLimit) {
		this.defaultLimit = defaultLimit;
		this.defaultFacetLimit = defaultFacetLimit;
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
	@RequestMapping(value="/project/ceramalex/quantify", method=RequestMethod.GET)
	public ModelAndView handleSearchRequest(@RequestParam("q") final String searchParam,
													  @RequestParam(value = "fq", required = false) final String filterValues,
													  @RequestParam(value = "fl", required = false) final Integer facetLimit,
													  final HttpServletResponse response) {

		final int resultFacetLimit = facetLimit == null ? defaultFacetLimit : facetLimit;
		final ModelMap modelMap = new ModelMap();
		modelMap.put("facets", filterValues);
		modelMap.put("searchParam", searchParam);
		
		final List<String> facetList = new ArrayList<String>();
		final List<String> filterValueList = searchService.getFilterValueList(filterValues, facetList);
		
		final Integer maxResultSize = 1000000;
		final Integer resultOffset = 0;
		
		final SearchRequestBuilder searchRequestBuilder = searchService.buildSearchRequest(searchParam, maxResultSize, resultOffset, filterValueList);
		searchService.addFacets(facetList, resultFacetLimit, searchRequestBuilder);
			
		final SearchResult searchResult = searchService.executeSearchRequest(searchRequestBuilder, maxResultSize, resultOffset, filterValues, facetList);
		LOGGER.debug("#Found records: " + searchResult.getSize());

		if (searchResult == null || searchResult.getEntities() == null) {
			final String message = "There was a problem executing the search. Please try again. If the problem persists please contact us.";
			modelMap.put("message", message);
			modelMap.put("containsContent", false);
			return new ModelAndView("ceramalexQuantification", modelMap);
		}
		
		// iterate over all found entities and retrieve the quantification-records for each mainabstract
		final List<SearchHit> entities = searchResult.getEntities();
		final Iterator<SearchHit> entityIter = entities.iterator();
		final List<QuantificationContent> quantities = new ArrayList<QuantificationContent>();
		String message;
		
		while(entityIter.hasNext()) {
			final SearchHit entity = entityIter.next();
			final Long id = entity.getEntityId();
			
			// get complete entity information
			final ArachneEntity arachneEntity = arachneEntityDao.getByEntityID(id);
			// only process mainabstract-records, skip any other
			if(arachneEntity == null || !"mainabstract".equals(arachneEntity.getTableName())) {
				entityIter.remove();
				continue;
			}
			
			// construct EntityId to use dataMapDao
			final EntityId entityId = new EntityId(arachneEntity);
			final Map<String, String> entityData = dataMapDao.getById(entityId);
			
			LOGGER.debug(entityData.toString());
			
			// does the mainabstract have a quantification-record connected?
			final String foreignKeyQuantification = entityData.get(foreignKeyLabel);
			LOGGER.debug("Requesting Data for Mainabstract " + entityId.getArachneEntityID() + ", Quantity-Record: " + foreignKeyQuantification);
			
			if(foreignKeyQuantification == null) {
				entityIter.remove();
				continue;
			}
			
			// get complete quantification record
			final Map<String, String> quantificationData = dataMapDao.getByPrimaryKeyAndTable(Integer.valueOf(foreignKeyQuantification), "quantities");
			final QuantificationContent quantityRecord = new QuantificationContent(quantificationData);
			quantities.add(new QuantificationContent(quantificationData));
		}
		
		// no result
		if(quantities.isEmpty()) {
			message = "The search result contains no conntected quantification records.";
			modelMap.put("containsContent", true);
		}
		
		// compute result-map and pass it back
		else {
			final QuantificationContent result = computeAggregation(quantities);
			message = "Aggregated quantification of " + quantities.size() + " connected quantity-records.";
			modelMap.putAll(result.getAsMap());
			modelMap.put("containsContent", true);
		}
		
		modelMap.put("message", message);
		LOGGER.debug("Finished Quantity-Processing");
		return new ModelAndView("ceramalexQuantification", modelMap);
	}

	/**
	 * Method gets a list of all available quantification records and computes the aggregation of the single components.  
	 * @param quantities List of QuantificationContent-instances which are used for the aggregation-computations
	 * @return QuantificationContent Result-instance holding the results of the aggregation 
	 */
	private QuantificationContent computeAggregation(
			final List<QuantificationContent> quantities) {
		
		// only one record? no computations necessary...
		if(quantities.size() == 1) {
			return quantities.get(0);
		}
		// else add all records to the result-instance
		else {
			final QuantificationContent result = new QuantificationContent();
			for(QuantificationContent cur : quantities) {
				result.add(cur);
			}
			return result;
		}
	}
}
