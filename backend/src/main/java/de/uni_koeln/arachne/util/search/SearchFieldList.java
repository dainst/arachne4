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
	 * Constructor that takes two lists of field names as comman separated <code>Strings</code>.
	 * @param textSearchFieldsAsString The text fields to search on.
	 * @param numericSearchFieldsAsString The numeric fields to search on.
	 */
	public SearchFieldList(final String textSearchFieldsAsString, final String numericSearchFieldsAsString) {
		if (!StrUtils.isEmptyOrNull(textSearchFieldsAsString)) {
			SearchFieldListPair textLists = initLists(textSearchFieldsAsString);
			textSearchFields = textLists.getList();
			textSearchFieldsWithBoosts = textLists.getListWithBoosts();			
		} else {
			LOGGER.warn("No text search fields provided. Check 'application.properties' file.");
		}
		
		if (!StrUtils.isEmptyOrNull(numericSearchFieldsAsString)) {
			SearchFieldListPair numericLists = initLists(numericSearchFieldsAsString);
			numericSearchFields = numericLists.getList();
			numericSearchFieldsWithBoosts = numericLists.getListWithBoosts();
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
	 * Getter for the list of text search fields (including boost values. Use this list to pass values to elasticsearch.
	 * @return
	 */
	public ImmutableList<String> numeric() {
		return numericSearchFieldsWithBoosts;
	}
	
	/**
	 * Method initializing two search field lists. One list only contains the field name while the other contains the 
	 * field name and an appended boost value in elasticsearch syntax.
	 * @param searchFields The search fields as comma separated <code>String</code>.
	 * @param listWithoutBoosts The list only containing the field names. This will be newly created.
	 * @return The list containing field names with added boost values.
	 */
	private SearchFieldListPair initLists(final String searchFields) {
		final List<String> listWithBoosts = StrUtils.getCommaSeperatedStringAsList(searchFields);
		final List<String> list = new ArrayList<String>();
		for (final String field: listWithBoosts) {
			int boostCharPos = field.indexOf('^');
			if (boostCharPos > 0) {
				list.add(field.substring(0, boostCharPos));
			} else {
				list.add(field);
			}
		}
		return new SearchFieldListPair(list, listWithBoosts);
	}

}
