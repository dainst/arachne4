package de.uni_koeln.arachne.response;

/**
 * This class represents one facet value of a <code>SearchResultFacet</code>.
 */
public class SearchResultFacetValue {
	
	/**
	 * The value of the facet.
	 */
	private String value;
	
	/**
	 * The number of times this facet occurs in the search result.
	 */
	private long count;

	/**
	 * Convenience constructor to create a completely filled instance.
	 * @param value The value of the facet.
	 * @param label The translated value of the facet.
	 * @param count The number of occurrences in the search result. 
	 */
	public SearchResultFacetValue(final String value, final String label, final long count) {
		this.value = value;
		this.count = count;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public long getCount() {
		return count;
	}

	public void setCount(final long count) {
		this.count = count;
	}
}
