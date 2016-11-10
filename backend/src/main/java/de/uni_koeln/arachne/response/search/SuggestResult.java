package de.uni_koeln.arachne.response.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Response class for suggest requests.
 * 
 * @author Reimar Grabowski
 *
 */
@JsonInclude(Include.NON_DEFAULT)
public class SuggestResult {
	// matches the reserved characters of the elasticsearch "mini-query-language" and white spaces
	private static Pattern escape = Pattern.compile("([+\\-=><!(){}\\[\\]^\"~*?:\\\\/\\040])"); 
	
	private List<String> suggestions = new ArrayList<>();

	/**
	 * Getter for the <code>suggestions</code>.
	 * @return The list of suggestions.
	 */
	public List<String> getSuggestions() {
		return suggestions;
	}

	/**
	 * Method to add one suggestion to the list of suggestions.
	 * Reserved characters are escaped so that the stored suggestions can be plugged into elasticsearch query string 
	 * queries directly. Spaces are also escaped so that the query string parser does not touch them.
	 * @param suggestion The suggestion to add.
	 */
	public void addSuggestion(final String suggestion) {
		suggestions.add(escapeSuggestion(suggestion));
	}
	
	/**
	 * Escapes the reserved characters of the elasticsearch "mini-query-language" and white spaces.
	 * @param suggestion The suggestion to escape.
	 * @return The suggestion with escaped characters.
	 */
	private String escapeSuggestion(final String suggestion) {
		Matcher matcher = escape.matcher(suggestion);
		return matcher.replaceAll("\\\\$1");
	}
}
