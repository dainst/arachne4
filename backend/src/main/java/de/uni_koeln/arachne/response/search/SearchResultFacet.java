package de.uni_koeln.arachne.response.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold one facet of a search result with ordered facet values.
 * 
 * @author Reimar Grabowski
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
	 * The filter group.
	 */
	private String group;

	/**
	 * The filter dependency.
	 */
	private String dependsOn;

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

	/**
	 * Convenience constructor to create an instance with name, group and dependsOn set.
	 * @param name The facet name.
	 * @param group The filter group.
	 * @param dependsOn The filter dependency.
	 */
	public SearchResultFacet(final String name, final String group, final String dependsOn) {
		this.name = name;
		this.group = group;
		this.dependsOn = dependsOn;
	}

	/**
	 * Gets the facet internal name.
	 *
	 * @return the facet internal name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the facet internal name.
	 *
	 * @param name
	 *            the new facet internal name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Gets the translated facet name to show in the frontend.
	 *
	 * @return the translated facet name to show in the frontend
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the translated facet name to show in the frontend.
	 *
	 * @param label
	 *            the new translated facet name to show in the frontend
	 */
	public void setLabel(final String label) {
		this.label = label;
	}

	/**
	 * Gets the filter group.
	 *
	 * @return the filter group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Sets the filter group.
	 *
	 * @param group
	 *            the new filter group
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Gets the filter dependency.
	 *
	 * @return the filter dependency
	 */
	public String getDependsOn() {
		return dependsOn;
	}

	/**
	 * Sets the filter dependency.
	 *
	 * @param dependsOn
	 *            the new filter dependency
	 */
	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
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
