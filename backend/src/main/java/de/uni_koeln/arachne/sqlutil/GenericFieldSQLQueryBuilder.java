package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericFieldSQLQueryBuilder extends AbstractSQLBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericFieldSQLQueryBuilder.class);
	
	protected transient SQLRightsConditionBuilder rightsConditionBuilder;
	
	private transient final String field2;
	
	private transient final String rightsCondition;
	
	/**
	 * Constructs a condition to query a field.
	 * @param tableName The name of the table of the query.
	 * @param field1 The field for which the id is given.
	 * @param field1Id The field Id.
	 * @param field2 The field to query.
	 */
	public GenericFieldSQLQueryBuilder(final String tableName, final String field1, final Long field1Id, final String field2
			, final boolean disableAuthorization) {
		
		conditions = new ArrayList<Condition>(1);
		table = tableName;
		this.field2 = SQLToolbox.getQualifiedFieldname(table, field2);
		
		if (disableAuthorization) {
			rightsCondition = "";
		} else {
			rightsCondition =  new SQLRightsConditionBuilder(table).getUserRightsSQLSnipplett();
		}
		
		// The key identification condition
		final Condition keyCondition = new Condition();
		keyCondition.setOperator("=");
		if ("ArachneEntityID".equals(field1)) {
			keyCondition.setPart1(field1);
		} else if (field1.equals(tableName)) {
			keyCondition.setPart1(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(field1)));
		} else {
			keyCondition.setPart1(SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generateForeignKeyName(field1)));
		}
		keyCondition.setPart2("\"" + field1Id.toString() + "\"");
		conditions.add(keyCondition);
		// The field2 not null condition
		final Condition notNullCondition = new Condition();
		notNullCondition.setOperator("IS NOT");
		notNullCondition.setPart1(SQLToolbox.getQualifiedFieldname(table, field2));
		notNullCondition.setPart2("NULL");
		conditions.add(notNullCondition);
	}
	
	@Override
	protected void buildSQL() {
		final StringBuilder result = new StringBuilder(64).append(sql);
		result.append("SELECT ");
		result.append(field2);
		result.append(" FROM `");
		result.append(table);
		result.append("` WHERE 1");
		result.append(this.buildAndConditions());
		result.append(rightsCondition);
		result.append(';');
		LOGGER.debug(result.toString());
		sql = result.toString();
	}
}
