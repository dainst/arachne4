package de.uni_koeln.arachne.mapping.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;

/**
 * ORM class for the entity table (<code>arachneentityidentification</code>).
 * POJO for hibernate mapping.
 */
@Entity
@Table(name="arachneentityidentification")
public class ArachneEntity {
	
	/**
	 * Primary key of the table (<code>ArachneEntityID</code>).
	 */
	@Id
	@Column(name="ArachneEntityID")
	private Long entityId = null; 
	
	/**
	 * Table name of the table the entity is located in (<code>TableName</code>).
	 */
	@Column(name="TableName")
	String tableName = null; 
	
	/**
	 * Foreign key of the table (<code>ForeignKey</code>).
	 * This is the primary key of the table referenced in the <code>TableName</code> field.
	 */
	@Column(name="ForeignKey")
	Long foreignKey = null; 
	
	/**
	 * Field indicating if the referenced data record has been deleted (<code>isDeleted</code>).
	 * This is used to keep URLs persistent even if the corresponding data record has been deleted.  
	 */
	@Column(name="isDeleted")
	boolean isDeleted;
	
	/**
	 * The file name of the corresponding image if the entity represents an image.
	 * This allows retrieving the new Entity for the image if the old Entity has been deleted
	 * which may happen from time to time if image collections are reimported.
	 */
	@Column(name="DateinameMarbilder")
	String imageFilename;
	
	/**
	 * The returns the Unique Arachne Identifier.
	 * @return The Arachne Entity ID
	 */
	public Long getEntityId() {
		return entityId;
	}
	
	/**
	 * The Table Key of the Arachne.
	 * @return Primary key of the Internal Arachne Dataset
	 */
	public Long getForeignKey() {
		return foreignKey;
	}
	
	/**
	 * Returns the internal Database Table  
	 * @return Internal Tablename in Which the Entity Information is Stored
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Check if the Dataset is Deleted.
	 * @return true or False
	 */
	public boolean isDeleted() {
		return isDeleted;
	}
	
	/**
	 * Returns the image filename if entity is an image
	 * @return image filename
	 */
	public String getImageFilename() {
		return imageFilename;
	}
}
