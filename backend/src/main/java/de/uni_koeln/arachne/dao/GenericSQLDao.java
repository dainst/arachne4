package de.uni_koeln.arachne.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.GenericFieldMapperLong;
import de.uni_koeln.arachne.mapping.GenericFieldMapperString;
import de.uni_koeln.arachne.mapping.GenericFieldsMapperString;
import de.uni_koeln.arachne.service.SQLResponseObject;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.sqlutil.GenericFieldSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericEntitiesSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericFieldsEntityIdJoinedSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericFieldsSQLQueryBuilder;

/**
 * Class to retrieve referenced ids from 'cross tables'.
 */
@Repository("GenericSQLDao")
public class GenericSQLDao extends SQLDao {
	
	@Autowired
	private UserRightsService userRightsService;
	
	/**
	 * Retrieves a list of ids from a 'cross table' field by a specified foreign key field and corresponding id or <code>null</code>.
	 * @param tableName The 'cross table' to query.
	 * @param field1 Foreign key field for which the id is given.
	 * @param field1Id The foreign key.
	 * @param field2 The key field to be queried.
	 * @return a list of foreign ids or <code>null</code>.
	 */
	public List<Long> getIdByFieldId(String tableName, String field1, Long field1Id, String field2) {
		GenericFieldSQLQueryBuilder queryBuilder = new GenericFieldSQLQueryBuilder(tableName, field1, field1Id, field2, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		List<Long> queryResult = (List<Long>)this.executeSelectQuery(queryBuilder.getSQL(), new GenericFieldMapperLong());
		if (queryResult != null) {
			queryResult.remove(null);
			if (!queryResult.isEmpty()) {
				return queryResult;
			}
		}
		return null;
	}
	
	public List<String> getStringField(String tableName, String field1, Long field1Id, String field2) {
		GenericFieldSQLQueryBuilder queryBuilder = new GenericFieldSQLQueryBuilder(tableName, field1, field1Id, field2, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		List<String> queryResult = (List<String>)this.executeSelectQuery(queryBuilder.getSQL(), new GenericFieldMapperString());
		// IMPORTANT because string casting can add null strings to the list
		if (queryResult != null) {
			queryResult.remove(null);
			if (!queryResult.isEmpty()) {
				return queryResult;
			}
		}
		return null;
	}
	
	public List<List<String>> getStringFields(String tableName, String field1, Long field1Id, List<String> fields) {
		GenericFieldsSQLQueryBuilder queryBuilder = new GenericFieldsSQLQueryBuilder(tableName, field1, field1Id, fields, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		List<List<String>> queryResult = (List<List<String>>)this.executeSelectQuery(queryBuilder.getSQL(),
				new GenericFieldsMapperString(fields.size()));
		// IMPORTANT because string casting can add null strings to the list
		if (queryResult != null) {
			queryResult.remove(null);
			if (!queryResult.isEmpty()) {
				return queryResult;
			}
		}
		return null;
	}
	
	public List<Map<String, String>> getEntitiesById(String tableName, String field1, Long field1Id) {
		GenericEntitiesSQLQueryBuilder queryBuilder = new GenericEntitiesSQLQueryBuilder(tableName, field1, field1Id, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		List<Map<String, String>> queryResult = (List<Map<String, String>>)this.executeSelectQuery(queryBuilder.getSQL()
				, new GenericEntitesMapper());
		if (queryResult != null) {
			queryResult.remove(null);
			if (!queryResult.isEmpty()) {
				return queryResult;
			}
		}
		return null;
	}

	public List<? extends SQLResponseObject> getStringFieldsWithCustomRowMapper(String tableName,
			String field1, Long field1Id, ArrayList<String> fields, RowMapper<? extends SQLResponseObject> rowMapper) {
		GenericFieldsSQLQueryBuilder queryBuilder = new GenericFieldsSQLQueryBuilder(tableName, field1, field1Id, fields, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		List<? extends SQLResponseObject> queryResult = (List<? extends SQLResponseObject>)this.executeSelectQuery(
				queryBuilder.getSQL(), rowMapper);
		// IMPORTANT because string casting can add null strings to the list
		if (queryResult != null) {
			queryResult.remove(null);
			if (!queryResult.isEmpty()) {
				return queryResult;
			}
		}
		return null;
	}

	public List<? extends SQLResponseObject> getStringFieldsEntityIdJoinedWithCustomRowMapper(
			String tableName, String field1, Long field1Id,
			ArrayList<String> fields,
			RowMapper<? extends SQLResponseObject> rowMapper) {
		GenericFieldsEntityIdJoinedSQLQueryBuilder queryBuilder = new GenericFieldsEntityIdJoinedSQLQueryBuilder(tableName, field1, field1Id, fields, userRightsService.getCurrentUser());
		@SuppressWarnings("unchecked")
		List<? extends SQLResponseObject> queryResult = (List<? extends SQLResponseObject>)this.executeSelectQuery(
				queryBuilder.getSQL(), rowMapper);
		// IMPORTANT because string casting can add null strings to the list
		if (queryResult != null) {
			queryResult.remove(null);
			if (!queryResult.isEmpty()) {
				return queryResult;
			}
		}
		return null;
	}
}
