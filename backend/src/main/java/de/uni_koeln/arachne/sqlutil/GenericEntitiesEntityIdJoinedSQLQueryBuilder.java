package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.mapping.UserAdministration;

public class GenericEntitiesEntityIdJoinedSQLQueryBuilder extends AbstractSQLBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericEntitiesEntityIdJoinedSQLQueryBuilder.class);
	
	protected transient SQLRightsConditionBuilder rcb;
	private transient String entityIdLeftJoin;
		
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
		String categoryTable = table;
		if (categoryTable.contains("_leftjoin_")) {
			categoryTable = categoryTable.substring(categoryTable.indexOf("_leftjoin_") + 10);  
		}
		
		entityIdLeftJoin = "LEFT JOIN `arachneentityidentification` ON (`arachneentityidentification`.`TableName` = \"" 
				+ categoryTable + "\" AND `arachneentityidentification`.`ForeignKey` = " 
				+ SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(categoryTable)) + ") ";
		
		// The key identification condition
		final Condition keyCondition = new Condition();
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
		LOGGER.debug(sql);
		return sql;
	}
}
