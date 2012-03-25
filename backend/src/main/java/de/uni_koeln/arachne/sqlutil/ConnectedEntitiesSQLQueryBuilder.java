package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.mapping.UserAdministration;

public class ConnectedEntitiesSQLQueryBuilder extends AbstractSQLBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(ConnectedEntitiesSQLQueryBuilder.class);
	
	protected SQLRightsConditionBuilder rcb;
	
	private String field2;
	
	/**
	 * Constructs a condition to query a field.
	 * @param tableName The name of the table of the query.
	 * @param field1 The field for which the id is given.
	 * @param field1Id The field Id.
	 * @param field2 The field to query.
	 */
	public ConnectedEntitiesSQLQueryBuilder(String contextType, Long entityId, UserAdministration user) {
		sql = "";
		conditions = new ArrayList<Condition>(1);
		table = contextType;
		this.field2 = SQLToolbox.getQualifiedFieldname(table, field2);
		rcb = new SQLRightsConditionBuilder(table,user);
		// The key identification condition
		Condition keyCondition = new Condition();
		keyCondition.setOperator("=");
		/*if (field1.equals(tableName)) {
			keyCondition.setPart1(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(field1)));
		} else {
			keyCondition.setPart1(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generateForeignKeyName(field1)));
		}*/
		keyCondition.setPart1("Source");
		keyCondition.setPart2(entityId.toString());
		conditions.add(keyCondition);
		// category selection condition
		Condition categoryCondition = new Condition();
		categoryCondition.setOperator("=");
		categoryCondition.setPart1("TypeTarget");
		categoryCondition.setPart2("\""+contextType+"\"");
		conditions.add(categoryCondition);
		// The field2 not null condition
		Condition notNullCondition = new Condition();
		notNullCondition.setOperator("IS NOT");
		notNullCondition.setPart1(SQLToolbox.getQualifiedFieldname(table, field2));
		notNullCondition.setPart2("NULL");
		//conditions.add(notNullCondition);
	}
	
	@Override
	protected String buildSQL() {
		sql += "SELECT * FROM `ArachneSemanticConnection` LEFT JOIN `" + table + "` ON " 
				+ SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(table)) + " = "
				+ "`ArachneSemanticConnection`.`ForeignKeyTarget` WHERE 1";
		sql += this.buildAndConditions();
		sql += rcb.getUserRightsSQLSnipplett();  
		sql += ";";
		logger.debug(sql);
		return sql;
	}
}
