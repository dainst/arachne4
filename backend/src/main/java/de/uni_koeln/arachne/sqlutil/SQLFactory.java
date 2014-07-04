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
		final StringBuilder result = new StringBuilder(128)
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
	
	public String getFieldByIdQuery(final String tableName, final long id, String field) {
		final String key = SQLToolbox.getQualifiedFieldname(tableName, SQLToolbox.generatePrimaryKeyName(tableName));
		field = SQLToolbox.getQualifiedFieldname(tableName, field);
		final StringBuilder result = new StringBuilder(128)
			.append("SELECT ")
			.append(field)
			.append(" FROM `")
			.append(tableName)
			.append("` WHERE ")
			.append(key)
			.append(" = \"")
			.append(id)
			.append("\" AND ")
			.append(field)
			.append(" IS NOT NULL")
			.append(userRightsService.getSQL(tableName))
			.append(" LIMIT 1;");
		return result.toString();
	}

	public String getFieldQuery(final String tableName, String key, final long id, String field
			, final boolean disableAuthorization) {
		if (key.equals(tableName)) {
			key = SQLToolbox.getQualifiedFieldname(tableName, SQLToolbox.generatePrimaryKeyName(key));
		} else {
			key = SQLToolbox.getQualifiedFieldname(tableName, SQLToolbox.generateForeignKeyName(key));
		}
		field = SQLToolbox.getQualifiedFieldname(tableName, field);
		
		final StringBuilder result = new StringBuilder(128)
			.append("SELECT ")
			.append(field)
			.append(" FROM `")
			.append(tableName)
			.append("` WHERE ")
			.append(key)
			.append(" = \"")
			.append(id)
			.append("\" AND ")
			.append(field)
			.append(" IS NOT NULL");
		if (!disableAuthorization) {
			result.append(userRightsService.getSQL(tableName));
		}
		result.append(" LIMIT 1;");
		return result.toString();
	}
}