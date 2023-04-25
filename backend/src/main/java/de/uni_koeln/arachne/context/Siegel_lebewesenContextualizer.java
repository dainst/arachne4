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

				String combined_row = createCombinedString(context);

				fields.put("siegel_lebewesen.combined_row", combined_row);
				context.forEach((key, value) -> {
					// Also put single column values as fields
 					fields.put(key, value);
                });

				link.setFields(fields);
				result.add(link);
			}
		}
		return result;
	}

	private String createCombinedString(Map<String, String> context) {

		String result = "";

		String lw1 = context.get("siegel_lebewesen.LW1");
		if (!StrUtils.isEmptyOrNull(lw1)) {
			result += lw1;
		}

		String lw2 = context.get("siegel_lebewesen.LW2");
		if (!StrUtils.isEmptyOrNull(lw2)) {
			result += " / " + lw2;
		}
		
		String lw3 = context.get("siegel_lebewesen.LW3");
		if (!StrUtils.isEmptyOrNull(lw3)) {
			result += " / " + lw3;
		}

		String art = context.get("siegel_lebewesen.Art");
		if (!StrUtils.isEmptyOrNull(art)) {
			result += " (" + art + ") - ";
		}

		String ansicht = context.get("siegel_lebewesen.stAnsicht");
		if (!StrUtils.isEmptyOrNull(ansicht)) {
			result += "standardisierte Ansicht: " + ansicht;
		}

		String haltung = context.get("siegel_lebewesen.sHaltung");
		if (!StrUtils.isEmptyOrNull(haltung)) {
			result += " | Haltung: " + haltung;
		}

		String kopf = context.get("siegel_lebewesen.Kopfrich");
		if (!StrUtils.isEmptyOrNull(kopf)) {
			result += " | Kopfrichtung: " + kopf;
		}

		String hals = context.get("siegel_lebewesen.Halsrich");
		if (!StrUtils.isEmptyOrNull(hals)) {
			result += " | Halsrichtung: " + hals;
		}

		String geschlecht = context.get("siegel_lebewesen.sGeschlecht");
		if (!StrUtils.isEmptyOrNull(geschlecht) && !geschlecht.equals("nein")) {
			result += " | Geschlecht: " + geschlecht;
		}

		String kleidung = "";

		String kleidung_a = context.get("siegel_lebewesen.KleidungA");
		if (!StrUtils.isEmptyOrNull(kleidung_a) && !kleidung_a.equals("nein")) {
			kleidung += kleidung_a + ", ";
		}

		String kleidung_b = context.get("siegel_lebewesen.KleidungB");
		if (!StrUtils.isEmptyOrNull(kleidung_b) && !kleidung_b.equals("nein")) {
			kleidung += kleidung_b + ", ";
		}

		String kleidung_c = context.get("siegel_lebewesen.KleidungC");
		if (!StrUtils.isEmptyOrNull(kleidung_c) && !kleidung_c.equals("nein")) {
			kleidung += kleidung_c;
		}

		kleidung = kleidung.replaceAll(", $", "");
		if (kleidung != "") result += " | Kleidung: " + kleidung;

		return result;
	}
}
