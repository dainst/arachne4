package de.uni_koeln.arachne.util.search;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import de.uni_koeln.arachne.util.StrUtils;

/**
 * Class that holds information about the fields to search on in elasticsearch.
 * It holds two immutable lists of search fields (numeric and text) and convenience methods to make working with the 
 * lists easier. 
 * @author Reimar Grabowski
 */
public class SearchFieldList {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchFieldList.class);
	
	private ImmutableList<String> textSearchFields;
	private ImmutableList<String> textSearchFieldsWithBoosts;
	
	private ImmutableList<String> numericSearchFields;
	private ImmutableList<String> numericSearchFieldsWithBoosts;
	
	/**
	 * Constructor that takes two lists of field names.
	 * @param textSearchFields The text fields to search on.
	 * @param numericSearchFields The numeric fields to search on.
	 */
	public SearchFieldList(final List<String> textSearchFields, final List<String> numericSearchFields) {
		if (!StrUtils.isEmptyOrNull(textSearchFields)) {
			SearchFieldListPair textLists = initLists(textSearchFields);
			this.textSearchFields = textLists.getList();
			this.textSearchFieldsWithBoosts = textLists.getListWithBoosts();			
		} else {
			LOGGER.warn("No text search fields provided. Check 'application.properties' file.");
		}
		
		if (!StrUtils.isEmptyOrNull(numericSearchFields)) {
			SearchFieldListPair numericLists = initLists(numericSearchFields);
			this.numericSearchFields = numericLists.getList();
			this.numericSearchFieldsWithBoosts = numericLists.getListWithBoosts();
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
		return textSearchFields.contains(field);
	}
	
	/**
	 * Returns <code>true</code> if the field is contained in the list of numeric search fields.
	 * @param field The field name to be looked up.
	 * @return If the field is in the list of numeric search fields.
	 */
	public boolean containsNumeric(final String field) {
		return numericSearchFields.contains(field);
	}
	
	/**
	 * Getter for the list of text search fields (including boost values). Use this list to pass values to elasticsearch.
	 * @return
	 */
	public ImmutableList<String> text() {
		return textSearchFieldsWithBoosts;
	}
	
	/**
	 * Getter for the list of text search fields (including boost values). Use this list to pass values to elasticsearch.
	 * @return
	 */
	public ImmutableList<String> textNoBoosts() {
		return textSearchFields;
	}
	
	/**
	 * Getter for the list of text search fields (including boost values. Use this list to pass values to elasticsearch.
	 * @return
	 */
	public ImmutableList<String> numeric() {
		return numericSearchFieldsWithBoosts;
	}
	
	/**
	 * Getter for the list of text search fields (including boost values. Use this list to pass values to elasticsearch.
	 * @return
	 */
	public ImmutableList<String> numericNoBoosts() {
		return numericSearchFields;
	}
	
	/**
	 * Method initializing two search field lists. One list only contains the field name while the other contains the 
	 * field name and an appended boost value in elasticsearch syntax.
	 * @param searchFields The list of search fields.
	 * @param listWithoutBoosts The list only containing the field names. This will be newly created.
	 * @return The list containing field names with added boost values.
	 */
	private SearchFieldListPair initLists(final List<String> searchFields) {
		final List<String> list = new ArrayList<String>();
		for (final String field: searchFields) {
			int boostCharPos = field.indexOf('^');
			if (boostCharPos > 0) {
				list.add(field.substring(0, boostCharPos));
			} else {
				list.add(field);
			}
		}
		return new SearchFieldListPair(list, searchFields);
	}

}