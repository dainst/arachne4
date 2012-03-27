package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.mapping.UserAdministration;

public class GenericEntitiesSQLQueryBuilder extends AbstractSQLBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericEntitiesSQLQueryBuilder.class);
	
	protected SQLRightsConditionBuilder rightsConditionBuilder;
	
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
		rightsConditionBuilder = new SQLRightsConditionBuilder(table, user);
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
		sql += rightsConditionBuilder.getUserRightsSQLSnipplett();  
		sql += ";";
		LOGGER.debug(sql);
		return sql;
	}
}
