package de.uni_koeln.arachne.mapping.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

@Entity
@Table(name="verwaltung_datensatzgruppen")
@SuppressWarnings("PMD")
public class DatasetGroup {
	
	public DatasetGroup() {
		// needed for spring autowiring
	}
	
	public DatasetGroup(final String name) {
		this.name = name;
	}
	
	@Id
	@Column(name="dgid")
	private int dgid; 
	
	@Column(name="dgname")
	private String name;
	
	/**
	 * @return the id
	 */
	@XmlTransient
	@JsonIgnore
	public int getId() {
		return dgid;
	}
	
	/**
	 * @param dgid the id to set
	 */
	public void setId(final int dgid) {
		this.dgid = dgid;
	}
	/**
	 * @return the name
	 */
	
	@JsonValue
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dgid;
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
		if (!(obj instanceof DatasetGroup)) {
			return false;
		}
		DatasetGroup other = (DatasetGroup) obj;
		if (dgid != other.dgid) {
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
