package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.mapping.UserAdministration;

public class ConnectedEntitiesSQLQueryBuilder extends AbstractSQLBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectedEntitiesSQLQueryBuilder.class);
	
	transient protected SQLRightsConditionBuilder rightsConditionBuilder;
	
	/**
	 * Constructs a condition to query a field.
	 * @param tableName The name of the table of the query.
	 * @param field1 The field for which the id is given.
	 * @param field1Id The field Id.
	 * @param field2 The field to query.
	 */
	public ConnectedEntitiesSQLQueryBuilder(final String contextType, final Long entityId, final UserAdministration user) {
		super();
		conditions = new ArrayList<Condition>(1);
		table = contextType;
		rightsConditionBuilder = new SQLRightsConditionBuilder(table,user);
		// The key identification condition
		final Condition keyCondition = new Condition();
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
		final Condition categoryCondition = new Condition();
		categoryCondition.setOperator("=");
		categoryCondition.setPart1("TypeTarget");
		categoryCondition.setPart2("\""+contextType+"\"");
		conditions.add(categoryCondition);
	}
	
	@Override
	protected String buildSQL() {
		sql += "SELECT * FROM `ArachneSemanticConnection` LEFT JOIN `" + table + "` ON " 
				+ SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(table)) + " = "
				+ "`ArachneSemanticConnection`.`ForeignKeyTarget` WHERE 1";
		sql += this.buildAndConditions();
		sql += rightsConditionBuilder.getUserRightsSQLSnipplett();  
		sql += ";";
		LOGGER.debug(sql);
		return sql;
	}
}
