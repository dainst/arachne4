package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Class to hold place information for entities.
 * @author Reimar Grabowski
 */
@JsonInclude(Include.NON_NULL)
public class Place {

	/**
	 * The name of the place
	 */
	private String name;
	
	/**
	 * The relation of this place to its 'parent' entity.
	 */
	private String relation = null;
	
	/**
	 * Geo-coordinates as <code>String</code> array.
	 */
	private String[] location;
	
	public Place(final String name) {
		this.name = name;
		location = new String[2];
	}
	
	public Place(final String name, final String relation) {
		this.name = name;
		location = new String[2];
		this.relation = relation;
	}
	
	public Place(final String name, final String latitude, final String longitude, final String relation) {
		this.name = name;
		location = new String[2];
		location[0] = latitude;
		location[1] = longitude;
		this.relation = relation;
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

	/**
	 * @return the location
	 */
	public String[] getLocation() {
		return location;
	}

	/**
	 * @param latitude the latitude of this place
	 */
	public void setLocation(final String latitude, final String longitude) {
		this.location[0] = latitude;
		this.location[1] = longitude;
	}
}
