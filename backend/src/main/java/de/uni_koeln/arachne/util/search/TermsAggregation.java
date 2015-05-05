/**
 * 
 */
package de.uni_koeln.arachne.util.search;

import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

/**
 * Wrapper for an elasticsearch terms aggregation. 
 * 
 * @author Reimar Grabowski
 *
 */
public class TermsAggregation extends Aggregation {

	public static final String CATEGORY_FACET = "facet_kategorie";
	public static final String RELATION_FACET = "facet_ortsangabe";
	
	/**
	 * Possible sort orders.
	 */
	public static enum Order {
		DOC, TERMS
	}
	
	/**
	 * The sort order of the aggregation.
	 */
	private Order order = Order.DOC;
	
	/**
	 * Convenience constructor where name and field are equal and you can set the size. 
	 * @param name The name of the aggregation which must also be the field name in the elastic search index.
	 * @param size The maximum number of returned aggregation results.
	 */
	public TermsAggregation(String name, int size) {
		super(name, name, size);
	}

	/**
	 * Convenience constructor for terms aggregations where name and field are equal and you can set size and order. 
	 * @param name The name of the aggregation which must also be the field name in the elastic search index.
	 * @param size The maximum number of returned aggregation results.
	 * @param order The order in which the result values are sorted.
	 */
	public TermsAggregation(String name, int size, Order order) {
		super(name, name, size);
		this.order = order;
	}

	@Override
	public AbstractAggregationBuilder build() { 
		String field = this.field;
		field = "".equals(field) ? name : field;
		return AggregationBuilders.terms(name).field(field).order(getESOrder()).size(size);
	}
	
	/**
	 * @return the order
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(Order order) {
		this.order = order;
	}
	
	/**
	 * Returns a Elasticsearch API usable search order instance for terms aggregations based on the value of 
	 * <code>order</code>.
	 * @return The current search order.
	 */
	private org.elasticsearch.search.aggregations.bucket.terms.Terms.Order getESOrder() {
		switch (order) {
		case TERMS:
			return Terms.Order.term(true);
		default:
			return Terms.Order.count(false);
		}
	}
}
