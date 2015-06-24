package de.uni_koeln.arachne.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

/**
 * Class to provide methods for interaction with the elasticsearch index.
 * 
 * @author Reimar Grabowski
 */
@Service
public class ESService implements ServletContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(ESService.class);

	private static final String MAPPING_FILE = "/WEB-INF/search/mapping.json";

	private static final String SETTINGS_FILE = "/WEB-INF/search/settings.json";

	private static final String ES_MAPPING_SUCCESS = "Elasticsearch mapping set.";
	private static final String ES_MAPPING_FAILURE = "Failed to set elasticsearch mapping.";
	// Not 'final' so that it can be changed via reflection when testing
	private static String INDEX_1 = "arachne4_1";
	private static String INDEX_2 = "arachne4_2";
	private transient ServletContext servletContext;

	@Autowired
	private transient IUserRightsService userRightsService;
	
	@Autowired
	private transient Transl8Service ts;
	
	private transient final String esName;

	private transient final int esBulkActions;
	private transient final int esBulkSize;

	private transient final boolean esRemoteClient;
	private transient final String esFullAddress ;

	private transient final Node node;
	private transient final Client client;

	private transient final String searchIndexAlias;

	@Autowired
	public ESService(final @Value("${esProtocol}") String esProtocol
			, final @Value("${esAddress}") String esAddress
			, final @Value("${esRemotePort}") int esRemotePort
			, final @Value("${esName}") String esName
			, final @Value("${esBulkActions}") int esBulkActions
			, final @Value("${esBulkSize}") int esBulkSize
			, final @Value("${esClientTypeRemote}") boolean esRemoteClient
			, final @Value("${esRESTPort}") String esRESTPort) {

		this.esName = esName;
		this.searchIndexAlias = esName;
		this.esBulkActions = esBulkActions;
		this.esBulkSize = esBulkSize;
		this.esRemoteClient = esRemoteClient;
		esFullAddress  = esProtocol + "://" + esAddress + ':' + esRESTPort + '/';

		if (esRemoteClient) {
			LOGGER.info("Setting up elasticsearch transport client...");
			node = null;
			final Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", esName).build();
			client = new TransportClient(settings);
			((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(esAddress, esRemotePort));
		} else {
			LOGGER.info("Setting up elasticsearch node client...");
			final Settings settings = ImmutableSettings.settingsBuilder().put("discovery.zen.ping.multicast.enabled", false).build();
			node = NodeBuilder.nodeBuilder().client(true).clusterName(esName).settings(settings).node();
			client = node.client();
		}
	}

	/**
	 * Deletes the elasticsearch index with the given name.
	 * @param indexName The name of the index to delete.
	 * @return A boolean value indicating success.
	 */
	public boolean deleteIndex(final String indexName) {
		boolean result = true;
		LOGGER.info("Deleting index " + indexName);
		DeleteIndexResponse delete = null;
		try {
			delete = client.admin().indices().prepareDelete(indexName).execute().actionGet();
			if (!delete.isAcknowledged()) {
				LOGGER.error("Index " + indexName + " was not deleted.");
				result = false;
			}
		} catch (IndexMissingException e) { // NOPMD
			// No problem if no index exists as it should be deleted anyways
		}
		return result;
	}

	/**
	 * Closes the elastic search client or node.
	 */
	@PreDestroy
	public void destroy() {
		if (esRemoteClient) {
			LOGGER.info("Closing elasticsearch transport client...");
			client.close();
		} else {
			LOGGER.info("Closing elasticsearch node client...");
			node.close();
		}
	}

	/**
	 * This method constructs a access control query filter for Elasticsearch using the <code>UserRightsService</code>.
	 * @return The constructed filter.
	 */
	public BoolFilterBuilder getAccessControlFilter() {
		final User user = userRightsService.getCurrentUser();
		if (user.isAll_groups()) {
			return FilterBuilders.boolFilter().must(FilterBuilders.matchAllFilter());
		} else {
			final Set<DatasetGroup> datasetGroups = user.getDatasetGroups();
			final OrFilterBuilder orFilter = FilterBuilders.orFilter();
			for (final DatasetGroup datasetGroup: datasetGroups) {
				orFilter.add(FilterBuilders.termFilter("datasetGroup", datasetGroup.getName()));
			}
			return FilterBuilders.boolFilter().must(orFilter);
		}
	}

	/**
	 * Returns the maximum number of actions that are bulked in one request.
	 * @return The maximum number of actions for a bulk.
	 */
	public int getBulkActions() {
		return esBulkActions;
	}

	/**
	 * Returns the maximum size for a bulk request.
	 * @return The maximum size for a bulk request in MB.
	 */
	public int getBulkSize() {
		return esBulkSize;
	}

	/**
	 * Gets the current elasticsearch client for re-use.
	 * @return The elasticsearch client.
	 */
	public Client getClient() {
		return this.client;
	}

	/**
	 * This method creates the new elasticsearch index that will be used for the dataimport and sets its mapping. It deletes any
	 * existing index of the same name and it fails if the the mapping cannot be set.
	 * @return The index name of the new index or "NoIndex" in case of failure.
	 */
	public String getDataImportIndex() {
		String result = "NoIndex";
		final String indexName = getDataImportIndexName();

		deleteIndex(indexName);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		final String settings = getJsonFromFile(SETTINGS_FILE);

		CreateIndexRequestBuilder prepareCreate = client.admin().indices().prepareCreate(indexName);

		if (!"undefined".equals(settings)) {
			prepareCreate = prepareCreate.setSettings(ImmutableSettings.settingsBuilder().loadFromSource(settings));
		}

		final CreateIndexResponse createResponse = prepareCreate.execute().actionGet();

		if (!createResponse.isAcknowledged()) {
			LOGGER.error("Failed to create index '" + indexName + "'");
			return result;
		}
		LOGGER.info("Created index " + indexName);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		if (ES_MAPPING_SUCCESS.equals(setMapping(indexName))) {
			result = indexName;
		}

		return result;
	}

	/**
	 * Gets the elasticsearch cluster name.
	 * @return The clustername as <code>String</code>.
	 */
	public String getName() {
		return esName;
	}

	/**
	 * Gets the alias of the current search index.
	 * @return The alias as <code>String</code>.
	 */
	public String getSearchIndexAlias() {
		return this.searchIndexAlias;
	}

	/**
	 * Indicates whether a remote client is used. 
	 * @return <code>true</code> if the client is remote else <code>false</code>. 
	 */
	public boolean isRemote() {
		return esRemoteClient;
	}

	public void setRefreshInterval(final String indexName, final boolean enabled) {
		// close index		
		final CloseIndexResponse closeResponse = client.admin().indices()
				.prepareClose(indexName)
				.execute().actionGet();
		if (!closeResponse.isAcknowledged()) {
			LOGGER.error("Failed to close index '" + indexName + "'.");
		}

		// update settings
		String refreshValue = "1s";
		if (!enabled) {
			refreshValue = "-1";
		}
		final Settings settings = ImmutableSettings.settingsBuilder().put("refresh_intervall", refreshValue).build();

		final UpdateSettingsResponse response = client.admin().indices()
				.prepareUpdateSettings(indexName)
				.setSettings(settings)
				.execute().actionGet();
		if (!response.isAcknowledged()) {
			LOGGER.error("Failed to set 'refresh_interval' to " + refreshValue + " on index '" + indexName + "'.");
		}

		// open index
		final OpenIndexResponse openResponse = client.admin().indices()
				.prepareOpen(indexName)
				.execute().actionGet();
		if (!openResponse.isAcknowledged()) {
			LOGGER.error("Failed to open index '" + indexName + "'.");
		}
	}

	@Override
	public void setServletContext(final ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Updates the elasticsearch indices by changing the index alias and deleting the unused index. If this fails it tries to only
	 * set the new alias (this should only occur on the first dataimport as no alias to delete exists at that point). If this also fails 
	 * the method throws the corresponding exception.
	 */
	public String updateSearchIndex() throws IllegalStateException, IndexMissingException {
		final String indexName = getDataImportIndexName();
		final String oldName = INDEX_2.equals(indexName) ? INDEX_1 : INDEX_2;
		try {
			final IndicesAliasesResponse indexResponse = client.admin().indices().prepareAliases()
					.addAlias(indexName, searchIndexAlias)
					.removeAlias(oldName, searchIndexAlias)
					.execute().actionGet();
			LOGGER.debug("Trying to set alias for '" + indexName + "' and delete alias for '" + oldName + "'");
			if (indexResponse.isAcknowledged()) {
				LOGGER.info("Set alias for '" + indexName + "'");
				LOGGER.info("Removed alias for '" + oldName  + "'");
				deleteIndex(oldName);
			} else {
				LOGGER.error("Failed to set alias.");
				throw new IllegalStateException("Failed to set aliases.");
			}
		} catch (IndexMissingException e) {
			LOGGER.warn("Failed to set alias. Index Missing. Trying to just set the new one.");
			// perhaps we are running for the first time so try just to add the new alias
			try {
				final IndicesAliasesResponse indexResponse = client.admin().indices().prepareAliases().addAlias(indexName, searchIndexAlias)
						.execute().actionGet();
				if (indexResponse.isAcknowledged()) {
					LOGGER.info("Set alias for '" + indexName + "'");
					LOGGER.info("No alias removed.");
					deleteIndex(oldName);
				} else {
					LOGGER.error("Failed to set alias.");
					throw new IllegalStateException("Failed to set aliases."); // NOPMD
				}
			} catch (IndexMissingException ime) {
				LOGGER.error("Failed to set alias. Index Missing.");
				throw ime;
			}
		}
		return indexName;
	}

	/**
	 * Returns the number of documents in the search index.
	 * @return The number of documents or -1 on error.
	 */
	public long getCount() {
		final CountResponse countResponse = getClient().prepareCount(getSearchIndexAlias()).execute().actionGet();
		if (countResponse.status() == RestStatus.OK) {
			return countResponse.getCount();
		} else {
			LOGGER.error("Getting count from search index failed. Cause: " + countResponse.status().toString());
			return -1;
		}
	}

	/**
	 * Retrieves a document from the current index. Access control is handled transparently.
	 * @param id The entityId of the document or the internal ID of the entity if a category is given.
	 * @param internalFields Fields that must not be included in the response.
	 * @return The entity or <code>null</code> and an HTTP status code.
	 */
	public TypeWithHTTPStatus<String> getDocumentFromCurrentIndex(final long id, final String category
			, final String[] internalFields) {
		
		SearchResponse searchResponse = null;
		SearchResponse acLessSearchResponse = null;
		final FilterBuilder accessFilter = getAccessControlFilter();

		if (category == null) {
			final QueryBuilder query = QueryBuilders.filteredQuery(
					QueryBuilders.termQuery("entityId", id), accessFilter);
			LOGGER.debug("Entity query [" + id + "]: " + query);
			searchResponse = getClient().prepareSearch(getSearchIndexAlias())
					.setQuery(query)
					.setFetchSource(new String[] {"*"}, internalFields)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setFrom(0)
					.setSize(1)
					.execute().actionGet();

			final QueryBuilder acLessQuery = QueryBuilders.termQuery("entityId", id);
			LOGGER.debug("Entity query [" + id + "] (no access control): " + acLessQuery);
			acLessSearchResponse = getClient().prepareSearch(getSearchIndexAlias())
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
			searchResponse = getClient().prepareSearch(getSearchIndexAlias())
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
			acLessSearchResponse = getClient().prepareSearch(getSearchIndexAlias())
					.setQuery(acLessQuery)
					.setFetchSource(new String[] {"*"}, internalFields)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setFrom(0)
					.setSize(1)
					.execute().actionGet();
		}
		
		if (searchResponse.getHits().getTotalHits() == 1) {
    		return new TypeWithHTTPStatus<String>(searchResponse.getHits().getAt(0).getSourceAsString());
    	} else {
    		if (acLessSearchResponse.getHits().getTotalHits() == 1) {
    			return new TypeWithHTTPStatus<String>(HttpStatus.FORBIDDEN);
    		}
    	}
    	return new TypeWithHTTPStatus<String>(HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Sends a HTTP request to the elasticsearch alias endpoint to determine the index name to use for the dataimport.
	 * @return The index name of the index currently not in use. Either <code>arachne4_1</code> or <code>arachne4_2</code>.
	 */
	private String getDataImportIndexName() {
		String result = INDEX_1;
		final String url = esFullAddress + "*/_alias/*";
		if (sendRequest(url, "GET").contains(INDEX_1)) {
			result = INDEX_2;
		}
		return result;
	}

	/**
	 * Reads the elastic search json configs from the given file   
	 * @param filename The path to the json file.
	 * @return The JSON as <code>String</code>.
	 */
	private String getJsonFromFile(final String filename) {
		StringBuilder result = new StringBuilder(64);
		InputStream inputStream = null;
		try {
			inputStream = servletContext.getResourceAsStream(filename);
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			while ((inputLine = bufferedReader.readLine()) != null) {
				result.append(inputLine);
				LOGGER.debug(inputLine);
			}
		} catch (IOException e) {
			LOGGER.error("Could not read '" + filename + "'. " + e.getMessage());
			result = new StringBuilder("undefined");
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					LOGGER.error("Could not close '" + filename + "'. " + e.getMessage());
					result = new StringBuilder("undefined");
				}
			}
		}
		return result.toString();
	}

	// TODO: move to own class or replace with restTemplate (?)
	private String sendRequest(final String url, final String method) {

		final StringBuilder result = new StringBuilder(32); 
		HttpURLConnection connection = null;
		try {
			LOGGER.debug("HTTP " + method + ": " + url);
			final URL requestUrl = new URL(url);
			connection = (HttpURLConnection)requestUrl.openConnection();			
			connection.setRequestMethod(method);
			connection.setReadTimeout(5000);
			connection.connect();

			if (connection.getResponseCode() == 200) {
				final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				while ((inputLine = bufferedReader.readLine()) != null) {
					result.append(inputLine);
				}
			} else {
				LOGGER.error(method + " (" + url + ") request failed with " + connection.getResponseCode() + ' ' + connection.getResponseMessage());
			}
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (ProtocolException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (SocketTimeoutException e) {
			LOGGER.error("Elasticsearch REST connection timed out: ", e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (connection != null) {
				connection.disconnect();
				connection = null;
			}
		}
		return result.toString();
	}

	/**
	 * Sets the elasticsearch mapping on the specified index by reading the 'mapping.json' file and sending it as REST request to 
	 * elasticsearch.
	 * @return A status message.
	 */
	private String setMapping(final String indexName) {
		String message = ES_MAPPING_FAILURE;

		final String mapping = getJsonFromFile(MAPPING_FILE);

		if ("undefined".equals(mapping)) {
			return message;
		}

		final PutMappingResponse putResponse = client.admin().indices()
				.preparePutMapping(indexName)
				.setType("entity")
				.setSource(mapping)
				.execute().actionGet();

		if (putResponse.isAcknowledged()) {
			message = ES_MAPPING_SUCCESS;
			LOGGER.info(ES_MAPPING_SUCCESS);
		} else {
			LOGGER.error(ES_MAPPING_FAILURE);
		}

		return message;
	}
}
