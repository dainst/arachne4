package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.mapping.UserAdministration;

public class GenericFieldsSQLQueryBuilder extends AbstractSQLBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(GenericFieldsSQLQueryBuilder.class);
	
	protected SQLRightsConditionBuilder rcb;
	
	private String field2;
	
	/**
	 * Constructs a condition to query a field.
	 * @param tableName The name of the table of the query.
	 * @param field1 The field for which the id is given.
	 * @param field1Id The field Id.
	 * @param fields The fields to query.
	 */
	public GenericFieldsSQLQueryBuilder(String tableName, String field1, Long field1Id, List<String> fields, UserAdministration user) {
		sql = "";
		conditions = new ArrayList<Condition>(1);
		table = tableName;
		rcb = new SQLRightsConditionBuilder(table, user);
		// concatenate fields
		field2 = SQLToolbox.getQualifiedFieldname(table, fields.get(0));
		int i = 1;
		while (i<fields.size()) {
			field2 += ", " + SQLToolbox.getQualifiedFieldname(table,fields.get(i));
			i++;
		}
		System.out.println("field2: " + field2);
		// The key identification condition
		Condition keyCondition = new Condition();
		keyCondition.setOperator("=");
		if (field1.equals(tableName)) {
			keyCondition.setPart1(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(field1)));
		} else {
			keyCondition.setPart1(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generateForeignKeyName(field1)));
		}
		keyCondition.setPart2(field1Id.toString());
		conditions.add(keyCondition);
	}
	
	@Override
	protected String buildSQL() {
		sql += "SELECT " + field2 + " FROM `" + table + "` WHERE 1";
		sql += this.buildAndConditions();
		sql += rcb.getUserRightsSQLSnipplett();  
		sql += ";";
		logger.debug(sql);
		return sql;
	}
}
