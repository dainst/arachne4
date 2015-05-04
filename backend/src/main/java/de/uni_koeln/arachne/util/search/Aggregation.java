package de.uni_koeln.arachne.util.search;

import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

/**
 * Class to hold all information about a facet like name, type, field to work on, etc.
 * <br>
 * For hashCode and equals only name, type and field are considered for equality as they identify a facet. The other 
 * fields of this class are parameters for the facet execution.
 * 
 * @author Reimar Grabowski
 */
public class Aggregation {

	/**
	 * Possible sort orders.
	 */
	public static enum Order {
		DOC, TERMS
	}
	
	/**
	 * Supported aggregation types.
	 */
	public static enum Type {
		TERMS, GEOHASH
	}
	
	public static final String CATEGORY_FACET = "facet_kategorie";
	public static final String RELATION_FACET = "facet_ortsangabe";
	
	public static final String GEO_HASH_GRID_NAME = "agg_geogrid";
	public static final String GEO_HASH_GRID_FIELD = "places.location";
	
	/**
	 * The name of the aggregation.
	 */
	private String name = "";
	
	/**
	 * The field name in the Elasticsearch index to aggregate results for.
	 */
	private String field = "";
	
	/**
	 * The aggregations type.
	 */
	final private Type type;
	
	/**
	 * The maximum number of results/buckets for this aggregation.
	 */
	private int size = 0;
	
	/**
	 * The sort order of the aggregation.
	 */
	private Order order = Order.DOC;
	
	/**
	 * The geohash precision of a aggregation of type 'GEOHASH'.
	 */
	private int geoHashPrecision = 5;
	
	/**
	 * Per default a terms aggregation is constructed. 
	 * @param builderType The ES AggregationsBuilder class that is used to generate the aggregation.
	 */
	public Aggregation() {
		this.type = Type.TERMS;
	}
	
	/**
	 * Convenience constructor for terms aggregations where name and field are equal and you can set the size. 
	 * @param name The name of the aggregation which must also be the field name in the elastic search index.
	 * @param size The maximum number of returned aggregation results.
	 */
	public Aggregation(final String name, final int size) {
		type = Type.TERMS;
		this.name = name;
		this.field = name;
		this.size = size;
	}
	
	/**
	 * Convenience constructor for terms aggregations where name and field are equal and you can set size and order. 
	 * @param name The name of the aggregation which must also be the field name in the elastic search index.
	 * @param size The maximum number of returned aggregation results.
	 * @param order The order in which the result values are sorted.
	 */
	public Aggregation(final String name, final int size, final Order order) {
		type = Type.TERMS;
		this.name = name;
		this.field = name;
		this.size = size;
		this.order = order;
	}
	
	/**
	 * Constructor to set all non type specific fields except.
	 * @param type The type of the aggregation (TERMS or GEOHASH).
	 * @param name The name of the aggregation which must also be the field name in the elastic search index.
	 * @param field The field in the Elasticseasrch index this aggregation works on.
	 * @param size The maximum number of returned aggregation results.
	 */
	public Aggregation(final Type type, final String name, final String field, final int size) {
		this.type = type;
		this.name = name;
		this.field = field;
		this.size = size;
	}

	/**
	 * Builds an aggregation that is usable by the Elasticsearch API from the current values. If <code>field</code> is 
	 * not set <code>name</code> is used instead. 
	 * @return
	 */
	public AbstractAggregationBuilder build() {
		String field = this.field;
		field = "".equals(field) ? name : field;
		switch (type) {
		case TERMS:
			return AggregationBuilders.terms(name).field(field).order(getESOrder()).size(size);
			
		case GEOHASH:
			return AggregationBuilders.geohashGrid(name).field(field).precision(geoHashPrecision).size(size);
			
		default:
			break;
		}
		return null;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the field.
	 */
	public String getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
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
	 * @return the type
	 */
	public Type getType() {
		return type;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Aggregation)) {
			return false;
		}
		Aggregation other = (Aggregation) obj;
		if (field == null) {
			if (other.field != null) {
				return false;
			}
		} else if (!field.equals(other.field)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}
}
