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

	public static enum Order {
		DOC, TERMS
	}
	
	public static enum Type {
		TERMS, GEOHASH
	}
	
	public static final String CATEGORY_FACET = "facet_kategorie";
	public static final String RELATION_FACET = "facet_ortsangabe";
	
	public static final String GEO_HASH_GRID_NAME = "agg_geogrid";
	public static final String GEO_HASH_GRID_FIELD = "places.location";
		
	private String name = "";
	
	private String field = "";
	
	final private Type type;
	
	private int size = 0;
	
	private Order order = Order.DOC;
	
	private int geoHashPrecision = 5;
	
	/**
	 * Per default a terms aggregation is constructed. 
	 * @param builderType The ES AggregationsBuilder class that is used to generate the aggregation.
	 */
	public Aggregation() {
		this.type = Type.TERMS;
	}
	
	public Aggregation(final String name, final String field, final int size) {
		type = Type.TERMS;
		this.name = name;
		this.field = field;
		this.size = size;
	}
	
	public Aggregation(final Type type, final String name, final String field, final int size, 
			final Order order) {
		this.type = type;
		this.name = name;
		this.field = field;
		this.size = size;
		this.order = order;
	}

	public AbstractAggregationBuilder build() {
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
	 * @return the field
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
