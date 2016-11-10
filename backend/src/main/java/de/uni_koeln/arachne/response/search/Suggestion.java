package de.uni_koeln.arachne.response.search;

import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.arachne.response.FormattedArachneEntity;

/**
 * Elasticsearch suggestion field for {@link FormattedArachneEntity}.
 * @author Reimar Grabowski
 */
public class Suggestion {
	/**
	 * The input suggest terms.
	 */
	private List<String> input = new ArrayList<>();
	
	/**
	 * The suggestion weight (an integer ranking factor). Defaults to 100:
	 */
	private int weight = 100;

	/**
	 * Getter for input.
	 * @return The list of suggestions.
	 */
	public List<String> getInput() {
		return input;
	}

	/**
	 * Adds a suggestion to the input list.
	 * @param suggestion The suggestion term.
	 */
	public void add(final String suggestion) {
		input.add(suggestion);
	}

	/**
	 * Getter for the weight (ranking factor) for the suggestions.
	 * @return The weight.
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Getter for the weight (ranking factor) for the suggestions.
	 * @param weight The new weight.
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
}
