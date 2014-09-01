package de.uni_koeln.arachne.service;

import java.io.IOException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.arachne.util.ESClientUtil;
import de.uni_koeln.arachne.util.JSONUtil;

@Service
@Scope("prototype")
public class EntityCompareService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntityCompareService.class);
	
	@Autowired
	private transient ESClientUtil esClientUtil;
	
	@Autowired
	private transient JSONUtil jsonUtil;

	@Async
	public void compareToIndex(final Long entityId, final String json) {
		    	
    	SearchResponse searchResponse = null;
    	    	
    	final QueryBuilder acLessQuery = QueryBuilders.queryString("entityId:" + entityId);
    	searchResponse = esClientUtil.getClient().prepareSearch(esClientUtil.getSearchIndexAlias())
    			.setQuery(acLessQuery)
    			.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    			.setFrom(0)
    			.setSize(1)
    			.execute().actionGet();
    	
    	String jsonFromIndex = null;
    	if (searchResponse.getHits().getTotalHits() == 1) { 
    		 jsonFromIndex = searchResponse.getHits().getAt(0).getSourceAsString();
    	}
    	
    	/*if (jsonFromIndex == null) {
    		LOGGER.warn("Entity " + entityId + " not found in index.");
    	} else*/ {
    		ObjectMapper mapper = jsonUtil.getObjectMapper();
			JsonNode jsonDB = null;
			JsonNode jsonES = null;
			try {
				jsonDB = mapper.readTree(json);
				jsonES = mapper.readTree(jsonFromIndex);
			} catch (JsonProcessingException e) {
				//e.printStackTrace();
				LOGGER.warn("FailedId: " + entityId + ". Cause: " + e.getMessage());
			} catch (IOException e) {
				LOGGER.warn("FailedId: " + entityId + ". Cause: " + e.getMessage());
			}
			
			if (jsonDB != null && jsonDB.equals(jsonES)) {
				//LOGGER.info("Entity " + entityId + " is the same in DB and index.");
			} else {
				LOGGER.info("Entity " + entityId + " differs in DB and index.");
			}
    	}
	}
}
