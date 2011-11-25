package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;
import java.util.List;

public class GenericFieldsSQLQueryBuilder extends AbstractArachneSQLBuilder {
	protected ArachneSQLRightsConditionBuilder rcb;
	
	private String field2;
	
	/**
	 * Constructs a condition to query a field.
	 * @param tableName The name of the table of the query.
	 * @param field1 The field for which the id is given.
	 * @param field1Id The field Id.
	 * @param fields The fields to query.
	 */
	public GenericFieldsSQLQueryBuilder(String tableName, String field1, Long field1Id, List<String> fields) {
		sql = "";
		conditions = new ArrayList<Condition>(1);
		table = tableName;
		rcb = new ArachneSQLRightsConditionBuilder(table);
		// concatenate fields
		field2 = ArachneSQLToolbox.getQualifiedFieldname(table, fields.get(0));
		int i = 1;
		while (i<fields.size()) {
			field2 += ", " + ArachneSQLToolbox.getQualifiedFieldname(table,fields.get(i));
			i++;
		}
		System.out.println("field2: " + field2);
		// The key identification condition
		Condition keyCondition = new Condition();
		keyCondition.setOperator("=");
		keyCondition.setPart1(ArachneSQLToolbox.getQualifiedFieldname(table, ArachneSQLToolbox.generateForeignKeyName(field1)));
		keyCondition.setPart2(field1Id.toString());
		conditions.add(keyCondition);
	}
	
	@Override
	protected String buildSQL() {
		sql += "SELECT " + field2 + " FROM `" + table + "` WHERE 1";
		sql += this.buildAndConditions();
		sql += rcb.getUserRightsSQLSnipplett();  
		sql += ";";
		// TODO remove debug output
		System.out.println("GenericFieldsQueryBuilder SQL: " + sql);
		return sql;
	}
}
