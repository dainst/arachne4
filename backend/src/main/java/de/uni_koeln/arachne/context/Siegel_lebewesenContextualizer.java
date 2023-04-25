package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;

public class Siegel_lebewesenContextualizer extends AbstractContextualizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Siegel_lebewesenContextualizer.class);

	@Override
	public String getContextType() {
		return "siegel_lebewesen";
	}
	
	@Override
	public List<AbstractLink> retrieve(final Dataset parent) {
		final List<AbstractLink> result = new ArrayList<>(2);

		final List<Map<String, String>> lebewesenContextContents = genericSQLDao.getLebewesen(parent.getArachneId().getArachneEntityID());
		
		if (lebewesenContextContents != null) {
			final ListIterator<Map<String, String>> contextMap = lebewesenContextContents.listIterator();
			while (contextMap.hasNext()) {
				Map<String, String> context = contextMap.next();
				final ArachneLink link = new ArachneLink();
				link.setEntity1(parent);
				link.setEntity2(null);

				Map<String, String> fields = new HashMap<String, String>();

				context.forEach((key, value) -> {
					String strippedKey = key.replaceFirst("siegel_lebewesen\\.", "");
					fields.put(strippedKey, value);
				});				

				link.setFields(context);
				// if (!StrUtils.isEmptyOrNull(lebewesen)) {
				// 	Map<String, String> fields = new TreeMap<String, String>();
				// 	fields.put("Lebewesen", lebewesen);
				// 	link.setFields(fields);
				// }
				result.add(link);
			}
		}

		return result;
	}
}
