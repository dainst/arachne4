package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import de.uni_koeln.arachne.util.ArachneId;

/**
 * This class Constructs a Query for a Single Arachne-Entity.
 * @author Rasmus Krempel
 *
 */
public class ArachneSingleEntityQueryBuilder extends AbstractArachneSQLBuilder {
	
	protected ArachneId id;
	
	protected ArachneSQLRightsConditionBuilder rcb;
	
	/**
	 * Constructs a condition to find the Dataset described in ArachneId. creates <code>UserRightsConditionBuilder</code> , Limits the Result count to 1. 
	 * @param ident This is the <code>ArachneId</code> the SQL retrieve statement should be written for
	 */
	public ArachneSingleEntityQueryBuilder(ArachneId ident) {
		sql = "";
		conditions = new ArrayList<Condition>(1);
		id = ident;
		//Sets the Tablename
		table = id.getTableName();
		//Limits the Resultcount to 1
		limit1 = true;
		rcb = new ArachneSQLRightsConditionBuilder(table);
		//The Primary key Identification condition
		Condition cnd = new Condition();
		cnd.setOperator("=");
		cnd.setPart1(ArachneSQLToolbox.getQualifiedFieldname(table, ArachneSQLToolbox.generatePrimaryKeyName(table)));
		cnd.setPart2(id.getInternalKey().toString());
		conditions.add(cnd);
	}
	
	@Override
	protected String buildSQL() {
		sql += "SELECT * FROM `" + table + "` WHERE 1";
		sql += this.buildAndConditions();
		sql += rcb.getUserRightsSQLSnipplett();  
		sql += this.appendLimitOne();
		sql += ";";
		return sql;	
	}
}