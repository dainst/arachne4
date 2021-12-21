package de.uni_koeln.arachne.mapping.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ORM class for the connection table (<code>Verknuepfungen</code>).
 * POJO for hibernate mapping.
 */
@Entity
@Table(name="Verknuepfungen")
public class Connection {
	@Id
	@Column(name="PS_VerknuepfungenID")
	private Long internalId = null; 
	
	@Column(name="Teil1")
	private String part1 = null; 

	@Column(name="Teil2")
	private String part2 = null; 
	
	@Column(name="Tabelle")
	private String table = null; 
	
	@Column(name="Felder")
	private String fields = null; 
	
	@Column(name="Type")
	private String type = null; 

	/**
	 * Getter for the id.
	 * @return The id.
	 */
	public Long getId() {
		return internalId;
	}

	/**
	 * Getter for part1 ('Verknuepfungen.Teil1').
	 * @return Part1.
	 */
	public String getPart1() {
		return part1;
	}

	/**
	 * Getter for part2 ('Verknuepfungen.Teil2').
	 * @return Part2.
	 */
	public String getPart2() {
		return part2;
	}

	/**
	 * Getter for table.
	 * @return The DB table.
	 */
	public String getTable() {
		return table;
	}
	
	/**
	 * Getter for the fields.
	 * @return The fields.
	 */
	public String getFields() {
		return fields;
	}

	/**
	 * Getter for the type.
	 * @return The type.
	 */
	public String getType() {
		return type;
	}
}
