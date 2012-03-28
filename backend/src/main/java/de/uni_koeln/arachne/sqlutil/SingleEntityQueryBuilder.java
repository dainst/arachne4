package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * This class Constructs a Query for a Single Arachne-Entity.
 * @author Rasmus Krempel
 *
 */
public class SingleEntityQueryBuilder extends AbstractSQLBuilder {
	
	protected transient ArachneId id;
	
	protected transient SQLRightsConditionBuilder rcb;
	
	/**
	 * Constructs a condition to find the Dataset described in ArachneId. creates <code>UserRightsConditionBuilder</code> , Limits the Result count to 1. 
	 * @param ident This is the <code>ArachneId</code> the SQL retrieve statement should be written for
	 * @param user 
	 */
	public SingleEntityQueryBuilder(ArachneId ident, UserAdministration user) {
		sql = "";
		conditions = new ArrayList<Condition>(1);
		id = ident;
		//Sets the Tablename
		table = id.getTableName();
		//Limits the Resultcount to 1
		limit1 = true;
		rcb = new SQLRightsConditionBuilder(table,user);
		//The Primary key Identification condition
		Condition cnd = new Condition();
		cnd.setOperator("=");
		cnd.setPart1(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(table)));
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