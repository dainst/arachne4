package de.uni_koeln.arachne.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class ArachneRestTemplate extends RestTemplate {

	@Autowired
	public ArachneRestTemplate(final @Value("#{config.restConnectionTimeout}") int restConnectionTimeout,
			final @Value("#{config.restReadTimeout}") int restReadTimeout) {
		if (getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
            ((SimpleClientHttpRequestFactory)getRequestFactory()).setConnectTimeout(restConnectionTimeout);
            ((SimpleClientHttpRequestFactory)getRequestFactory()).setReadTimeout(restReadTimeout);
        } else if (getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
            ((HttpComponentsClientHttpRequestFactory)getRequestFactory()).setConnectTimeout(restConnectionTimeout);
        	((HttpComponentsClientHttpRequestFactory)getRequestFactory()).setReadTimeout(restReadTimeout);
        }
	}
}
