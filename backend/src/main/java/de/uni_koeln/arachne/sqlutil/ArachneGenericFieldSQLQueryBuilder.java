package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import de.uni_koeln.arachne.util.ArachneId;

public class ArachneGenericFieldSQLQueryBuilder extends AbstractArachneSQLBuilder {

	protected ArachneSQLRightsConditionBuilder rcb;
	private String field2;
	
	/**
	 * Constructs a condition to query a field. Creates <code>UserRightsConditionBuilder</code>.
	 * @param tableName The name of the table of the query.
	 * @param field1 The field for which the id is given.
	 * @param field1Id The field Id.
	 * @param field2 The field to query.
	 */
	public ArachneGenericFieldSQLQueryBuilder(String tableName, String field1, Long field1Id, String field2) {
		sql = "";
		conditions = new ArrayList<Condition>(1);
		table = tableName;
		this.field2 = ArachneSQLToolbox.getQualifiedFieldname(table, ArachneSQLToolbox.generateForeignKeyName(field2));
		rcb = new ArachneSQLRightsConditionBuilder(table);
		// The key identification condition
		Condition keyCondition = new Condition();
		keyCondition.setOperator("=");
		keyCondition.setPart1(ArachneSQLToolbox.getQualifiedFieldname(table, ArachneSQLToolbox.generateForeignKeyName(field1)));
		keyCondition.setPart2(field1Id.toString());
		conditions.add(keyCondition);
		// The field2 not null condition
		Condition notNullCondition = new Condition();
		notNullCondition.setOperator("IS NOT");
		notNullCondition.setPart1(ArachneSQLToolbox.getQualifiedFieldname(table, ArachneSQLToolbox.generateForeignKeyName(field2)));
		notNullCondition.setPart2("NULL");
		conditions.add(notNullCondition);
	}
	
	@Override
	protected String buildSQL() {
		sql += "SELECT " + field2 + " FROM `" + table + "` WHERE 1";
		sql += this.buildAndConditions();
		sql += rcb.getUserRightsSQLSnipplett();  
		sql += ";";
		System.out.println("GenericFieldQueryBuilder SQL: " + sql);
		return sql;
	}
}
