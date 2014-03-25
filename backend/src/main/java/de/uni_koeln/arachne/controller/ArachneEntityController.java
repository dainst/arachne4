package de.uni_koeln.arachne.controller;


import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.response.BaseArachneEntity;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.FormattedArachneEntity;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.ImageService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.util.ESClientUtil;
import de.uni_koeln.arachne.util.EntityId;

/**
 * Handles http requests (currently only get) for <code>/entity<code> and <code>/data</code>.
 */
@Controller
public class ArachneEntityController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ArachneEntityController.class);
	
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
	
	private transient String[] internalFields;
	
	@Autowired
	public ArachneEntityController(final @Value("#{config.internalFields}") String internalFieldsCS) {
		internalFields = internalFieldsCS.split(",");
	}
	
	/**
	 * Handles http request for /{entityId}.
	 * Requests for /entity/* return formatted data. This will be sent out either as JSON or as XML. The response format is set 
	 * by Springs content negotiation mechanism.
	 * @param entityId The unique entity id of the item to fetch.
     * @return A response object containing the data (this is serialized to JSON or XML depending on content negotiation).
     */
	@RequestMapping(value="/entity/{entityId}", method=RequestMethod.GET)
	public @ResponseBody Object handleGetEntityIdRequest(
			@PathVariable("entityId") final Long entityId,
			@RequestParam(value = "live", required = false) final Boolean isLive,
			final HttpServletRequest request,
			final HttpServletResponse response) {
		
		if (isLive != null && isLive) {
			return getEntityFromDB(entityId, null, response);
		} else {
			return getEntityFromIndex(entityId, null, request, response);
		}
	}
    
    /**
     * Handles http request for /{category}/{id}
     * Requests for /entity/* return formatted data. This will be sent out either as JSON or as XML. The response format is set 
	 * by Springs content negotiation mechanism.
     * @param category The database table to fetch the item from.
     * @param categoryId The internal id of the item to fetch
     * @return A response object containing the data (this is serialized to JSON or XML depending on content negotiation).
     */
    @RequestMapping(value="/entity/{category}/{categoryId}", method=RequestMethod.GET)
    public @ResponseBody Object handleGetCategoryIdRequest(
    		@PathVariable("category") final String category,
    		@PathVariable("categoryId") final Long categoryId,
    		@RequestParam(value = "live", required = false) final Boolean isLive,
    		final HttpServletRequest request,
    		final HttpServletResponse response) {
    	
    	LOGGER.debug("Request for category: " + category + " - id: " + categoryId);
    	if (isLive != null && isLive) {
			return getEntityFromDB(categoryId, category, response);
		} else {
			return getEntityFromIndex(categoryId, category, request, response);
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
     * @return A response object derived from <code>BaseArachneEntity</code>.
     */
    private BaseArachneEntity getEntityFromDB(final Long id, final String category //NOPMD
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
    		return responseFactory.createResponseForDeletedEntity(entityId);
    	}
    	
    	final FormattedArachneEntity result = entityService.getFormattedEntityById(entityId);
    	
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
     * If the entity is not found or the user does not have the necessary permission a HTTP 404 error message is returned.
     * @param id The unique entity ID if no category is given else the internal ID.
     * @param category The category to query or <code>null</code>.
     * @param response The <code>HttpServeletRsponse</code> object.
     * @return A response object derived from <code>BaseArachneEntity</code>.
     */
     private String getEntityFromIndex(final Long id, final String category //NOPMD
    		,final HttpServletRequest request, final HttpServletResponse response) { 
    	
    	final Long startTime = System.currentTimeMillis();
    	    	
    	String result = null;
    	SearchResponse searchResponse = null;
    	final FilterBuilder accessFilter = FilterBuilders.boolFilter().must(esClientUtil.getAccessControlFilter());
    	if (category == null) {
    		final QueryBuilder query = QueryBuilders.filteredQuery(QueryBuilders.queryString("entityId:" + id), accessFilter);
    		searchResponse = esClientUtil.getClient().prepareSearch(esClientUtil.getSearchIndexAlias())
    				.setQuery(query)
    				.setFetchSource(new String[] {"*"}, internalFields)
    				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    				.setFrom(0)
    				.setSize(1)
    				.execute().actionGet();
    	} else {
    		final QueryBuilder query = QueryBuilders.filteredQuery(QueryBuilders.queryString("type:" + category + " AND " + "internalId:" + id), accessFilter);
    		searchResponse = esClientUtil.getClient().prepareSearch(esClientUtil.getSearchIndexAlias())
    				.setQuery(query)
    				.setFetchSource(new String[] {"*"}, internalFields)
    				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    				.setFrom(0)
    				.setSize(1)
    				.execute().actionGet();
    	}
    	if (searchResponse.getHits().getTotalHits() == 1) { 
    		result = searchResponse.getHits().getAt(0).getSourceAsString();
    		if (request.getHeader("Accept").toLowerCase().contains("application/json")) {
    			response.setContentType("application/json");
    		} else {
    			try {
    				final JSONObject jsonObject = new JSONObject(result);
    				result = XML.toString(jsonObject, "entity");
    				response.setContentType("application/xml");
    				final PrintWriter writer = response.getWriter();
    				writer.write(result);
    				writer.flush();
    				writer.close();
    			} catch (Exception e) {
    				LOGGER.error("JSON to XML conversion for entity '" + category + ": " + id +"' failed. Cause: ", e);
    			}
    		} 
    	} else {
    		response.setStatus(404);
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
