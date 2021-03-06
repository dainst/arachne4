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
	private Integer storageFromDay = null;
	private Integer storageFromMonth = null;
	private Integer storageFromYear = null; //Needs to be null because empty date values have to be different from year 0

	/**
	 * The end of the time period of the relation to its parent entity.
	 */
	private Integer storageToDay = null;
	private Integer storageToMonth = null;
	private Integer storageToYear = null; //Needs to be null because empty date values have to be different from year 0

	private String country;
	private String city;
	private String region;
	private String subregion;
	private String locality;
	
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
	 * @return start day of the relation to parent 
	 */
	public Integer getStorageFromDay() {
	    return storageFromDay;
	}
	/**
	 * @return start month of the relation to parent
	 */
	public Integer getStorageFromMonth() {
	    return storageFromMonth;
	}
	/**
	 * @return start year of the relation to parent
	 */
	public Integer getStorageFromYear() {
		return storageFromYear;
	}

	public String getCountry() {
	    return country;
	}

	public String getRegion() {
	    return region;
	}

	public String getSubregion() {
	    return subregion;
	}

	public String getLocality() {
	    return locality;
	}

	public String getCity() {
	    return city;
	}

	/**
	 * @param storageFromDay day of the relation to parent to set
	 */
	public void setStorageFromDay(int storageFromDay) {
		this.storageFromDay = storageFromDay;
	}
	/**
	 * @param storageFromMonth month of the relation to parent to set 
	 */
	public void setStorageFromMonth(int storageFromMonth) {
		this.storageFromMonth = storageFromMonth;
	}
	/**
	 * @param storageFromYear year of the relation to parent to set 
	 */
	public void setStorageFromYear(int storageFromYear) {
		this.storageFromYear = storageFromYear;
	}

	/**
	 * @return day of the relation to parent
	 */
	public Integer getStorageToDay() {
	    return storageToDay;
	}
	/**
	 * @return month of the relation to parent
	 */
	public Integer getStorageToMonth() {
	    return storageToMonth;
	}
	/**
	 * @return year of the relation to parent 
	 */
	public Integer getStorageToYear() {
		return storageToYear;
	}

	/**
	 * @param storageToDay day of the relation to parent to set
	 */
	public void setStorageToDay(int storageToDay) {
		this.storageToDay = storageToDay;
	}
	/**
	 * @param storageToMonth month of the relation to parent to set
	 */
	public void setStorageToMonth(int storageToMonth) {
		this.storageToMonth = storageToMonth;
	}
	/**
	 * @param storageToYear year of the relation to parent to set
	 */
	public void setStorageToYear(int storageToYear) {
		this.storageToYear = storageToYear;
	}

	public void setCountry(String country) {
	    this.country = country;
	}

	public void setCity(String city) {
	    this.city = city;
	}

	public void setRegion(String region) {
	    this.region = region;
	}

	public void setSubregion(String subregion) {
	    this.subregion = subregion;
	}

	public void setLocality(String locality) {
	    this.locality = locality;
	}
}
