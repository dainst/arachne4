package de.uni_koeln.arachne.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.annotation.PreDestroy;

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

/**
 * Utility class to provide a reusable elastic search client and access to the configuration values.
 */
@Repository("ESClientUtil")
public class ESClientUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ESClientUtil.class);
	
	private transient final String esAddress;
	private transient final int esRemotePort;
	private transient final String esName;
	private transient final int esBulkSize;
	private transient final boolean esRemoteClient;
	private transient final String esRESTPort;
	private transient final String esFullAddress ;
	
	private transient final Node node;
	private transient final Client client;
		
	@Autowired
	public ESClientUtil(final @Value("#{config.esAddress}") String esAddress, final @Value("#{config.esRemotePort}") int esRemotePort
			, final @Value("#{config.esName}") String esName, final @Value("#{config.esBulkSize}") int esBulkSize
			, final @Value("#{config.esClientTypeRemote}") boolean esRemoteClient
			, final @Value("#{config.esRESTPort}") String esRESTPort) {
		
		this.esAddress = esAddress;
		this.esRemotePort = esRemotePort;
		this.esName = esName;
		this.esBulkSize = esBulkSize;
		this.esRemoteClient = esRemoteClient;
		this.esRESTPort = esRESTPort;
		esFullAddress  = "http://" + esAddress + ':' + esRESTPort + '/';
		
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
	 * This method creates the new elasticsearch index that will be used for the dataimport. It fails if the index already exists.
	 * @return The index name.
	 */
	public String getDataImportIndex() {
		String result = "NoIndex";
		final String indexName = getDataImportIndexName();
		final String url = esFullAddress + indexName;
		// TODO: implement better failure handling
		if (sendRequest(url, "PUT").contains("ok")) {
			result = indexName; 
		}
		return esName;//result;
	}

	/**
	 * Sends a HTTP requests to the elasticsearch alias endpoint to determine the index name to use for the dataimport.
	 * @return The index name of the index currently not in use. Either <code>arachne4_1</code> or <code>arachne4_2</code>.
	 */
	private String getDataImportIndexName() {
		String result = "arachne4_1";
		final String url = esFullAddress + "*/_alias/*";
		if (sendRequest(url, "GET").equals("{\"arachne4_1\":{\"aliases\":{\"arachne4\":{}}}}")) {
			result = "arachne4_2";
		}
		return result;
	}

	/**
	 * Updates the elasticsearch indicies by changing the index alias and deleting the unused index.
	 */
	public void updateSearchIndex(final String indexName) {
		// TODO Auto-generated method stub
		// create new alias
		// remove old alias
		// delete unused index
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

	// TODO: move to own class
	private String sendRequest(final String url, final String method) {
		
		final StringBuilder result = new StringBuilder(); 
		HttpURLConnection connection = null;
		try {
			LOGGER.debug("REST GET: " + url);
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
					result.append("/n");
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
