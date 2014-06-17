package de.uni_koeln.arachne.sqlutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.util.EntityId;

@Repository
public class SQLFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(SQLFactory.class);		
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	public String getSingleEntityQuery(final EntityId entityId) {
		final String tableName = entityId.getTableName();
		//SELECT * FROM `topographie` WHERE 1 AND `topographie`.`PS_TopographieID` = 500000 AND
		final StringBuilder result = new StringBuilder(64)
			.append("SELECT * FROM `")
			.append(tableName)
			.append("` WHERE ")
			.append(SQLToolbox.getQualifiedFieldname(tableName, SQLToolbox.generatePrimaryKeyName(tableName)))
			.append(" = ")
			.append(entityId.getInternalKey())
			.append(userRightsService.getSQL(tableName))
			.append(" LIMIT 1;");
		return result.toString();
	}
}
