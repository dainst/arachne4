package de.uni_koeln.arachne.response.search;

import java.text.Collator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Response for 'index requests' (not in the elasticsearch sense but requests for the list(s) of facet values). Values 
 * are sorted lexically by the default {@link java.text.Collator}. 
 * @author Reimar Grabowski
 */
public class IndexResult {
	private Set<String> facetValues = new TreeSet<>(Collator.getInstance());

	/**
	 * Getter for the values.
	 * @return The value list.
	 */
	public Set<String> getFacetValues() {
		return facetValues;
	}
	
	/**
	 * Adds a value to the value list.
	 * @param value The value to add.
	 */
	public void addValue(final String value) {
		facetValues.add(value);
	}
	
	/**
	 * Replaces the current <code>facetValues</code> with a subset of itself. 
	 * @param marker A marker indicating which sublist to generate.</br>
	 * '<'       : All terms starting with chars 'smaller' than '0'
	 * '$'       : All terms starting with a number
	 * 'a' - 'z' : All terms starting with the corresponding letter
	 * '>'       : All terms starting with charsequences 'larger' than 'zzz'
	 */
	public void reduce(final char marker) {
		if (marker == '<' || marker == '$' || (Character.isLetter(marker) && Character.isLowerCase(marker))) {
			String lowerLimit = "";
			String upperLimit = "";

			switch (marker) {
			case '<':
				upperLimit = "0";
				break;

			case '$':
				lowerLimit = "0";
				upperLimit = "a";
				break;
				
			case 'z':
				lowerLimit = Character.toString(marker);
				upperLimit = "zzz";
				break;
				
			default:
				lowerLimit = Character.toString(marker);
				upperLimit = Character.toString((char)(marker + 1));
				break;
			}

			facetValues = ((TreeSet<String>)facetValues).subSet(lowerLimit, upperLimit);
		} else {
			if (marker == '>') {
				facetValues = ((TreeSet<String>)facetValues).tailSet("zzz");
			}
		}
	}
}
