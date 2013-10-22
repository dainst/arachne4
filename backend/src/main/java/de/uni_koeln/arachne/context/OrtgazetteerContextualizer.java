package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
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

	@Override
	public String getContextType() {
		return "gazetteer";
	}

	@Override
	public List<AbstractLink> retrieve(final Dataset parent, final Integer offset, final Integer limit) {
		
		final List<AbstractLink> result = new ArrayList<AbstractLink>();
		
		final ExternalLink link = new ExternalLink();
		link.setEntity(parent);
		
		final RestTemplate restTemplate = new RestTemplate();
		final String gazId = parent.getFieldFromContext("ort.Gazetteerid");
		LOGGER.debug("gazId: {}", gazId);
		if (StrUtils.isEmptyOrNull(gazId)) {
			return null;
		}
		try {
			final long queryTime = System.currentTimeMillis();
			final String doc = restTemplate.getForObject("http://gazetteer.dainst.org/doc/{gazId}.json", String.class, gazId);
			LOGGER.debug("Query time: " + (System.currentTimeMillis() - queryTime) + " ms");
			final JSONObject jsonObject = new JSONObject(doc);
			
			final Map<String,String> fields = new HashMap<String,String>();
			final JSONObject prefName = jsonObject.optJSONObject("prefName");
			
			String title = null;
			if (prefName == null) {
				LOGGER.warn("Problem reading Gazetteer ID: " + gazId);
			} else {
				title = prefName.optString("title");
			}
			
			if (!StrUtils.isEmptyOrNull(title)) {
				fields.put("ortgazetteer.prefName", title);
			}
						
			final JSONObject prefLocation = jsonObject.optJSONObject("prefLocation"); 
			if (prefLocation != null) {
				final JSONArray coords = prefLocation.getJSONArray("coordinates");
				fields.put("ortgazetteer.lon", coords.getString(0));
				fields.put("ortgazetteer.lat", coords.getString(1));
			}
			
			link.setFields(fields);
			result.add(link);
		} catch (JSONException e) {
			LOGGER.error("Error while parsing JSON response for request: http://gazetteer.dainst.org/doc/" + gazId + ".json", e);
		} catch (HttpClientErrorException e) {
			LOGGER.error("Unable to get gazetteer data for id: " + gazId, e);
		}
		
		return result;
		
	}

}
