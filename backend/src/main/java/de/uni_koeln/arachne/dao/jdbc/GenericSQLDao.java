package de.uni_koeln.arachne.dao.jdbc;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.context.ContextPath;
import de.uni_koeln.arachne.mapping.jdbc.GenericEntitiesMapper;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.service.DataIntegrityLogService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.sql.ConnectedPathEntitiesSQLQueryBuilder;
import de.uni_koeln.arachne.util.sql.SQLToolbox;

/**
 * Class to retrieve data via SQL.
 */
@Repository("GenericSQLDao")
public class GenericSQLDao extends SQLDao {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericSQLDao.class);
	
	@Autowired
	private transient DataIntegrityLogService dataIntegrityLogService;
	
	private transient UserRightsService userRightsService;
	
	// 
	/**
	 * Setter for the UserRightsService. Needed to inject mock for testing - it should work without this setter 
	 * but it does not (bug in Spring)
	 * @param userRightsService The UserRightsService.
	 */
	@Autowired
	public void setUserRightsService(final UserRightsService userRightsService) {
		this.userRightsService = userRightsService;
	}
	
	/**
	 * Read any string field from the DB.
	 * @param tableName The table name.
	 * @param key The id field.
	 * @param id The id.
	 * @param field The field to read.
	 * @param disableAuthorization if user access rights should be taken into account.
	 * @return The value of the field.
	 */
	public String getStringField(final String tableName, String key, final long id, String field
			, final boolean disableAuthorization) {
		if (key.equals(tableName)) {
			key = SQLToolbox.getQualifiedFieldname(tableName, SQLToolbox.generatePrimaryKeyName(key));
		} else {
			key = SQLToolbox.getQualifiedFieldname(tableName, key);
		}
		field = SQLToolbox.getQualifiedFieldname(tableName, field);
		
		String sql = new StringBuilder(256)
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
			.append(" IS NOT NULL")
			.toString();
		if (!disableAuthorization) {
			sql += userRightsService.getSQL(tableName);
		}
		sql += " LIMIT 1;";
		
		final String result = queryForString(sql);
		if (!StrUtils.isEmptyOrNull(result)) {
			return result;
		}
		return null;
	}
	
	/**
	 * Convenience method to read a string field from the DB without taking user access rights into account.
	 * @param tableName The table name.
	 * @param field1 The id field.
	 * @param field1Id the id.
	 * @param field2 The field to read.
	 * @return The value of the field.
	 */
	public String getStringField(final String tableName, final String field1, final long field1Id
			, final String field2) {
		return getStringField(tableName, field1, field1Id, field2, false);		
	}
	
	/**
	 * Gets a list of connected entities as key value pairs.
	 * @param contextType The type of connected entity (a table name).
	 * @param entityId The entity id to get connected entities for.
	 * @return The list of connected entities.
	 */
	public List<Map<String, String>> getConnectedEntities(final String contextType, final long entityId) {
		final List<Map<String, String>> result = query(con -> {
			final String sql = "SELECT * FROM `SemanticConnection` "
					+ "LEFT JOIN `"
					+ contextType
					+ "` ON " 
					+ SQLToolbox.getQualifiedFieldname(contextType, SQLToolbox.generatePrimaryKeyName(contextType))
					+ " = `SemanticConnection`.`ForeignKeyTarget` "
					+ "WHERE Source = ? AND TypeTarget = '" + contextType + "'"
					+ userRightsService.getSQL(contextType);
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setLong(1, entityId);
			return ps;
		}, new GenericEntitiesMapper("AdditionalInfosJSON"));

		if (result != null && !result.isEmpty()) {
			return result;
		}
		return null;
	}
	
	/**
	 * Gets a list of connected entities as ids (images excluded).
	 * @param entityId The entity id to get connected entities for.
	 * @return The list of ids.
	 */
	public List<Long> getConnectedEntityIds(final long entityId) {
		final String sql = "SELECT `Target` FROM `SemanticConnection` "
				+ "WHERE NOT `Target` = 0 "
				+ "AND NOT `TypeSource` = 'marbilder' "
				+ "AND Source = " + entityId;
		@SuppressWarnings("unchecked")
		final List<Long> result = (List<Long>)queryForList(sql, Long.class);
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return null;
	}
	
	/**
	 * Gets a list of ids for 'path connected' (connected via multiple tables) entities as ids.
	 * @param entityId The entity id to get connected entities for.
	 * @param contextPath The path.
	 * @return The list of ids.
	 */
	public List<Long> getPathConnectedEntityIds(final long entityId, final ContextPath contextPath) {
		final ConnectedPathEntitiesSQLQueryBuilder sqlBuilder = new ConnectedPathEntitiesSQLQueryBuilder(contextPath, entityId);
		sqlBuilder.retriveFullDataset(false);
		final String sql= sqlBuilder.getSQL();
		LOGGER.debug(sql);
		
		@SuppressWarnings("unchecked")
		final List<Long> queryResult = (List<Long>)queryForList(sql, Long.class);
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	/**
	 * Gets a list of ids for 'path connected' (connected via multiple tables) entities as key value pairs.
	 * @param entityId The entity id to get connected entities for.
	 * @param contextPath The path.
	 * @return The list of connected entities.
	 */
	public List<Map<String, String>> getPathConnectedEntities(final long entityId, final ContextPath contextPath) {
		final ConnectedPathEntitiesSQLQueryBuilder sqlBuilder = new ConnectedPathEntitiesSQLQueryBuilder(contextPath, entityId);
		sqlBuilder.retriveFullDataset(true);
		final String sql= sqlBuilder.getSQL();
		LOGGER.debug(sql);
		final List<Map<String, String>> queryResult = (List<Map<String, String>>)query(sql
				, new GenericEntitiesMapper("AdditionalInfosJSON"));

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	/**
	 * Gets a list of connected images for the given entity from the DB. Handles user access rights.
	 * @param type The type of the entity.
	 * @param internalId The internal id of the entity.
	 * @return The list of images.
	 */
	public List<Image> getImageList(final String type
			, final long internalId) {
		List<Image> result = query(con -> {
			final String sql = "SELECT `marbilder`.`DateinameMarbilder`, `arachneentityidentification`.`ArachneEntityID` "
					+ "FROM `marbilder` "
					+ "LEFT JOIN `arachneentityidentification` "
					+ "ON (`arachneentityidentification`.`TableName` = 'marbilder' "
					+ "AND `arachneentityidentification`.`ForeignKey` = `marbilder`.`PS_MARBilderID`) "
					+ "WHERE " + SQLToolbox.getQualifiedFieldname("marbilder", SQLToolbox.generateForeignKeyName(type)) 
					+ " = ?"
					+ userRightsService.getSQL("marbilder");
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setLong(1, internalId);
			return ps;
		}, (rs, rowNUm) -> {
			final Image image = new Image();
			String fileName = rs.getString(1);
			if (fileName != null) {
				image.setImageSubtitle(fileName.substring(0, fileName.lastIndexOf('.')));
			} else {
				dataIntegrityLogService.logWarning(rs.getLong(2), "PS_MARBilderID", "Image without filename.");
			}
			image.setImageId(rs.getLong(2));
			return image;
		});
				
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return null;
	}

	/**
	 * Gets the literature for a given entity.
	 * @param tableName The table name of the entity.
	 * @param internalKey The internal key of the entity.
	 * @return The list of connected literature records.
	 */
	public List<Map<String, String>> getLiterature(final String tableName, final Long internalKey) {
		final List<Map<String, String>>	result = query(con -> {
			final String sql = "SELECT * FROM literaturzitat "
					+ "LEFT JOIN literatur ON FS_LiteraturID = PS_LiteraturID "
					+ "LEFT JOIN buch ON (ZenonID = bibid AND ZenonID <> '') "
					+ "LEFT JOIN arachneentityidentification ON (TableName = 'buch' "
					+ "AND ForeignKey = PS_BuchID) "
					+ "WHERE " + SQLToolbox.generateForeignKeyName(tableName) + " = ?;";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setLong(1, internalKey);
			return ps;
		}, new GenericEntitiesMapper("AdditionalInfosJSON"));
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return null;
	}

	/**
	 * Gets the cover page of a book record. The SQL will always return a result so that 'queryForLong' does not throw 
	 * an {@link EmptyResultDataAccessException}.
	 * @param internalKey The internal key of the book.
	 * @return The id of the cover page.
	 */
	public Long getBookCoverPage(Long internalKey) {
		final String sqlQuery = "(SELECT PS_BuchseiteID FROM buchseite WHERE seite = 0 and FS_BuchID = " 
				+ internalKey + ") UNION (SELECT null) LIMIT 1;";
		final Long cover = queryForLong(sqlQuery);
		return cover;
	}

	/**
	 * Gets a list of persons connected to a collection as key value pairs. 
	 * @param collectionId The internal key of the collection ('FS_SammlungenID').
	 * @return The list of persons.
	 */
	public List<Map<String, String>> getPersonsByCollectionId(final Long collectionId) {
        final List<Map<String, String>> result = query(con -> {
            final String sql = "SELECT * FROM personsammlung "
                    +"LEFT JOIN person ON FS_PersonID = PS_PersonID "
                    +"WHERE personsammlung.FS_SammlungenID = ? "
                    +"ORDER BY personsammlung.Sammlerreihenfolge ASC;";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, collectionId);
            return ps;
        }, new GenericEntitiesMapper("AdditionalInfosJSON"));

        if (result != null && !result.isEmpty()) {
            return result;
        }
        return null;
    }
}
