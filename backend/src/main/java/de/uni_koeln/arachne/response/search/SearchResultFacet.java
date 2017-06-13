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
	 * The filter group
	 */
	private String group;
	
	/**
	 * The facet values.
	 */
	private final transient List<SearchResultFacetValue> values = new ArrayList<SearchResultFacetValue>();
	
	/**
	 * Convenience constructor to create an instance with name set.
	 * @param name The facet name.
	 */
	public SearchResultFacet(final String name) {
		this.name = name;
	}

	public SearchResultFacet(final String name, final String group) {
		this.name = name;
		this.group = group;
	}

	/**
	 * Getter for the facet name.
	 * @return The facet name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for the facet name.
	 * @param name The facet name.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Getter for the facet label.
	 * @return The facet label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Setter for the facet label.
	 * @param label The facet label.
	 */
	public void setLabel(final String label) {
		this.label = label;
	}

	/**
	 *
	 * @return string
	 */
	public String getGroup() {
		return group;
	}

	/**
	 *
	 * @param group
	 */
	public void setGroup(String group) {
		this.group = group;
	}


	/**
	 * Getter for the facet values.
	 * @return A list of facet values.
	 */
	public List<SearchResultFacetValue> getValues() {
		return values;
	}
	
	/**
	 * Adds a value to the facet value list.
	 * @param value The value to add.
	 */
	public void addValue(final SearchResultFacetValue value) {
		values.add(value);
	}
			
}
