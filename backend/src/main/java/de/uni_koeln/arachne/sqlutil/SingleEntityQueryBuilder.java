package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import de.uni_koeln.arachne.util.EntityId;

/**
 * This class Constructs a Query for a Single Arachne-Entity.
 * @author Rasmus Krempel
 *
 */
public class SingleEntityQueryBuilder extends AbstractSQLBuilder {
	
	protected transient EntityId entityId;
	
	protected transient SQLRightsConditionBuilder rightsConditionBuilder;
	
	/**
	 * Constructs a condition to find the Dataset described in ArachneId. creates <code>UserRightsConditionBuilder</code> , Limits the Result count to 1. 
	 * @param ident This is the <code>ArachneId</code> the SQL retrieve statement should be written for
	 * @param user 
	 */
	public SingleEntityQueryBuilder(final EntityId ident) {
		conditions = new ArrayList<Condition>(1);
		entityId = ident;
		//Sets the Tablename
		table = entityId.getTableName();
		//Limits the Resultcount to 1
		limit1 = true;
		rightsConditionBuilder = new SQLRightsConditionBuilder(table);
		//The Primary key Identification condition
		final Condition condition = new Condition();
		condition.setOperator("=");
		condition.setPart1(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(table)));
		condition.setPart2(entityId.getInternalKey().toString());
		conditions.add(condition);
	}
	
	@Override
	protected void buildSQL() {
		final StringBuilder result = new StringBuilder(sql);
		result.append("SELECT * FROM `");
		result.append(table);
		result.append("` WHERE 1");
		result.append(this.buildAndConditions());
		result.append(rightsConditionBuilder.getUserRightsSQLSnipplett());  
		result.append(this.appendLimitOne());
		result.append(';');
		sql = result.toString();
	}
}