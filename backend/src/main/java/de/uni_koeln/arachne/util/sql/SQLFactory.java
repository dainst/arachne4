package de.uni_koeln.arachne.util.sql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.EntityId;

/**
 * Contains methods to construct SQL query strings.
 */
@Repository
public class SQLFactory {

	@Autowired
	private transient UserRightsService userRightsService;
	
	public String getSingleEntityQuery(final EntityId entityId) {
		final String tableName = entityId.getTableName();
		final StringBuilder result = new StringBuilder(256)
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
		final StringBuilder result = new StringBuilder(256)
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
			key = SQLToolbox.getQualifiedFieldname(tableName, key);
		}
		field = SQLToolbox.getQualifiedFieldname(tableName, field);
		
		final StringBuilder result = new StringBuilder(256)
			.append("SELECT ")
			.append(field)
			.append(" FROM `")
			.append(tableName)
			.append("` WHERE ")
			.append(key)
			.append(" = ")
			.append(id)
			.append(" AND ")
			.append(field)
			.append(" IS NOT NULL");
		if (!disableAuthorization) {
			result.append(userRightsService.getSQL(tableName));
		}
		result.append(" LIMIT 1;");
		return result.toString();
	}

	public String getConnectedEntitiesQuery(final String contextType, final long entityId) {
		final StringBuilder result = new StringBuilder(256)
			.append("SELECT * FROM `SemanticConnection` LEFT JOIN `")
			.append(contextType)
			.append("` ON ")
			.append(SQLToolbox.getQualifiedFieldname(contextType, SQLToolbox.generatePrimaryKeyName(contextType)))
			.append(" = `SemanticConnection`.`ForeignKeyTarget` WHERE Source = ")
			.append(entityId)
			.append(" AND TypeTarget = \"")
			.append(contextType)
			.append("\"")
			.append(userRightsService.getSQL(contextType))
			.append(';');
		return result.toString();
	}

	public String getConnectedEntityIdsQuery(final long entityId) {
		final StringBuilder result = new StringBuilder(128)
			.append("SELECT `Target` FROM `SemanticConnection` WHERE NOT `Target` = 0 AND NOT `TypeSource` = "
					+ "\"marbilder\" AND Source = ")
			.append(entityId)
			.append(';');
		return result.toString();
	}

	public String getImageListQuery(final String type, final long internalId) {
		final StringBuilder result = new StringBuilder(256)
			.append("SELECT `marbilder`.`DateinameMarbilder`, `arachneentityidentification`.`ArachneEntityID` FROM "
					+ "`marbilder` LEFT JOIN `arachneentityidentification` ON (`arachneentityidentification`.`TableName` "
					+ "= \"marbilder\" AND `arachneentityidentification`.`ForeignKey` = `marbilder`.`PS_MARBilderID`) "
					+ "WHERE ")
			.append(SQLToolbox.getQualifiedFieldname("marbilder", SQLToolbox.generateForeignKeyName(type)))
			.append(" = \"")
			.append(internalId)
			.append('"')
			.append(userRightsService.getSQL("marbilder"))
			.append(';');
		return result.toString();
	}

	public String getLiteratureQuery(String tableName, long internalKey) {
		final StringBuilder result = new StringBuilder(128)
				.append("SELECT * FROM literaturzitat "
						+ "LEFT JOIN literatur ON FS_LiteraturID = PS_LiteraturID "
						+ "LEFT JOIN buch ON (ZenonID = bibid AND ZenonID <> '') "
						+ "LEFT JOIN arachneentityidentification ON (TableName = 'buch' "
						+ "AND ForeignKey = PS_BuchID) "
						+ "WHERE ")
				.append(SQLToolbox.generateForeignKeyName(tableName))
				.append(" = ")
				.append(internalKey)
				.append(";");
		return result.toString();
	}
	
}