package de.uni_koeln.arachne.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.EntityIdMapper;
import de.uni_koeln.arachne.mapping.GenericFieldMapperLong;
import de.uni_koeln.arachne.mapping.GenericFieldMapperString;
import de.uni_koeln.arachne.mapping.GenericFieldsMapperString;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.service.SQLResponseObject;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.sqlutil.ConnectedEntityIdsSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericEntitiesEntityIdJoinedSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.ConnectedEntitiesSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericFieldSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericEntitiesSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericFieldsEntityIdJoinedSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericFieldsSQLQueryBuilder;

/**
 * Class to retrieve data via SQL.
 */
@Repository("GenericSQLDao")
public class GenericSQLDao extends SQLDao {
	
	@Autowired
	private UserRightsService userRightsService; // NOPMD
	
	/**
	 * Retrieves a list of ids from a 'cross table' field by a specified foreign key field and corresponding id or <code>null</code>.
	 * @param tableName The 'cross table' to query.
	 * @param field1 Foreign key field for which the id is given.
	 * @param field1Id The foreign key.
	 * @param field2 The key field to be queried.
	 * @return a list of foreign ids or <code>null</code>.
	 */
	public List<Long> getIdByFieldId(final String tableName, final String field1, final Long field1Id, final String field2) {
		final GenericFieldSQLQueryBuilder queryBuilder = new GenericFieldSQLQueryBuilder(tableName, field1, field1Id, field2, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		final List<Long> queryResult = (List<Long>)this.executeSelectQuery(queryBuilder.getSQL(), new GenericFieldMapperLong());

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<String> getStringField(final String tableName, final String field1, final Long field1Id
			, final String field2, final UserAdministration currentUser) {
		
		final GenericFieldSQLQueryBuilder queryBuilder = new GenericFieldSQLQueryBuilder(tableName, field1
				, field1Id, field2, currentUser);
		@SuppressWarnings("unchecked")
		final List<String> queryResult = (List<String>)this.executeSelectQuery(queryBuilder.getSQL(), new GenericFieldMapperString());

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<Map<String, String>> getConnectedEntities(final String contextType, final Long entityId) {
		final ConnectedEntitiesSQLQueryBuilder queryBuilder = new ConnectedEntitiesSQLQueryBuilder(contextType, entityId
				, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		final List<Map<String, String>> queryResult = (List<Map<String, String>>)this.executeSelectQuery(queryBuilder.getSQL()
				, new GenericEntitesMapper());

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<Long> getConnectedEntityIds(final Long entityId) {
		final ConnectedEntityIdsSQLQueryBuilder queryBuilder = new ConnectedEntityIdsSQLQueryBuilder(entityId, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		final List<Long> queryResult = (List<Long>)this.executeSelectQuery(queryBuilder.getSQL()
				, new EntityIdMapper());
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<List<String>> getStringFields(final String tableName, final String field1, final Long field1Id, final List<String> fields) {
		final GenericFieldsSQLQueryBuilder queryBuilder = new GenericFieldsSQLQueryBuilder(tableName, field1, field1Id, fields, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		final List<List<String>> queryResult = (List<List<String>>)this.executeSelectQuery(queryBuilder.getSQL(),
				new GenericFieldsMapperString(fields.size()));

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<Map<String, String>> getEntitiesById(final String tableName, final String field1, final Long field1Id) {
		final GenericEntitiesSQLQueryBuilder queryBuilder = new GenericEntitiesSQLQueryBuilder(tableName, field1, field1Id, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		final List<Map<String, String>> queryResult = (List<Map<String, String>>)this.executeSelectQuery(queryBuilder.getSQL()
				, new GenericEntitesMapper());

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<Map<String, String>> getEntitiesEntityIdJoinedById(final String tableName, final String field1, final Long field1Id) {
		final GenericEntitiesEntityIdJoinedSQLQueryBuilder queryBuilder = new GenericEntitiesEntityIdJoinedSQLQueryBuilder(tableName, field1, field1Id, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		final List<Map<String, String>> queryResult = (List<Map<String, String>>)this.executeSelectQuery(queryBuilder.getSQL()
				, new GenericEntitesMapper());

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}

	public List<? extends SQLResponseObject> getStringFieldsWithCustomRowMapper(final String tableName,
			final String field1, final Long field1Id, final List<String> fields, final RowMapper<? extends SQLResponseObject> rowMapper) {
		final GenericFieldsSQLQueryBuilder queryBuilder = new GenericFieldsSQLQueryBuilder(tableName, field1, field1Id
				, fields, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		final List<? extends SQLResponseObject> queryResult = (List<? extends SQLResponseObject>)this.executeSelectQuery(
				queryBuilder.getSQL(), rowMapper);
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}

	public List<? extends SQLResponseObject> getStringFieldsEntityIdJoinedWithCustomRowMapper(
			final String tableName, final String field1, final Long field1Id, final List<String> fields
			, final RowMapper<? extends SQLResponseObject> rowMapper) {
		final GenericFieldsEntityIdJoinedSQLQueryBuilder queryBuilder = new GenericFieldsEntityIdJoinedSQLQueryBuilder(
				tableName, field1, field1Id, fields, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		final List<? extends SQLResponseObject> queryResult = (List<? extends SQLResponseObject>)this.executeSelectQuery(
				queryBuilder.getSQL(), rowMapper);
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
}
