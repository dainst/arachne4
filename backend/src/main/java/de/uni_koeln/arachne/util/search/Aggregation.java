package de.uni_koeln.arachne.util.search;

import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;

/**
 * Base Class for elasticsearch facets/aggregations.
 *  
 * @author Reimar Grabowski
 */
public abstract class Aggregation {

	/**
	 * The name of the aggregation.
	 */
	protected String name = "";
	
	/**
	 * The field name in the Elasticsearch index to aggregate results for.
	 */
	protected String field = "";
	
	/**
	 * The maximum number of results/buckets for this aggregation.
	 */
	protected int size = 0;
	
	/**
	 * Constructor to set all fields.
	 * @param name The name of the aggregation which must also be the field name in the elastic search index.
	 * @param field The field in the Elasticseasrch index this aggregation works on.
	 * @param size The maximum number of returned aggregation results.
	 */
	public Aggregation(final String name, final String field, final int size) {
		this.name = name;
		this.field = field;
		this.size = size;
	}

	/**
	 * Builds an aggregation that is usable by the Elasticsearch API from the current values. If <code>field</code> is 
	 * not set <code>name</code> is used instead. 
	 * @return An ES <code>AggregationBuilder</code>
	 */
	public abstract AbstractAggregationBuilder build();
	
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

	// For hashCode and equals only name, type and field are considered for equality as they identify a facet.
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		return true;
	}
}
