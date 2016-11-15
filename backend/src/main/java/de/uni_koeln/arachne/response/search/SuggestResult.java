package de.uni_koeln.arachne.response.search;

import java.util.ArrayList;
import java.util.List;

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
	private List<String> suggestions = new ArrayList<>();
	
	/**
	 * Getter for the suggestions.
	 * @return The list of suggestions.
	 */
	public List<String> getSuggestions() {
		return suggestions;
	}

	/**
	 * Method to add one suggestion to the list of suggestions.
	 * @param suggestion The suggestion to add.
	 */
	public void addSuggestion(final String suggestion) {
		suggestions.add(suggestion);
	}
}
