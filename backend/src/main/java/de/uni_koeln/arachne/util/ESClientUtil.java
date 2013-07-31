package de.uni_koeln.arachne.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.ServletContextAware;

/**
 * Utility class to provide a reusable elastic search client and access to the configuration values.
 */
@Repository("ESClientUtil")
public class ESClientUtil implements ServletContextAware {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ESClientUtil.class);
	
	private transient ServletContext servletContext;
	
	private transient final String esAddress;
	private transient final int esRemotePort;
	private transient final String esName;
	private transient final int esBulkSize;
	private transient final boolean esRemoteClient;
	private transient final String esRESTPort;
	private transient final String esFullAddress ;
	
	private transient final Node node;
	private transient final Client client;
		
	private static final String ES_MAPPING_SUCCESS = "Elasticsearch mapping set.";
	private static final String ES_MAPPING_FAILURE = "Failed to set elasticsearch mapping.";
	
	private static final String INDEX_1 = "arachne4_1";
	private static final String INDEX_2 = "arachne4_2";
	
	@Autowired
	public ESClientUtil(final @Value("#{config.esProtocol}") String esProtocol
			, final @Value("#{config.esAddress}") String esAddress
			, final @Value("#{config.esRemotePort}") int esRemotePort
			, final @Value("#{config.esName}") String esName
			, final @Value("#{config.esBulkSize}") int esBulkSize
			, final @Value("#{config.esClientTypeRemote}") boolean esRemoteClient
			, final @Value("#{config.esRESTPort}") String esRESTPort) {
		
		this.esAddress = esAddress;
		this.esRemotePort = esRemotePort;
		this.esName = esName;
		this.esBulkSize = esBulkSize;
		this.esRemoteClient = esRemoteClient;
		this.esRESTPort = esRESTPort;
		esFullAddress  = esProtocol + "://" + esAddress + ':' + esRESTPort + '/';
		
		if (esRemoteClient) {
			LOGGER.info("Setting up elasticsearch transport client...");
			node = null;
			final Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", esName).build();
			client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(esAddress, esRemotePort));
		} else {
			LOGGER.info("Setting up elasticsearch node client...");
			node = NodeBuilder.nodeBuilder(). client(true).clusterName(esName).node();
			client = node.client();
		}
	}
	
	/**
	 * This method creates the new elasticsearch index that will be used for the dataimport and sets its mapping. It fails if the
	 *  index already exists or the mapping cannot be set.
	 * @return The index name of the new index or "NoIndex" in case of failure.
	 */
	public String getDataImportIndex() {
		String result = "NoIndex";
		final String indexName = getDataImportIndexName();
		final String url = esFullAddress + indexName;
		// TODO: implement better failure handling
		if (sendRequest(url, "PUT").contains("ok") && ES_MAPPING_SUCCESS.equals(setMapping(indexName))) {
			result = indexName; 
		}
		return result;
	}

	/**
	 * Updates the elasticsearch indicies by changing the index alias and deleting the unused index.
	 */
	public void updateSearchIndex() {
		final String indexName = getDataImportIndexName();
		final String oldName = "arachne4_2".equals(indexName) ? "arachne4_1" : "arachne4_2";
		final IndicesAliasesResponse response = client.admin().indices().prepareAliases().addAlias(indexName, esName)
				.removeAlias(oldName, esName).execute().actionGet();
		if (response.isAcknowledged()) {
			LOGGER.info("Set alias for " + indexName);
			LOGGER.info("Removed alias for " + oldName);
			deleteIndex(oldName);
		} else {
			LOGGER.error("Setting aliases failed.");
		}
	}
	
	/**
	 * Deletes the elasticsearch index with the given name.
	 * @param indexName
	 */
	public void deleteIndex(final String indexName) {
		LOGGER.info("Deleting index " + indexName);
		final DeleteIndexResponse delete = client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
		if (!delete.isAcknowledged()) {
			LOGGER.error("Index wasn't deleted");
		}
	}

	public Client getClient() {
		return this.client;
	}
	
	public boolean isRemote() {
		return esRemoteClient;
	}
	
	public String getName() {
		return esName;
	}
	
	public int getBulkSize() {
		return esBulkSize;
	}
	
	@Override
	public void setServletContext(final ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	/**
	 * Closes the elastic search client or node.
	 */
	@PreDestroy
	public void destroy() {
		if (esRemoteClient) {
			LOGGER.info("Closing up elasticsearch transport client...");
			client.close();
		} else {
			LOGGER.info("Closing up elasticsearch node client...");
			node.close();
		}
	}

	/**
	 * Sends a HTTP requests to the elasticsearch alias endpoint to determine the index name to use for the dataimport.
	 * @return The index name of the index currently not in use. Either <code>arachne4_1</code> or <code>arachne4_2</code>.
	 */
	private String getDataImportIndexName() {
		String result = INDEX_1;
		final String url = esFullAddress + "*/_alias/*";
		if (sendRequest(url, "GET").equals("{\"" + INDEX_1 + "\":{\"aliases\":{\"arachne4\":{}}}}")) {
			result = INDEX_2;
		}
		return result;
	}

	/**
	 * Sets the elasticsearch mapping on the specified index by reading the 'mapping.json' file and sending it as REST request to 
	 * elasticsearch.
	 * @return A status message.
	 */
	private String setMapping(final String indexName) {
		HttpURLConnection connection = null;
		String message = ES_MAPPING_FAILURE;

		final String mapping = getMappingFromFile();

		if ("undefined".equals(mapping)) {
			return message;
		}

		try {
			LOGGER.debug("Elasticsearch set mapping: " + esFullAddress + indexName + "/entity/_mapping");
			final URL serverAdress = new URL(esFullAddress + indexName + "/entity/_mapping");
			connection = (HttpURLConnection)serverAdress.openConnection();			
			connection.setRequestMethod("PUT");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(mapping);
			writer.flush();
			writer.close();

			if (connection.getResponseCode() == 200) {
				message = ES_MAPPING_SUCCESS;
				LOGGER.info(ES_MAPPING_SUCCESS);
			} else {
				LOGGER.error(ES_MAPPING_FAILURE + ". Elasticsearch HTTP request returned status code: " + connection.getResponseCode());
			}
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage());
		} catch (ProtocolException e) {
			LOGGER.error(e.getMessage());
		} catch (SocketTimeoutException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			connection.disconnect();
			connection = null;
		}
		return message;
	}
	
	/**
	 * Reads the elastic search mapping from "/WEB-INF/search/mapping.json".   
	 * @return The JSON mapping as <code>String</code>.
	 */
	private String getMappingFromFile() {
		final String filename = "/WEB-INF/search/mapping.json";
		StringBuilder mapping = new StringBuilder(64);
		InputStream inputStream = null;
		try {
			inputStream = servletContext.getResourceAsStream(filename);
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			while ((inputLine = bufferedReader.readLine()) != null) {
				mapping.append(inputLine);
				LOGGER.debug(inputLine);
			}
		} catch (IOException e) {
			LOGGER.error("Could not read '" + filename + "'. " + e.getMessage());
			mapping = new StringBuilder("undefined");
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					LOGGER.error("Could not close '" + filename + "'. " + e.getMessage());
					mapping = new StringBuilder("undefined");
				}
			}
		}
		return mapping.toString();
	}
	
	// TODO: move to own class (?)
	private String sendRequest(final String url, final String method) {
		
		final StringBuilder result = new StringBuilder(); 
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
				LOGGER.error(method + " request failed with " + connection.getResponseCode() + ' ' + connection.getResponseMessage());
			}
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage());
		} catch (ProtocolException e) {
			LOGGER.error(e.getMessage());
		} catch (SocketTimeoutException e) {
			LOGGER.error("Elasticsearch REST connection timed out: " + e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		} finally {
			connection.disconnect();
			connection = null;
		}
		return result.toString();
	}		
}
