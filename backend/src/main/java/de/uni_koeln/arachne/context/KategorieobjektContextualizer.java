package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.sqlutil.SQLToolbox;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * This contextualizer is used to determine if an image belongs to the unstructured ones or not.
 */
public class KategorieobjektContextualizer extends AbstractContextualizer {

	private final transient List<String> subcategories = new ArrayList<String>(9);
	
	public KategorieobjektContextualizer() {
		// TODO - this should be more flexible, same as SingleEntityDataService
		subcategories.add("objektbauornamentik");
		subcategories.add("objektgemaelde");
		subcategories.add("objektkeramik");
		subcategories.add("objektlebewesen");
		subcategories.add("objektmosaik");
		subcategories.add("objektplastik");
		subcategories.add("objektplomben");
		subcategories.add("objektsiegel");
		subcategories.add("objektterrakotten");
	}
	
	@Override
	public String getContextType() {
		return null;
	}

	/**
	 * This method does not retrieve anything. It just changes the dataset directly. If any of the subcategories primary key field 
	 * is present the field "KategorieObjekt.Typ" is added and set to the corresponding value.
	 */
	@Override
	public List<AbstractLink> retrieve(final Dataset parent) {
		final Set<String> keySet = parent.getFields().keySet();
		String subcategoryValue = "";
		for (final String objektSubcategory: subcategories) {
			if (keySet.contains(objektSubcategory + '.' + SQLToolbox.generatePrimaryKeyName(objektSubcategory))) {
				// write multiple values as ';' separated list
				subcategoryValue += SQLToolbox.ucFirst(objektSubcategory.substring(6)) + ';'; // NOPMD
			}
		}
		if (!StrUtils.isEmptyOrNullOrZero(subcategoryValue)) {
			subcategoryValue = subcategoryValue.substring(0, subcategoryValue.length() - 1);
		}
		final Map<String, String> subcategory = new HashMap<String, String>();
		subcategory.put("KategorieObjekt.Typ", subcategoryValue);
		parent.appendFields(subcategory);
		return null;
	}

}
