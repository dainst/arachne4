package de.uni_koeln.arachne.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.arachne.util.ArachneRestTemplate;

/**
 * Gets the translations lazily from transl8 and offers translation functionality.
 */
@Service
public class Transl8Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(Transl8Service.class);

	@Autowired
	private transient ArachneRestTemplate restTemplate;

	private transient Map<String, Boolean> translationsAvailable = new HashMap<String, Boolean>();
	
	private transient Map<String, String> translationMap = new HashMap<String, String>();

	private transient Map<String, String> categoryMap = new HashMap<String, String>();

	private transient List<String> languages = new ArrayList<String>(1);
	
	private transient String transl8Url;
	
	@Autowired
	public Transl8Service(final @Value("#{config.transl8Url}") String transl8Url) {
		this.transl8Url = transl8Url;
		languages.add("de");
		translationsAvailable.put(languages.get(0), false);
	}

	/**
	 * Contacts transl8 via rest call and updates the internal translation map.
	 */
	@SuppressWarnings("unchecked")
	public void updateTranslations() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Accept-Language", languages.get(0));

		HttpEntity<String> entity = new HttpEntity<String>("", headers);

		try {
			final ResponseEntity<String> response = restTemplate.exchange(transl8Url , HttpMethod.GET, entity, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				final String doc = response.getBody();
				try {
					translationMap = new ObjectMapper().readValue(doc, HashMap.class);
				} catch (JsonParseException e) {
					LOGGER.error("Could not parse transl8 response.", e);
				} catch (JsonMappingException e) {
					LOGGER.error("Could not map transl8 response.", e);
				} catch (IOException e) {
					LOGGER.error("Could not create translation map.", e);
				}

				if (translationMap != null && !translationMap.isEmpty()) {
					categoryMap = new HashMap<String, String>();
					for (final Map.Entry<String, String> entry: translationMap.entrySet()) {
						String key = entry.getKey();
						if (key.startsWith("facet_kategorie_")) {
							categoryMap.put(entry.getValue(), key.substring(16));
						}
					}
					translationsAvailable.put(languages.get(0), true);
					LOGGER.info("Translations are available for: " + languages);
				} else {
					LOGGER.error("Translation map is empty. Translations are not available.");
				}
			} else {
				LOGGER.warn("There was a problem contacting transl8. Translations are not available. Http status code: " + response.getStatusCode());
			}	
		} catch (HttpServerErrorException e) {
			LOGGER.warn("There was a problem contacting transl8. Translations are not available. Cause: ", e);
		} catch (RestClientException e) {
			LOGGER.warn("There was a problem contacting transl8. Translations are not available. Cause: ", e);
		}
	}
	
	/**
	 * Looks up a key in the translations map and returns the corresponding value if found or the key else.
	 * @param key Key to look up translation for.
	 * @return Either a translation or the key.
	 */
	public String transl8(String key) {
		if (!translationsAvailable.get(languages.get(0))) {
			updateTranslations();
		}
		if (!translationMap.isEmpty()) {
			String value = translationMap.get(key);
			if (value != null) {
				return value;
			}
		}
		return key;
	}
	
	/**
	 * Looks up a facet key in the translation map and returns the corresponding value if found or the key else.
	 * For facet translations the key prefix is generated from the facet name.
	 * @param name of the facet.
	 * @param key Key to look up translation for.
	 * @return Either a translation or the key.
	 */
	public String transl8Facet(String facetName, String key) {
		if (!translationsAvailable.get(languages.get(0))) {
			updateTranslations();
		}
		if (!translationMap.isEmpty()) {
			String value = translationMap.get("facet_" + facetName + '_' + key);
			if (value != null) {
				return value;
			}
		}
		return key;
	}
	
	/**
	 * Looks up a a category key in the reverse LUT.
	 * @param key The translated category value.
	 * @return The category key if found else the unchanged key parameter.
	 */
	public String categoryLookUp(String key) {
		if (!translationsAvailable.get(languages.get(0))) {
			updateTranslations();
		}
		if (!categoryMap.isEmpty()) {
			String value = categoryMap.get(key);
			if (value != null) {
				return value;
			}
		}
		return key;
	}
}