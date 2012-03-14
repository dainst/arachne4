package de.uni_koeln.arachne.util;

import org.springframework.stereotype.Repository;

// TODO find a better way to set the Solr url from the application.properties file (perhaps using @Value)

/**
 * Class that contains only a single <code>String</code> property.
 * It is used to inject the solr url from the <code>application.properties</code> file.
 */
@Repository
public class SolrUrlString {
	
	private String solrUrl = "";

	public String getSolrUrl() {
		return solrUrl;
	}

	public void setSolrUrl(String url) {
		solrUrl = url;
	}
}
