package de.uni_koeln.arachne.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import de.uni_koeln.arachne.util.StrUtils;

/**
 * Class to hold place information for entities.
 * @author Reimar Grabowski
 */
@JsonInclude(Include.NON_EMPTY)
public class Place {

	@JsonInclude(Include.NON_EMPTY)
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

	/**
	 * The beginning of the time period of the relation to its parent entity.
	 */
	private int storageFromDay = 0;
	private int storageFromMonth = 0;
	private int storageFromYear = 0;

	/**
	 * The end of the time period of the relation to its parent entity.
	 */
	private int storageToDay = 0;
	private int storageToMonth = 0;
	private int storageToYear = 0;

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
	 * @param longitude the longitude of this place
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

	/**
	 * @param start day of the relation to parent
	 */
	public int getStorageFromDay() {
	    return storageFromDay;
	}
	/**
	 * @param start month of the relation to parent
	 */
	public int getStorageFromMonth() {
	    return storageFromMonth;
	}
	/**
	 * @param start year of the relation to parent
	 */
	public int getStorageFromYear() {
		return storageFromYear;
	}

	/**
	 * @param start day of the relation to parent to set
	 */
	public void setStorageFromDay(int storageFromDay) {
		this.storageFromDay = storageFromDay;
	}
	/**
	 * @param start month of the relation to parent to set
	 */
	public void setStorageFromMonth(int storageFromMonth) {
		this.storageFromMonth = storageFromMonth;
	}
	/**
	 * @param start year of the relation to parent to set
	 */
	public void setStorageFromYear(int storageFromYear) {
	    System.out.println("Set storage from year");
		this.storageFromYear = storageFromYear;
	}

	/**
	 * @param end day of the relation to parent
	 */
	public int getStorageToDay() {
	    return storageToDay;
	}
	/**
	 * @param end month of the relation to parent
	 */
	public int getStorageToMonth() {
	    return storageToMonth;
	}
	/**
	 * @param end year of the relation to parent
	 */
	public int getStorageToYear() {
		return storageToYear;
	}

	/**
	 * @param end day of the relation to parent to set
	 */
	public void setStorageToDay(int storageToDay) {
		this.storageToDay = storageToDay;
	}
	/**
	 * @param end month of the relation to parent to set
	 */
	public void setStorageToMonth(int storageToMonth) {
		this.storageToMonth = storageToMonth;
	}
	/**
	 * @param end year of the relation to parent to set
	 */
	public void setStorageToYear(int storageToYear) {
		this.storageToYear = storageToYear;
	}

}
