package de.uni_koeln.arachne.util.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.util.StrUtils;

/**
 * Class that holds information about the fields to search on in elasticsearch.
 * It holds two immutable lists of search fields (numeric and text) and convenience methods to make working with the 
 * lists easier. 
 * @author Reimar Grabowski
 */
public class SearchFieldList {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchFieldList.class);

	private Map<String, Float> textFields;
	private Map<String, Float> numericFields;
	
	/**
	 * Constructor that takes two lists of field names.
	 * @param textSearchFields The text fields to search on.
	 * @param numericSearchFields The numeric fields to search on.
	 */
	public SearchFieldList(final List<String> textSearchFields, final List<String> numericSearchFields) {
		if (!StrUtils.isEmptyOrNull(textSearchFields)) {
			textFields = initFields(textSearchFields);
		} else {
			LOGGER.warn("No text search fields provided. Check 'application.properties' file.");
		}
		
		if (!StrUtils.isEmptyOrNull(numericSearchFields)) {
			numericFields = initFields(numericSearchFields);
		} else {
			LOGGER.warn("No numeric search fields provided. Check 'application.properties' file.");
		}
	}

	/**
	 * Returns <code>true</code> if the field is contained in the list of text search fields.
	 * @param field The field name to be looked up.
	 * @return If the field is in the list of text search fields.
	 */
	public boolean containsText(final String field) {
		return textFields.containsKey(field);
	}
	
	/**
	 * Returns <code>true</code> if the field is contained in the list of numeric search fields.
	 * @param field The field name to be looked up.
	 * @return If the field is in the list of numeric search fields.
	 */
	public boolean containsNumeric(final String field) {
		return numericFields.containsKey(field);
	}
	
	/**
	 * Getter for the map of text search fields (including boost values). Use this to pass values to elasticsearch.
	 * @return A map of the textual search fields including boost values.
	 */
	public Map<String, Float> text() {
		return textFields;
	}
	
	/**
	 * Getter for the map of numeric search fields (including boost values. Use this list to pass values to elasticsearch.
	 * @return A map of the numeric search fields with boost values.
	 */
	public Map<String, Float> numeric() {
		return numericFields;
	}

	private Map<String, Float> initFields(final List<String> searchFields) {
		final Map<String, Float> fields = new HashMap<String, Float>();
		for (final String field: searchFields) {
			int boostCharPos = field.indexOf('^');
			if (boostCharPos > 0) {
				fields.put(
					field.substring(0, boostCharPos),
					Float.parseFloat(field.substring(boostCharPos + 1))
				);
			} else {
				fields.put(field, 1.0f);
			}
		}
		return fields;
	}

}
