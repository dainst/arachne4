package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Class to hold date information for entities.
 * @author Sebastian Cuy
 */
@JsonInclude(Include.NON_EMPTY)
public class DateAssertion {
	
	/**
	 * A human readable label for the date
	 */
	private String label;

	/**
	 * The instant in time represented by this assertion.
	 * Will be replaced by a more sophisticated type 
	 * that can represent fuzzy intervals
	 */
	private String date;
	
	/**
	 * The relation of this date to its 'parent' entity.
	 */
	private String relation = null;
	
	public DateAssertion(final String label, final String relation) {
		this.label = label;
		this.relation = relation;
	}
	
	public DateAssertion(final String label, final String relation, String date) {
		this.label = label;
		this.relation = relation;
		this.date = date;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(final String label) {
		this.label = label;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(final String date) {
		this.date = date;
	}

	/**
	 * @return the relation
	 */
	public String getRelation() {
		return relation;
	}

	/**
	 * @param relation the relation to set
	 */
	public void setRelation(final String relation) {
		this.relation = relation;
	}

}
