package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
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
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.arachne.dao.hibernate.ArachneEntityDao;
import de.uni_koeln.arachne.mapping.hibernate.ArachneEntity;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.QuantificationContent;
import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.service.SearchService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.util.EntityId;

/**
 * 
 * Project-specific Controller, handles Ceramalex-specific requests
 * @author Patrick Gunia
 * @author Reimar Grabowski
 */

@Controller
public class CeramalexController  {

	private static final Logger LOGGER = LoggerFactory.getLogger(CeramalexController.class);
	
	@Autowired
	private transient ArachneEntityDao arachneEntityDao; 
	
	@Autowired
	private transient SingleEntityDataService singleEntityDataService;
	
	@Autowired
	private transient SearchService searchService;
		
	private transient final Integer defaultFacetLimit;
	
	private static final String FOREIGN_KEY_LABEL = "mainabstract.FS_QuantitiesID";
		
	@Autowired
	public CeramalexController(final @Value("#{config.esDefaultFacetLimit}") int defaultFacetLimit) {
		this.defaultFacetLimit = defaultFacetLimit;
	}
	
	/**
	 * Method handles a Ceramalex-quantify-request. It uses the regular elasticsearch query- and facet-parameters to first receive a list of mainabstract-records
	 * and afterwards retrieves a list of all avaiable quantities-records connected with them. These are summed and passed back as JSP which can then be rendered 
	 * by the frontend.
	 * @param searchParam The value of the search parameter. (mandatory)
	 * @param filterValues The values of the elasticsearch filter query. (optional)
	 * @param facetLimit The maximum number of facets. (optional)
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
		
		final SearchRequestBuilder searchRequestBuilder = searchService.buildSearchRequest(searchParam, maxResultSize
				, resultOffset, filterValueList, null, false, null);
		searchService.addFacets(facetList, resultFacetLimit, searchRequestBuilder);
			
		final SearchResult searchResult = searchService.executeSearchRequest(searchRequestBuilder, maxResultSize
				, resultOffset, filterValueList, facetList);
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
						
			// get complete entity information
			final ArachneEntity arachneEntity = arachneEntityDao.getByEntityID(entity.getEntityId());
				
			// only process mainabstract-records, skip any other
			if(arachneEntity == null || !"mainabstract".equals(arachneEntity.getTableName())) {
				entityIter.remove();
				continue;
			}
			
			// construct EntityId to use dataMapDao
			final EntityId entityId = new EntityId(arachneEntity);
			final Dataset localDataset = singleEntityDataService.getSingleEntityByArachneId(entityId);
			final Map<String, String> datasetFields = localDataset.getFields();
	
			// does the mainabstract have a quantification-record connected?
			final String foreignKeyQuantification = datasetFields.get(FOREIGN_KEY_LABEL);
			LOGGER.debug("Requesting Data for Mainabstract " + entityId.getArachneEntityID() + ", Quantity-Record: " + foreignKeyQuantification);
			
			if(foreignKeyQuantification == null) {
				entityIter.remove();
				continue;
			}
			quantities.add(new QuantificationContent(datasetFields));		
		}
		
		// no result
		if(quantities.isEmpty()) {
			message = "The search result contains no conntected quantification records.";
			modelMap.put("containsContent", false);
		}
		
		// compute result-map and pass it back
		else {
			final QuantificationContent result = computeAggregation(quantities);
			message = "Aggregated quantification of " + quantities.size() + " quantity-records connected with the records matching your search request.";
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
	private QuantificationContent computeAggregation(final List<QuantificationContent> quantities) {
		
		// only one record? no computations necessary...
		if (quantities.size() == 1) {
			return quantities.get(0);
		}
		// else add all records to the result-instance
		else {
			final QuantificationContent result = new QuantificationContent();
			for (final QuantificationContent cur : quantities) {
				result.add(cur);
			}
			return result;
		}
	}
}
