package de.uni_koeln.arachne.util.search;

import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

import de.uni_koeln.arachne.util.StrUtils;

/**
 * Wrapper for an elasticsearch geo hash grid aggregation for nested location object.
 * 
 * @author Richard Henck
 *
 */
public class NestedGeoHashGridAggregation extends GeoHashGridAggregation {

    public NestedGeoHashGridAggregation(String name, String field, int precision, int size) {
        super(name, field, precision, size);
    }

    public AbstractAggregationBuilder build() { 
        if (StrUtils.isEmptyOrNull(name) || StrUtils.isEmptyOrNull(field)) {
            return null;
        }

        return AggregationBuilders.nested(name).path("places").subAggregation(
            AggregationBuilders.geohashGrid(name).field(field).precision(precision).size(size));
    }
}
