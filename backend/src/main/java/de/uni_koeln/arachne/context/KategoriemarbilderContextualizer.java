package de.uni_koeln.arachne.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * This contextualizer is used to determine if an image belongs to the unstructured ones or not.
 */
public class KategoriemarbilderContextualizer extends AbstractContextualizer {

	@Override
	public String getContextType() {
		return null;
	}

	/**
	 * This method does not retrieve anything. It just changes the dataset directly. If the field 
	 * "marbilderbestand.DateinameMarbilderbestand" is not present in the dataset the field 'KategorieMarbilder.Typ' 
	 * is added and set to "strukturiert" else it is set to "unstrukturiert".
	 */
	@Override
	public List<AbstractLink> retrieve(final Dataset parent, final Integer offset, final Integer limit) {
		final Map<String, String> subcategory = new HashMap<String, String>();
		if (StrUtils.isEmptyOrNull(parent.getField("marbilderbestand.DateinameMarbilderbestand"))) {
			subcategory.put("KategorieMarbilder.Typ", "strukturiert");
		} else {
			subcategory.put("KategorieMarbilder.Typ", "unstrukturiert");
		}
		parent.appendFields(subcategory);
		return null;
	}

}
