package de.uni_koeln.arachne.sqlutil;

/**
 * 
 * Query-Builder constructs a very basic query to receive a dataset from a given table only using its primary key and table name
 * @author Patrick Gunia
 *
 */

public class SimpleTableEntityQueryBuilder extends AbstractSQLBuilder {
	
	private transient final Integer primaryKey;
	
	private transient final String primaryKeyLabel;
	
	public SimpleTableEntityQueryBuilder(final String tableName, final Integer primaryKey) {
		this.table = tableName; 
		this.primaryKeyLabel = "PS_" + tableName.substring(0,1).toUpperCase() + tableName.substring(1) + "ID";
		this.primaryKey = primaryKey;
	}
	
	@Override
	protected void buildSQL() {
		final StringBuffer buffer = new StringBuffer("SELECT * FROM `" + this.table + "` WHERE `" + primaryKeyLabel + "` = '" + primaryKey + "'");
		sql = buffer.toString();
	}
 
}
