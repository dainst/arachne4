package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.mapping.UserAdministration;

public class GenericFieldsEntityIdJoinedSQLQueryBuilder extends AbstractSQLBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericFieldsEntityIdJoinedSQLQueryBuilder.class);
	
	protected transient SQLRightsConditionBuilder rightsConditionBuilder;
	
	private transient String field2;
	
	private transient final String entityIdLeftJoin;
	
	/**
	 * Constructs a condition to query multiple fields. The <code>ArachneEntityId</code> is automatically retrieved by
	 * left joining the <code>arachneentityidentification</code> table.
	 * @param tableName The name of the table of the query.
	 * @param field1 The field for which the id is given.
	 * @param field1Id The field Id.
	 * @param fields The fields to query.
	 */
	public GenericFieldsEntityIdJoinedSQLQueryBuilder(final String tableName, final String field1, final Long field1Id, final List<String> fields
			, final UserAdministration user) {
		sql = "";
		conditions = new ArrayList<Condition>(1);
		table = tableName;
		rightsConditionBuilder = new SQLRightsConditionBuilder(table, user);
		// concatenate fields
		field2 = SQLToolbox.getQualifiedFieldname(table, fields.get(0));
		int index = 1;
		while (index<fields.size()) {
			field2 += ", " + SQLToolbox.getQualifiedFieldname(table,fields.get(index));
			index++;
		}
		// add ArachneEntityId to result
		field2 += ", " + SQLToolbox.getQualifiedFieldname("arachneentityidentification", "ArachneEntityID");
				
		entityIdLeftJoin = "LEFT JOIN `arachneentityidentification` ON (`arachneentityidentification`.`TableName` = \"" 
				+ table + "\" AND `arachneentityidentification`.`ForeignKey` = " 
				+ SQLToolbox.getQualifiedFieldname(table, SQLToolbox.generatePrimaryKeyName(table)) + ") ";
		
		// The key identification condition
		final Condition keyCondition = new Condition();
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
		sql += "SELECT " + field2 + " FROM `" + table +  "` "+ entityIdLeftJoin + " WHERE 1";
		sql += this.buildAndConditions();
		sql += rightsConditionBuilder.getUserRightsSQLSnipplett();  
		sql += ";";
		LOGGER.debug(sql);
		return sql;
	}
}
