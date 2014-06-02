package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.StrUtils;

public class OrtgazetteerContextualizer extends AbstractContextualizer implements IContextualizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrtgazetteerContextualizer.class);

	private transient Map<String, ExternalLink> cachedContexts = new LinkedHashMap<String, ExternalLink>();
	
	private transient final RestTemplate restTemplate = new RestTemplate();
	
	@Override
	public String getContextType() {
		return "gazetteer";
	}

	@Override
	public List<AbstractLink> retrieve(final Dataset parent) {
		
		final String gazId = parent.getFieldFromContext("ort.Gazetteerid");
		LOGGER.debug("gazId: {}", gazId);
		if (StrUtils.isEmptyOrNullOrZero(gazId)) {
			return null;
		}

		final List<AbstractLink> result = new ArrayList<AbstractLink>();
		ExternalLink link = cachedContexts.get(gazId);

		if (link == null) {
			link = new ExternalLink();
			link.setEntity(parent);
			final Map<String,String> fields = new HashMap<String,String>();
			
			try {
				final long queryTime = System.currentTimeMillis();
				final String doc = restTemplate.getForObject("http://gazetteer.dainst.org/doc/{gazId}.json", String.class, gazId);
				LOGGER.debug("Query time: " + (System.currentTimeMillis() - queryTime) + " ms");
				final JSONObject jsonObject = new JSONObject(doc);

				final JSONObject prefName = jsonObject.optJSONObject("prefName");

				String title = null;
				if (prefName == null) {
					LOGGER.warn("Problem reading Gazetteer ID: " + gazId);
				} else {
					title = prefName.optString("title");
				}

				if (!StrUtils.isEmptyOrNullOrZero(title)) {
					fields.put("ortgazetteer.prefName", title);
				}

				final JSONObject prefLocation = jsonObject.optJSONObject("prefLocation"); 
				if (prefLocation != null) {
					final JSONArray coords = prefLocation.getJSONArray("coordinates");
					fields.put("ortgazetteer.lon", coords.getString(0));
					fields.put("ortgazetteer.lat", coords.getString(1));
				}

				link.setFields(fields);
				cachedContexts.put(gazId, link);
				result.add(link);
			} catch (JSONException e) {
				LOGGER.error("Error while parsing JSON response for request: http://gazetteer.dainst.org/doc/" + gazId + ".json", e);
			} catch (HttpClientErrorException e) {
				LOGGER.error("Unable to get gazetteer data for id: " + gazId, e);
				final StringBuilder failedId = new StringBuilder("Unknown GazetteerID (");
				failedId.append(gazId);
				failedId.append(") [");
				failedId.append(e.getStatusText());
				failedId.append("]");
				fields.put("ortgazetteer.prefName", failedId.toString());
				link.setFields(fields);
				cachedContexts.put(gazId, link);
				result.add(link);
			}
		} else {
			result.add(link);
		}
		return result;
	}
	
}
