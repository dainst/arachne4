package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectedEntitiesSQLQueryBuilder extends AbstractSQLBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectedEntitiesSQLQueryBuilder.class);
	
	transient protected SQLRightsConditionBuilder rightsConditionBuilder;
	
	/**
	 * Constructs a condition to query a field.
	 * @param contextType The type of the context.
	 * @param entityId The id of the entity the context belongs to.
	 * @param user The user information used for authorizing.
	 */
	public ConnectedEntitiesSQLQueryBuilder(final String contextType, final Long entityId) {
		super();
		conditions = new ArrayList<Condition>(1);
		table = contextType;
		rightsConditionBuilder = new SQLRightsConditionBuilder(table);
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
		final StringBuilder result = new StringBuilder(sql);
		result.append("SELECT * FROM `SemanticConnection` LEFT JOIN `");
		result.append(table);
		result.append("` ON ");
		result.append(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(table)));
		result.append(" = `SemanticConnection`.`ForeignKeyTarget` WHERE 1");
		result.append(this.buildAndConditions());
		result.append(rightsConditionBuilder.getUserRightsSQLSnipplett());  
		result.append(';');
		sql = result.toString();
		LOGGER.debug(sql);
		return sql;
	}
}
