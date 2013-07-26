package de.uni_koeln.arachne.util;

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
	
	private transient final String esName;
	private transient final int esBulkSize;
	private transient final boolean esRemoteClient;
	
	private transient final Node node;
	private transient final Client client;
	
	@Autowired
	public ESClientUtil(final @Value("#{config.esAddress}") String esAddress, final @Value("#{config.esRemotePort}") int esRemotePortPort
			, final @Value("#{config.esName}") String esName, final @Value("#{config.esBulkSize}") int esBulkSize
			, final @Value("#{config.esClientTypeRemote}") boolean esRemoteClient) {
		
		this.esName = esName;
		this.esBulkSize = esBulkSize;
		this.esRemoteClient = esRemoteClient;
		
		if (esRemoteClient) {
			LOGGER.info("Setting up elastic search transport client...");
			node = null;
			final Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", esName).build();
			client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(esAddress, esRemotePortPort));
		} else {
			LOGGER.info("Setting up elastic search node client...");
			node = NodeBuilder.nodeBuilder(). client(true).clusterName(esName).node();
			client = node.client();
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
	
	/**
	 * Closes the elastic search client or node.
	 */
	@PreDestroy
	public void destroy() {
		if (esRemoteClient) {
			client.close();
		} else {
			node.close();
		}
	}
}
