package de.uni_koeln.arachne.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Gets the translations from transl8 and offers translation functionality;
 */
@Service
public class Transl8Service {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Transl8Service.class);
	
	private transient final RestTemplate restTemplate = new RestTemplate();
	
	private transient Map<String, String> translationMap;

	public Transl8Service() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Accept-Language", "de");
		
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		
		// TODO read transl8 URL from application.properties
		String url = "http://crazyhorse.archaeologie.uni-koeln.de/transl8/translation/json?application=arachne4_backend";
		final ResponseEntity<String> response = restTemplate.exchange(url , HttpMethod.GET, entity, String.class);
				
		String doc = response.getBody();
				
		try {
			translationMap = new ObjectMapper().readValue(doc, HashMap.class);
		} catch (JsonParseException e) {
			LOGGER.error("Could not parse transl8 response.", e);
		} catch (JsonMappingException e) {
			LOGGER.error("Could not map transl8 response.", e);
		} catch (IOException e) {
			LOGGER.error("Could not create translation map.", e);
		}
	}
	
	public String transl8(String key) {
		if (!translationMap.isEmpty()) {
			String value = translationMap.get(key);
			if (value != null) {
				return value;
			}
		}
		return key;
	}
}
