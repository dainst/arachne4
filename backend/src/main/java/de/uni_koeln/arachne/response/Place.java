package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import de.uni_koeln.arachne.util.StrUtils;

/**
 * Class to hold place information for entities.
 * @author Reimar Grabowski
 */
@JsonInclude(Include.NON_NULL)
public class Place {

	@JsonInclude(Include.NON_NULL)
	private class Location {
		public String lat;
		public String lon;
	}
	
	/**
	 * The name of the place
	 */
	private String name;
	
	/**
	 * The relation of this place to its 'parent' entity.
	 */
	private String relation = null;
	
	/**
	 * Geo-coordinates as <code>Location</code> object.
	 */
	private Location location = null;
	
	/**
	 * The Gazetteer-ID of the place.
	 */
	private Long gazetteerId = null;
	
	public Place(final String name) {
		this.name = name;
	}
	
	public Place(final String name, final String relation) {
		this.name = name;
		this.relation = relation;
	}
	
	public Place(final String name, final String latitude, final String longitude, final String relation) {
		this.name = name;
		location = new Location();
		location.lat = latitude;
		location.lon = longitude;
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
	 * @return the location as string or <code>null</code> if the location is empty.
	 */
	@JsonIgnore
	public String getLocationAsString() {
		if (location != null) {
			return '[' + location.lat + ',' + location.lon + ']';
		} else {
			return null;
		}
	}

	/**
	 * @return the location as string
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * @param latitude the latitude of this place
	 */
	public void setLocation(final String latitude, final String longitude) {
		if (!StrUtils.isEmptyOrNull(latitude) && !StrUtils.isEmptyOrNull(longitude)) {
			location = new Location();
			location.lat = latitude;
			location.lon = longitude;
		}
	}

	/**
	 * @return the gazetteerId
	 */
	public Long getGazetteerId() {
		return gazetteerId;
	}

	/**
	 * @param gazetteerId the gazetteerId to set
	 */
	public void setGazetteerId(Long gazetteerId) {
		this.gazetteerId = gazetteerId;
	}
}
