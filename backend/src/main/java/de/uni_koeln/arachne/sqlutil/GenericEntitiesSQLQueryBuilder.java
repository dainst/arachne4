package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import de.uni_koeln.arachne.mapping.UserAdministration;

public class GenericEntitiesSQLQueryBuilder extends AbstractSQLBuilder {

	protected SQLRightsConditionBuilder rcb;
	
	/**
	 * Constructs a condition to query a table.
	 * @param tableName The name of the table of the query.
	 * @param field1 The field for which the id is given.
	 * @param field1Id The field Id.
	 */
	public GenericEntitiesSQLQueryBuilder(String tableName, String field1, Long field1Id, UserAdministration user) {
		sql = "";
		conditions = new ArrayList<Condition>(1);
		table = tableName;
		rcb = new SQLRightsConditionBuilder(table, user);
		// The key identification condition
		Condition keyCondition = new Condition();
		keyCondition.setOperator("=");
		keyCondition.setPart1(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generateForeignKeyName(field1)));
		keyCondition.setPart2(field1Id.toString());
		conditions.add(keyCondition);
	}
	
	@Override
	protected String buildSQL() {
		sql += "SELECT * FROM `" + table + "` WHERE 1";
		sql += this.buildAndConditions();
		sql += rcb.getUserRightsSQLSnipplett();  
		sql += ";";
		// TODO remove debug output
		System.out.println("GenericEntitiesQueryBuilder SQL: " + sql);
		return sql;
	}
}
