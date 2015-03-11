package de.uni_koeln.arachne.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.service.DataImportService;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.ImageService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.service.Transl8Service;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.network.ESClientUtil;

/**
 * Handles http requests (currently only get) for <code>/entity<code> and <code>/data</code>.
 */
@Controller
public class EntityController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityController.class);
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService;
	
	@Autowired
	private transient EntityService entityService;

	@Autowired
	private transient SingleEntityDataService singleEntityDataService;
	
	@Autowired
	private transient ESClientUtil esClientUtil;
	
	@Autowired
	private transient ResponseFactory responseFactory;
	
	@Autowired
	private transient ImageService imageService;
		
	@Autowired
	private transient IUserRightsService userRightsService; 
	
	@Autowired
	private transient Transl8Service ts;
	
	@Autowired
	private transient DataImportService dataImportService;
	
	
	private transient String[] internalFields;
	
	@Autowired
	public EntityController(final @Value("#{config.internalFields}") String internalFieldsCS) {
		internalFields = internalFieldsCS.split(",");
	}
	
	@RequestMapping(value="/entity/count", method=RequestMethod.GET, produces="application/json;charset=UTF-8")
	public @ResponseBody String handleGetEntityCountRequest() {
		CountResponse countResponse = esClientUtil.getClient().prepareCount(esClientUtil.getSearchIndexAlias())
				.execute().actionGet();
		return "{\"entityCount\":" + countResponse.getCount() + "}";
	}
	
	/**
	 * Handles http request for /{entityId}.
	 * Requests for /entity/* return formatted data. This will be sent out either as JSON or as XML. The response format is set 
	 * by Springs content negotiation mechanism.
	 * @param entityId The unique entity id of the item to fetch.
     * @return A response object containing the data (this is serialized to JSON).
     */
	@RequestMapping(value="/entity/{entityId}", method=RequestMethod.GET, produces="application/json;charset=UTF-8")
	public @ResponseBody Object handleGetEntityIdRequest(
			@PathVariable("entityId") final Long entityId,
			@RequestParam(value = "live", required = false) final Boolean isLive,
			final HttpServletResponse response) {
		
		if (isLive != null && isLive) {
			return getEntityFromDB(entityId, null, response);
		} else {
			return getEntityFromIndex(entityId, null, response);
		}
	}
    
    /**
     * Handles http request for /{category}/{id}
     * Requests for /entity/* return formatted data. This will be sent out either as JSON or as XML. The response format is set 
	 * by Springs content negotiation mechanism.
     * @param category The database table to fetch the item from.
     * @param categoryId The internal id of the item to fetch
     * @return A response object containing the data (this is serialized to JSON).
     */
    @RequestMapping(value="/entity/{category}/{categoryId}", method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    public @ResponseBody Object handleGetCategoryIdRequest(
    		@PathVariable("category") final String category,
    		@PathVariable("categoryId") final Long categoryId,
    		@RequestParam(value = "live", required = false) final Boolean isLive,
    		final HttpServletResponse response) {
    	
    	LOGGER.debug("Request for category: " + category + " - id: " + categoryId);
    	if (isLive != null && isLive) {
			return getEntityFromDB(categoryId, category, response);
		} else {
			return getEntityFromIndex(categoryId, category, response);
		}
    }

    /**
     * Internal function handling all http GET requests for <code>/entity/*</code>.
     * It fetches the data for a given entity and returns it as a response object.
     * <br>
     * If the entity is not found a HTTP 404 error message is returned.
     * <br>
     * If the user does not have permission to see an entity a HTTP 403 status message is returned.
     * @param id The unique entity ID if no category is given else the internal ID.
     * @param category The category to query or <code>null</code>.
     * @param response The <code>HttpServeletRsponse</code> object.
     * @return The response body as <code>String</code>.
     */
    private String getEntityFromDB(final Long id, final String category //NOPMD
    		, final HttpServletResponse response) { 
    	
    	final Long startTime = System.currentTimeMillis();
    	    	
    	EntityId entityId;
    	if (category == null) {
    		entityId = entityIdentificationService.getId(id);
    	} else {
    		entityId = entityIdentificationService.getId(category, id);
    	}
    	
    	if (entityId == null) {
    		response.setStatus(404);
    		return null;
    	}
    	
    	LOGGER.debug("Request for entity: " + entityId.getArachneEntityID() + " - type: " + entityId.getTableName());
    	
    	if (entityId.isDeleted()) {
    		return responseFactory.createResponseForDeletedEntityAsJsonString(entityId);
    	}
    	
    	final String result = entityService.getFormattedEntityByIdAsJsonString(entityId);
    	
    	if ("forbidden".equals(result)) {
    		response.setStatus(403);
    		return null;
    	}
    	
    	if (result != null) {
    		LOGGER.debug("-----------------------------------");
    		LOGGER.debug("-- Complete response took " + (System.currentTimeMillis() - startTime) + " ms");
    		
    		return result;
    	}
    	response.setStatus(404);
    	return null;
    }
    
    /**
     * Internal function handling all http GET requests for <code>/entity/*</code>.
     * It fetches the data for a given entity from the elasticsearch index and returns it as a JSON or XML string.
     * <br>
     * Actually two queries are run on the elasticsearch index. One with and one without an access control filter.
     * If the query without an access filter returns a result and the other one doesn't HTTP status code is set to 403.
     * This should be faster than hitting the DB multiple times for access control.
     * @param id The unique entity ID if no category is given else the internal ID.
     * @param category The category to query or <code>null</code>.
     * @param response The <code>HttpServeletRsponse</code> object.
     * @return The response body as <code>String</code>.
     */
     private String getEntityFromIndex(final Long id, final String category //NOPMD
    		, final HttpServletResponse response) { 
    	
    	final Long startTime = System.currentTimeMillis();
    	    	
    	String result = null;
    	
    	SearchResponse searchResponse = null;
    	SearchResponse acLessSearchResponse = null;
    	final FilterBuilder accessFilter = esClientUtil.getAccessControlFilter();
    	
    	if (category == null) {
    		final QueryBuilder query = QueryBuilders.filteredQuery(QueryBuilders.termQuery("entityId", id), accessFilter);
    		LOGGER.debug("Entity query [" + id + "]: " + query);
    		searchResponse = esClientUtil.getClient().prepareSearch(esClientUtil.getSearchIndexAlias())
    				.setQuery(query)
    				.setFetchSource(new String[] {"*"}, internalFields)
    				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    				.setFrom(0)
    				.setSize(1)
    				.execute().actionGet();
    		
    		final QueryBuilder acLessQuery = QueryBuilders.termQuery("entityId", id);
    		LOGGER.debug("Entity query [" + id + "] (no access control): " + acLessQuery);
    		acLessSearchResponse = esClientUtil.getClient().prepareSearch(esClientUtil.getSearchIndexAlias())
    				.setQuery(acLessQuery)
    				.setFetchSource(new String[] {"*"}, internalFields)
    				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    				.setFrom(0)
    				.setSize(1)
    				.execute().actionGet();
    	} else {
    		final QueryBuilder query = QueryBuilders.filteredQuery(
    				QueryBuilders.boolQuery()
    					.must(QueryBuilders.termQuery("type", ts.transl8(category)))
    					.must(QueryBuilders.termQuery("internalId", id))
    				, accessFilter);
    		LOGGER.debug("Entity query [" + ts.transl8(category) + "/" + id + "]: " + query);
    		searchResponse = esClientUtil.getClient().prepareSearch(esClientUtil.getSearchIndexAlias())
    				.setQuery(query)
    				.setFetchSource(new String[] {"*"}, internalFields)
    				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    				.setFrom(0)
    				.setSize(1)
    				.execute().actionGet();
    		
    		final QueryBuilder acLessQuery = QueryBuilders.boolQuery()
					.must(QueryBuilders.termQuery("type", ts.transl8(category)))
					.must(QueryBuilders.termQuery("internalId", id));
    		LOGGER.debug("Entity query [" + ts.transl8(category) + "/" + id + "] (no access control): " + acLessQuery);
    		acLessSearchResponse = esClientUtil.getClient().prepareSearch(esClientUtil.getSearchIndexAlias())
    				.setQuery(acLessQuery)
    				.setFetchSource(new String[] {"*"}, internalFields)
    				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    				.setFrom(0)
    				.setSize(1)
    				.execute().actionGet();
    	}
    	
    	if (searchResponse.getHits().getTotalHits() == 1) { 
    		result = searchResponse.getHits().getAt(0).getSourceAsString();
    		response.setContentType("application/json");
    	} else {
    		if (acLessSearchResponse.getHits().getTotalHits() == 1) {
    			response.setStatus(403);
    		} else {
    			// if the entity is not found in the ES index it may have been deleted, so we try to retrieve it from 
    			// the DB to get a nice deleted message without duplicating code or burdening the dataimport with the 
    			// task of keeping track of deleted entities or adding them to the index
    			return getEntityFromDB(id, category, response);
    		}
    		result = null;
    	}
    	
    	LOGGER.debug("-----------------------------------");
    	LOGGER.debug("-- Complete response took " + (System.currentTimeMillis() - startTime) + " ms");
    	return result;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Handles http request for /doc/{id}
     * Not implemented!
     */
    @RequestMapping(value="/doc/{entityId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDocEntityRequest(@PathVariable("entityId") final Long entityId) {
    	// TODO implement me
    	return null;
    }

    /**
     * Handles http request for /doc/{category}/{id}
     * Not implemented!
     */
    @RequestMapping(value="doc/{category}/{categoryId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDocCategoryIdRequest(@PathVariable("category") final String category
    		, @PathVariable("categoryId") final Long categoryId) {
    	// TODO implement me
		return null;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Handles http request for /data/{id}.
     * Requests for /data/* return the raw data.
     * Depending on content negotiation either JSON or XML is returned.
     * @param entityId The unique entityID.
     * @param response The outgoing HTTP response.
     * @return The <code>Dataset</code> of the requested entity.
     */
    @RequestMapping(value="/data/{entityId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDataEntityRequest(@PathVariable("entityId") final Long entityId, final HttpServletResponse response) {
    	return getDataRequestResponse(entityId, null, response);
    }
    
    /**
     * Handles http request for /data/{category}/{id}.
     * Requests for /data/* return the raw data.
     * Depending on content negotiation either JSON or XML is returned.
     * @param categoryId The internal ID of the requested entity.
     * @param category The category to query.
     * @param response The outgoing HTTP response.
     * @return The <code>Dataset</code> of the requested entity
     */
    @RequestMapping(value="data/{category}/{categoryId}", method=RequestMethod.GET)
    public @ResponseBody Dataset handleGetDataCategoryIdRequest(@PathVariable("category") final String category
    		, @PathVariable("categoryId") final Long categoryId, final HttpServletResponse response) {
    	return getDataRequestResponse(categoryId, category, response);
    }
    
    /**
     * Internal function handling all http GET requests for <code>/data/*</code>.
     * It fetches the data for a given entity and returns the <code>Dataset</code>.
     * <br>
     * If the entity is not found a HTTP 404 error message is returned.
     * <br>
     * If the user does not have permission to see an entity a HTTP 403 status message is returned.
     * @param id The unique entity ID if no category is given else the internal ID.
     * @param category The category to query or <code>null</code>.
     * @param response The <code>HttpServeletRsponse</code> object.
     * @return The <code>Dataset</code> of the requested entity.
     */
    private Dataset getDataRequestResponse(final Long id, final String category, final HttpServletResponse response) { // NOPMD
    	final Long startTime = System.currentTimeMillis();
        
    	EntityId arachneId;
    	
    	if (category == null) {
    		arachneId = entityIdentificationService.getId(id);
    	} else {
    		arachneId = entityIdentificationService.getId(category, id);
    	}
    	
    	if (arachneId == null) {
    		response.setStatus(404);
    		return null;
    	}
    	
    	LOGGER.debug("Request for entity: " + arachneId.getArachneEntityID() + " - type: " + arachneId.getTableName());
    	
    	final String datasetGroupName = singleEntityDataService.getDatasetGroup(arachneId);
    	final DatasetGroup datasetGroup = new DatasetGroup(datasetGroupName);
    	
    	if (!userRightsService.userHasDatasetGroup(datasetGroup) && !(userRightsService.getCurrentUser().getGroupID()>=500)) {
    		response.setStatus(403);
    		return null;
    	}
    	
    	final Dataset arachneDataset = singleEntityDataService.getSingleEntityByArachneId(arachneId);
    	
    	final long fetchTime = System.currentTimeMillis() - startTime;
    	long nextTime = System.currentTimeMillis();
    	
    	imageService.addImages(arachneDataset);
    	
    	final long imageTime = System.currentTimeMillis() - nextTime;
    	nextTime = System.currentTimeMillis();
    	
    	// TODO find a way to handle contexts or discuss if we want to add them at all
    	//contextService.addMandatoryContexts(arachneDataset);
    	    	
    	final long contextTime = System.currentTimeMillis() - nextTime;
    	nextTime = System.currentTimeMillis();
    	
    	LOGGER.debug("-- Fetching entity took " + fetchTime + " ms");
    	LOGGER.debug("-- Adding images took " + imageTime + " ms");
    	LOGGER.debug("-- Adding contexts took " + contextTime + " ms");
    	LOGGER.debug("-----------------------------------");
    	LOGGER.debug("-- Complete response took " + (System.currentTimeMillis() - startTime) + " ms");
    	LOGGER.debug("Dataset: " + arachneDataset);    	
    	
    	return arachneDataset;
    }
}
