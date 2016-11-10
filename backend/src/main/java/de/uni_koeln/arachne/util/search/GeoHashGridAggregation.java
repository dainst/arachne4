/**
 * 
 */
package de.uni_koeln.arachne.util.search;

import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

import de.uni_koeln.arachne.util.StrUtils;

/**
 * Wrapper for an elasticsearch geo hash grid aggregation. 
 * 
 * @author Reimar Grabowski
 *
 */
public class GeoHashGridAggregation extends Aggregation {

	public static final String GEO_HASH_GRID_NAME = "agg_geogrid";
	public static final String GEO_HASH_GRID_FIELD = "places.location";
	
	/**
	 * The geohash precision of a aggregation of type 'GEOHASH'.
	 */
	private int precision = 5;
		
	/**
	 * Constructor to set all fields except 'precision'.
	 * @param name The name of the aggregation which must also be the field name in the elastic search index.
	 * @param field The field in the Elasticseasrch index this aggregation works on.
	 * @param size The maximum number of returned aggregation results.
	 */
	public GeoHashGridAggregation(String name, String field, int size) {
		super(name, field, size);
	}

	/**
	 * Constructor to set all fields including 'precision'.
	 * @param name The name of the aggregation which must also be the field name in the elastic search index.
	 * @param field The field in the Elasticseasrch index this aggregation works on.
	 * @param precision The geo hash precision.
	 * @param size The maximum number of returned aggregation results.
	 */
	public GeoHashGridAggregation(String name, String field, int precision, int size) {
		super(name, field, size);
		this.precision = precision;
	}
		
	public AbstractAggregationBuilder build() { 
		if (StrUtils.isEmptyOrNull(name) || StrUtils.isEmptyOrNull(field)) {
			return null;
		}
		return AggregationBuilders.geohashGrid(name).field(field).precision(precision).size(size);
	}
}
