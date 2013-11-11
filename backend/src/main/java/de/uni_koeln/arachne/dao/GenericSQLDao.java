package de.uni_koeln.arachne.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.context.ContextPath;
import de.uni_koeln.arachne.mapping.EntityIdMapper;
import de.uni_koeln.arachne.mapping.GenericFieldMapperInteger;
import de.uni_koeln.arachne.mapping.GenericFieldMapperString;
import de.uni_koeln.arachne.service.SQLResponseObject;
import de.uni_koeln.arachne.sqlutil.ConnectedEntityIdsSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.ConnectedEntitiesSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.ConnectedPathEntitiesSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericFieldSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericFieldsEntityIdJoinedSQLQueryBuilder;

/**
 * Class to retrieve data via SQL.
 */
@Repository("GenericSQLDao")
public class GenericSQLDao extends SQLDao {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericSQLDao.class);
	
	public List<String> getStringField(final String tableName, final String field1, final Long field1Id
			, final String field2, final boolean disableAuthorization) {
		final GenericFieldSQLQueryBuilder queryBuilder = new GenericFieldSQLQueryBuilder(tableName, field1
				, field1Id, field2, disableAuthorization);
		@SuppressWarnings("unchecked")
		final List<String> queryResult = (List<String>)this.executeSelectQuery(queryBuilder.getSQL(), new GenericFieldMapperString());
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<String> getStringField(final String tableName, final String field1, final Long field1Id
			, final String field2) {
		return getStringField(tableName, field1, field1Id, field2, false);		
	}
	
	public List<Integer> getIntegerField(final String tableName, final String field1, final Long field1Id
			, final String field2, final boolean disableAuthorization) {
		final GenericFieldSQLQueryBuilder queryBuilder = new GenericFieldSQLQueryBuilder(tableName, field1
				, field1Id, field2, disableAuthorization);
		@SuppressWarnings("unchecked")
		final List<Integer> queryResult = (List<Integer>)this.executeSelectQuery(queryBuilder.getSQL(), new GenericFieldMapperInteger());

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<Integer> getIntegerField(final String tableName, final String field1, final Long field1Id
			, final String field2) {
		return getIntegerField(tableName, field1, field1Id, field2, false);		
	}
	
	public List<Map<String, String>> getConnectedEntities(final String contextType, final Long entityId) {
		final ConnectedEntitiesSQLQueryBuilder queryBuilder = new ConnectedEntitiesSQLQueryBuilder(contextType, entityId);
		@SuppressWarnings("unchecked")
		final List<Map<String, String>> queryResult = (List<Map<String, String>>)this.executeSelectQuery(queryBuilder.getSQL()
				, new GenericEntitiesMapper("AdditionalInfosJSON"));

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<Long> getConnectedEntityIds(final Long entityId) {
		final ConnectedEntityIdsSQLQueryBuilder queryBuilder = new ConnectedEntityIdsSQLQueryBuilder(entityId);
		@SuppressWarnings("unchecked")
		final List<Long> queryResult = (List<Long>)this.executeSelectQuery(queryBuilder.getSQL()
				, new EntityIdMapper());
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<Long> getPathConnectedEntityIds(final Long entityId, final ContextPath contextPath) {
		final ConnectedPathEntitiesSQLQueryBuilder sqlBuilder = new ConnectedPathEntitiesSQLQueryBuilder(contextPath, entityId);
		sqlBuilder.retriveFullDataset(false);
		final String sql= sqlBuilder.getSQL();
		LOGGER.debug(sql);
		
		@SuppressWarnings("unchecked")
		final List<Long> queryResult = (List<Long>)this.executeSelectQuery(sql, new EntityIdMapper());
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<Map<String, String>> getPathConnectedEntities(final Long entityId, final ContextPath contextPath) {
		final ConnectedPathEntitiesSQLQueryBuilder sqlBuilder = new ConnectedPathEntitiesSQLQueryBuilder(contextPath, entityId);
		sqlBuilder.retriveFullDataset(true);
		final String sql= sqlBuilder.getSQL();
		LOGGER.debug(sql);
		@SuppressWarnings("unchecked")
		final List<Map<String, String>> queryResult = (List<Map<String, String>>)this.executeSelectQuery(sql
				, new GenericEntitiesMapper("AdditionalInfosJSON"));

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<? extends SQLResponseObject> getStringFieldsEntityIdJoinedWithCustomRowMapper(
			final String tableName, final String field1, final Long field1Id, final List<String> fields
			, final RowMapper<? extends SQLResponseObject> rowMapper) {
		final GenericFieldsEntityIdJoinedSQLQueryBuilder queryBuilder = new GenericFieldsEntityIdJoinedSQLQueryBuilder(
				tableName, field1, field1Id, fields);
		@SuppressWarnings("unchecked")
		final List<? extends SQLResponseObject> queryResult = (List<? extends SQLResponseObject>)this.executeSelectQuery(
				queryBuilder.getSQL(), rowMapper);
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
}
