package de.uni_koeln.arachne.response.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold one facet of a search result with ordered facet values. 
 */
public class SearchResultFacet {

	/**
	 * The facet internal name.
	 */
	private String name;
	
	/**
	 * The translated facet name to show in the frontend.
	 */
	private String label;
	
	/**
	 * The facet values.
	 */
	private final transient List<SearchResultFacetValue> values = new ArrayList<SearchResultFacetValue>();
	
	/**
	 * Convenience constructor to create an instance with name set.
	 * @param name
	 */
	public SearchResultFacet(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public List<SearchResultFacetValue> getValues() {
		return values;
	}
	
	public void addValue(final SearchResultFacetValue value) {
		values.add(value);
	}
			
}
