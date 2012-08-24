package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.mapping.UserAdministration;


public class ConnectedEntityIdsSQLQueryBuilder extends AbstractSQLBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectedEntitiesSQLQueryBuilder.class);
	
	transient protected SQLRightsConditionBuilder rightsConditionBuilder;
	
	/**
	 * Constructs a condition to query a field.
	 * @param entityId The entityId of the object of interest.
	 */
	public ConnectedEntityIdsSQLQueryBuilder(final Long entityId, final UserAdministration user) {
		super();
		conditions = new ArrayList<Condition>(1);
		//rightsConditionBuilder = new SQLRightsConditionBuilder(table, user);
		// The key identification condition
		final Condition keyCondition = new Condition();
		keyCondition.setOperator("=");
		keyCondition.setPart1("Source");
		keyCondition.setPart2(entityId.toString());
		conditions.add(keyCondition);
	}
	
	@Override
	protected String buildSQL() {
		sql += "SELECT `Target` FROM `SemanticConnection` WHERE 1";
		sql += this.buildAndConditions();
		//sql += rightsConditionBuilder.getUserRightsSQLSnipplett();
		sql += ";";
		LOGGER.debug(sql);
		return sql;
	}
}
