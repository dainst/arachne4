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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.uni_koeln.arachne.util.JSONUtil;
import de.uni_koeln.arachne.util.network.ArachneRestTemplate;
import sun.rmi.runtime.Log;

import javax.xml.bind.annotation.XmlType;

/**
 * Gets the translations lazily from transl8 and offers translation functionality.</br>
 * For development without access to transl8 the exception throwing can be disabled by setting <code>throwException</code> 
 * to <code>false</code>. CAUTION: Do not forget to enable it again before pushing.
 */
@Service
public class Transl8Service {
	// set to false when developing without access to transl8
	private static final boolean throwException = true;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Transl8Service.class);

	private static final String DEFAULT_LANG = "en";

	@Autowired
	private transient ArachneRestTemplate restTemplate;
	
	private transient Map<String, Boolean> translationsAvailable = new HashMap<String, Boolean>();
	
	private transient Map<String, String> translationMap = new HashMap<String, String>();

	private transient Map<String, String> categoryMap = new HashMap<String, String>();

	private transient List<String> languages = new ArrayList<String>(2);
	
	private transient String transl8Url;
	
	/**
	 * Constructor setting the Transl8 URL.
	 * @param transl8Url The Transl8 URL.
	 */
	@Autowired
	public Transl8Service(final @Value("${transl8Url}") String transl8Url) {
		this.transl8Url = transl8Url;
		languages.add("de");
        languages.add("en");
        translationsAvailable.put(languages.get(0), false);
		translationsAvailable.put(languages.get(1), false);
	}

	/**
	 * Contacts transl8 via rest call and updates the internal translation map.
	 * @param lang the language in which the key should be retrieved
	 * @throws Transl8Exception if transl8 cannot be reached.
	 */
	@SuppressWarnings("unchecked")
	private void updateTranslations(String lang) throws Transl8Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		LOGGER.info("accepting {}", lang);
		headers.set("Accept-Language", lang);
		HttpEntity<String> entity = new HttpEntity<String>("", headers);
		try {
			final ResponseEntity<String> response = restTemplate.exchange(transl8Url , HttpMethod.GET, entity, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				final String doc = response.getBody();
				try {
					translationMap = JSONUtil.MAPPER.readValue(doc, HashMap.class);
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
					for (int i = 0; i < languages.size(); i++) //Set other langs to false again
                        translationsAvailable.put(languages.get(i), false);
                    translationsAvailable.put(lang, true);
					LOGGER.info("Translations are available for: " + languages);
				} else {
					LOGGER.error("Translation map is empty. Translations are not available.");
				}
			} else {
				LOGGER.warn("There was a problem contacting transl8. Translations are not available. Http status code: " + response.getStatusCode());
			}	
		} catch (RestClientException e) {
			if (throwException) {
				throw new Transl8Exception("There was a problem contacting transl8. Translations are not available.", e);
			}
		}
	}
	
	/**
	 * Looks up a key in the translations map and returns the corresponding value if found or the key else.
	 * @param key Key to look up translation for.
	 * @return Either a translation or the key.
	 * @throws Transl8Exception if transl8 cannot be reached. 
	 */
	public String transl8(String key, String lang) throws Transl8Exception {
	    LOGGER.info("Attempting {} to {}", key, lang);
        if(key != null && lang != null) {
            lang = extractLanguage(lang);
            LOGGER.info("Extracted {}", lang);
			if (!translationsAvailable.get(lang)) {
                updateTranslations(lang);
                LOGGER.info("Updated {}", lang);
			}
			else
			    LOGGER.info("not updated for {}", lang);
			if (!translationMap.isEmpty()) {
                LOGGER.info("Map not empty");
				String value = translationMap.get(key);
                LOGGER.info("value {}", value);
				if (value != null)
					return value;
			}
		}
		return key;
	}
	
	/**
	 * Looks up a facet key in the translation map and returns the corresponding value if found or the key else.
	 * For facet translations the key prefix is generated from the facet name.
	 * @param facetName of the facet.
	 * @param key Key to look up translation for.
	 * @return Either a translation or the key.
	 * @throws Transl8Exception if transl8 cannot be reached. 
	 */
	public String transl8Facet(String facetName, String key, String lang) throws Transl8Exception {
	    if(facetName != null && key != null && lang != null) {
            lang = extractLanguage(lang);
            if (!translationsAvailable.get(lang)) {
                updateTranslations(lang);
            }
            if (!translationMap.isEmpty()) {
                String value = translationMap.get("facet_" + facetName + '_' + key);
                if (value != null) {
                    return value;
                }
            }
        }
		return key;
	}
	
	/**
	 * Looks up a a category key in the reverse LUT.
	 * @param key The translated category value.
	 * @return The category key if found else the unchanged key parameter.
	 * @throws Transl8Exception if transl8 cannot be reached. 
	 */
	public String categoryLookUp(String key, String lang) throws Transl8Exception {
		if(key != null && lang != null) {
            lang = extractLanguage(lang);
			if (!translationsAvailable.get(lang)) {
				updateTranslations(lang);
			}
			if (!categoryMap.isEmpty()) {
				String value = categoryMap.get(key);
				if (value != null) {
					return value;
				}
			}
		}
		return key;
	}

    /**
     * Extracts the language key from the accept-language header to prevent server errors
     */
	public String extractLanguage(String lang) {
	    boolean found = false;
	    String retLang = lang;
	    for(String l: languages)
            if (retLang.contains(l) || retLang.equals(l)) {
                retLang = l;
                found = true;
            }
	    return (found) ? retLang : DEFAULT_LANG;
    }
	
	/**
	 * Exception thrown if transl8 cannot be reached.
	 */
	public class Transl8Exception extends Exception {
		
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor taking a message argument.
		 * @param message The exception message.
		 */
		public Transl8Exception(final String message) {
			super(message);
		}
		
		/**
		 * Constructor taking a message and a cause argument.
		 * @param message The exception message.
		 * @param cause The cause of the esception.
		 */
		public Transl8Exception(final String message, final Throwable cause) {
			super(message, cause);
		}
	}
}