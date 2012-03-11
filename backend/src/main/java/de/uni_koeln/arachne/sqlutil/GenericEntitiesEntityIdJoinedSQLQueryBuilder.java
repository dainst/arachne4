package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import org.apache.log4j.Category;

import de.uni_koeln.arachne.mapping.UserAdministration;

public class GenericEntitiesEntityIdJoinedSQLQueryBuilder extends AbstractSQLBuilder {
	
	protected SQLRightsConditionBuilder rcb;
	private String entityIdLeftJoin;
		
	/**
	 * Constructs a condition to query a table.
	 * @param tableName The name of the table of the query.
	 * @param field1 The field for which the id is given.
	 * @param field1Id The field Id.
	 */
	public GenericEntitiesEntityIdJoinedSQLQueryBuilder(String tableName, String field1, Long field1Id, UserAdministration user) {
		sql = "";
		conditions = new ArrayList<Condition>(1);
		table = tableName;
		rcb = new SQLRightsConditionBuilder(table, user);
		
		// add ArachneEntityId to result
		System.out.println("field1: " + field1);
			
		String categoryTable = table;
		if (categoryTable.contains("_leftjoin_")) {
			categoryTable = categoryTable.substring(categoryTable.indexOf("_leftjoin_") + 10);  
		}
		
		entityIdLeftJoin = "LEFT JOIN `arachneentityidentification` ON (`arachneentityidentification`.`TableName` = \"" 
				+ categoryTable + "\" AND `arachneentityidentification`.`ForeignKey` = " 
				+ SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(categoryTable)) + ") ";
		
		// The key identification condition
		Condition keyCondition = new Condition();
		keyCondition.setOperator("=");
		keyCondition.setPart1(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generateForeignKeyName(field1)));
		keyCondition.setPart2(field1Id.toString());
		conditions.add(keyCondition);
	}
	
	@Override
	protected String buildSQL() {
		sql += "SELECT * FROM `" + table +  "` "+ entityIdLeftJoin + " WHERE 1";
		sql += this.buildAndConditions();
		sql += rcb.getUserRightsSQLSnipplett();  
		sql += ";";
		// TODO remove debug output
		System.out.println("GenericEntitiesEntityIdJoinedQueryBuilder SQL: " + sql);
		return sql;
	}
}
