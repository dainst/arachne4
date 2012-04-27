package de.uni_koeln.arachne.mapping;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonValue;

@Entity
@Table(name="verwaltung_datensatzgruppen")
public class DatasetGroup {
	
	public DatasetGroup() {
		// needed for spring autowiring
	}
	
	public DatasetGroup(final String name) {
		this.name = name;
	}
	
	@Id
	@Column(name="dgid")
	private int dgid; // NOPMD
	
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

	/**
	 * Function to determine if a name of a dataset group is in a set of dataset groups.
	 * @return <code>True</code> if the name is contained within the set <code>False</code> if it is not.
	 */
	@XmlTransient
	@JsonIgnore
	public boolean isInDatasetGroups(final Set<DatasetGroup> datasetGroups) {
		for (DatasetGroup datasetGroup: datasetGroups) {
			if (this.getName().equals(datasetGroup.getName())) {
				return true;
			}
		}
		return false;
	}
}
