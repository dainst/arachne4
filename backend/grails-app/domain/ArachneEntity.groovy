

/**
 * 
 * ORM class for the entity table (<code>arachneentityidentification</code>).
 * 
 */
class ArachneEntity {
	
	/**
	 * Primary key of the table (<code>ArachneEntityID</code>).
	 */
	int id;
	
	/**
	 * Table name of the table the entity is located in (<code>TableName</code>).
	 */
	String tableName;
	
	/**
	 * Foreign key of the table (<code>ForeignKey</code>).
	 * This is the primary key of the table referenced in the <code>TableName</code> field.
	 */
	int foreignKey;
	
	/**
	 * Field indicating if the referenced data record has been deleted (<code>isDeleted</code>).
	 * This is used to keep URLs persistent even if the corresponding data record has been deleted.  
	 */
	int isDeleted;

	/**
	 * The actual object relational mapping. 
	 */
	static mapping = {
		table 'arachneentityidentification'
		id column: 'ArachneEntityID'
		foreignKey column: 'ForeignKey'
		isDeleted column: 'isDeleted'
		tableName column: 'TableName'
		version false
	}
	
	/**
	 * Not used.
	 */
    static constraints = {
    }
}