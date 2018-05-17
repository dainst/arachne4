package de.uni_koeln.arachne.util.search;

import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;

/**
 * Wrapper for an elasticsearch terms aggregation that also uses term filters and
 * inspects the places object. 
 * 
 * @author Richard Henck
 *
 */
public class NestedTermsAggregation extends TermsAggregation {

    private Map<String, String> selectedPlaceFacets;

    public NestedTermsAggregation(String name, int limit, Map<String, String> selectedFacets) {
        super(name, limit);
        
        for (String key : selectedFacets.keySet()) {
            if (key.equals("facet_ortsangabe")) {
                selectedFacets.put("facet_relation", selectedFacets.get(key));
                selectedFacets.remove(key);
            } else if (key.equals("facet_land")) {
                selectedFacets.put("facet_country", selectedFacets.get(key));
                selectedFacets.remove(key);
            } else if (key.equals("facet_ort")) {
                selectedFacets.put("facet_city", selectedFacets.get(key));
                selectedFacets.remove(key);
            }
        }
        selectedPlaceFacets = selectedFacets;
    }
    
    private BoolQueryBuilder buildQueryFilters() {
        BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();

        for (String facetName : selectedPlaceFacets.keySet()) {
            String filterKey = facetName.substring(facetName.indexOf("_")+1);
            boolFilter.must(QueryBuilders.termQuery("places." + filterKey, selectedPlaceFacets.get(facetName)));
        }
        return boolFilter;
    }

    @Override
	public AbstractAggregationBuilder build() {
		// Fix some naming inconsistencies between facet names and places properties
		String facetName = "";
        if (name.equals("facet_ortsangabe")) {
            facetName = "relation";
        } else if (name.equals("facet_land")) {
            facetName = "country";
        } else if (name.equals("facet_ort")) {
            facetName = "city";
        } else {
            facetName = name.substring(name.indexOf("_") + 1);
        }

        BoolQueryBuilder boolFilter = buildQueryFilters();
        
        FilterAggregationBuilder nestedFilter = AggregationBuilders.filter("nestedFilter").
            filter((QueryBuilder) boolFilter);

        return AggregationBuilders.nested(name).path("places")
                .subAggregation(nestedFilter
                .subAggregation(
                    AggregationBuilders.terms("newFacet").field("places." + facetName)
            ));
	}
}
