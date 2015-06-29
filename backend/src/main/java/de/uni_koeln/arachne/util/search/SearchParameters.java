package de.uni_koeln.arachne.util.search;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Convenience class to hold search parameters. As far as it is possible values are stored as the primitive types and 
 * unboxed in the setter methods which take the object types as parameters.
 * 
 * @author Reimar Grabowski
 */
@Component
@Scope("request")
public class SearchParameters {

	/**
	 * The query string.
	 */
	private String query;

	/**
	 * The maximum number of returned entities.
	 */
	private int limit;

	/**
	 * An offset into the search result.
	 */
	private int offset = 0;

	/**
	 * The maximum number of distinct facet values returned. Zero means that all values will be returned.
	 */
	private int facetLimit = 0;

	/**
	 * The name of the field to sort on.
	 */
	private String sortField = "";

	/**
	 * A flag indicating if the sort order is descending
	 */
	private boolean orderDesc = false;

	/**
	 * The coordinates (upper left, lower right) of a geo bounding box to filter the results on.
	 */
	private Double[] boundingBox = {};

	/**
	 * The precision of the geohash used in the geo grid aggregation.
	 */
	private int geoHashPrecision;

	/**
	 * Constructor that sets a default limit.
	 * @param defaultLimit The limit to set. Must be greater than -1.
	 */
	public SearchParameters(final int defaultLimit) {
		if (defaultLimit > -1) { 
			limit = defaultLimit;
		}
	}
	
	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public SearchParameters setQuery(String query) {
		this.query = query;
		return this;
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public SearchParameters setLimit(Integer limit) {
		if (limit != null && limit > -1) {
			this.limit = limit;
		}
		return this;
	}

	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @param offset the offset to set
	 */
	public SearchParameters setOffset(Integer offset) {
		if (offset != null && offset > 0) {
			this.offset = offset;
		}
		return this;
	}

	/**
	 * @return the facetLimit
	 */
	public int getFacetLimit() {
		return facetLimit;
	}

	/**
	 * @param facetLimit the facetLimit to set
	 */
	public SearchParameters setFacetLimit(Integer facetLimit) {
		if (facetLimit != null && facetLimit > -1) {
			this.facetLimit = facetLimit;
		}
		return this;
	}

	/**
	 * @return the sortField
	 */
	public String getSortField() {
		return sortField;
	}

	/**
	 * @param sortField the sortField to set
	 */
	public SearchParameters setSortField(String sortField) {
		if (sortField != null) {
			this.sortField = sortField;
		}
		return this;
	}

	/**
	 * @return the orderDesc
	 */
	public boolean isOrderDesc() {
		return orderDesc;
	}

	/**
	 * @param orderDesc the orderDesc to set
	 */
	public SearchParameters setOrderDesc(Boolean orderDesc) {
		if (orderDesc != null) {
			this.orderDesc = orderDesc;
		}
		return this;
	}

	/**
	 * @return the boundingBox
	 */
	public Double[] getBoundingBox() {
		return boundingBox;
	}

	/**
	 * @param boundingBox the boundingBox to set
	 */
	public SearchParameters setBoundingBox(Double[] boundingBox) {
		if (boundingBox != null && boundingBox.length == 4) {
			this.boundingBox = boundingBox;
		}
		return this;
	}

	/**
	 * @return the geoHashPrecision
	 */
	public int getGeoHashPrecision() {
		return geoHashPrecision;
		
	}

	/**
	 * @param geoHashPrecision the geoHashPrecision to set
	 */
	public SearchParameters setGeoHashPrecision(Integer geoHashPrecision) {
		// limit geohash precision to 10 as it is plenty of resolution
		if (geoHashPrecision != null && geoHashPrecision > 0 && geoHashPrecision < 11) {
			this.geoHashPrecision = geoHashPrecision;
		}
		return this;
	}

}
