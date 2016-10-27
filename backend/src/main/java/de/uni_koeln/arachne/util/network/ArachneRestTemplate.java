package de.uni_koeln.arachne.util.network;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

/**
 * Convenience class to provide a ready-to-use rest template.
 * 
 * @author Reimar Grabowski
 *
 */
@Repository
public class ArachneRestTemplate extends RestTemplate {

	/**
	 * Constructor which sets the connection and read timeouts from 'applicatrion.properties' and adds a byte array 
	 * message converter.
	 * @param restConnectionTimeout The connection timeout value.
	 * @param restReadTimeout The read timeout value.
	 */
	@Autowired
	public ArachneRestTemplate(final @Value("${restConnectionTimeout}") int restConnectionTimeout,
			final @Value("${restReadTimeout}") int restReadTimeout) {
		
		if (getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
            ((SimpleClientHttpRequestFactory)getRequestFactory()).setConnectTimeout(restConnectionTimeout);
            ((SimpleClientHttpRequestFactory)getRequestFactory()).setReadTimeout(restReadTimeout);
        } else if (getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
            ((HttpComponentsClientHttpRequestFactory)getRequestFactory()).setConnectTimeout(restConnectionTimeout);
        	((HttpComponentsClientHttpRequestFactory)getRequestFactory()).setReadTimeout(restReadTimeout);
        }
		
		this.getMessageConverters().add(new ByteArrayHttpMessageConverter());
	}
}
