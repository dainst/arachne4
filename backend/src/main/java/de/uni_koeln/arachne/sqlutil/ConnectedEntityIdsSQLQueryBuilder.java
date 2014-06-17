package de.uni_koeln.arachne.sqlutil;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import de.uni_koeln.arachne.service.IUserRightsService;

@Configurable(preConstruction=true)
public class ConnectedEntityIdsSQLQueryBuilder extends AbstractSQLBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectedEntitiesSQLQueryBuilder.class);
	
	@Autowired
	transient protected IUserRightsService userRightsService;
	
	/**
	 * Constructs a condition to query a field.
	 * @param entityId The entityId of the object of interest.
	 */
	// TODO IMPORTANT - check if rights management must be enabled
	public ConnectedEntityIdsSQLQueryBuilder(final Long entityId) {
		super();
		conditions = new ArrayList<Condition>(1);
		// The key identification condition
		final Condition keyCondition = new Condition();
		keyCondition.setOperator("=");
		keyCondition.setPart1("Source");
		keyCondition.setPart2(entityId.toString());
		conditions.add(keyCondition);
	}
	
	@Override
	protected void buildSQL() {
		final StringBuilder result = new StringBuilder(128).append(sql);
		result.append("SELECT `Target` FROM `SemanticConnection` WHERE NOT `Target` = 0 AND NOT `TypeTarget` = \"marbilder\"");
		result.append(this.buildAndConditions());
		result.append(';');
		sql = result.toString();
		LOGGER.debug(sql);
	}
}
