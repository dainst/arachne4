package de.uni_koeln.arachne.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.context.ContextPath;
import de.uni_koeln.arachne.service.SQLResponseObject;
import de.uni_koeln.arachne.sqlutil.ConnectedEntityIdsSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.ConnectedEntitiesSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.ConnectedPathEntitiesSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericFieldsEntityIdJoinedSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.SQLFactory;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * Class to retrieve data via SQL.
 */
@Repository("GenericSQLDao")
public class GenericSQLDao extends SQLDao {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericSQLDao.class);
	
	@Autowired
	private transient SQLFactory sqlFactory;
	
	public String getStringField(final String tableName, final String field1, final long field1Id
			, final String field2, final boolean disableAuthorization) {
		final String queryResult = queryForString(sqlFactory.getFieldQuery(tableName, field1, field1Id, field2, disableAuthorization));
		if (!StrUtils.isEmptyOrNull(queryResult)) {
			return queryResult;
		}
		return null;
	}
	
	public String getStringField(final String tableName, final String field1, final long field1Id
			, final String field2) {
		return getStringField(tableName, field1, field1Id, field2, false);		
	}
	
	public int getIntFieldById(final String tableName, final long id, final String field) {
		return queryForInt(sqlFactory.getFieldByIdQuery(tableName, id, field));
	}
	
	public List<Map<String, String>> getConnectedEntities(final String contextType, final long entityId) {
		final ConnectedEntitiesSQLQueryBuilder queryBuilder = new ConnectedEntitiesSQLQueryBuilder(contextType, entityId);
		@SuppressWarnings("unchecked")
		final List<Map<String, String>> queryResult = (List<Map<String, String>>)query(queryBuilder.getSQL()
				, new GenericEntitiesMapper("AdditionalInfosJSON"));

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<Long> getConnectedEntityIds(final long entityId) {
		final ConnectedEntityIdsSQLQueryBuilder queryBuilder = new ConnectedEntityIdsSQLQueryBuilder(entityId);
		@SuppressWarnings("unchecked")
		final List<Long> queryResult = (List<Long>)queryForList(queryBuilder.getSQL(), Long.class);
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
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
	
	public List<Map<String, String>> getPathConnectedEntities(final long entityId, final ContextPath contextPath) {
		final ConnectedPathEntitiesSQLQueryBuilder sqlBuilder = new ConnectedPathEntitiesSQLQueryBuilder(contextPath, entityId);
		sqlBuilder.retriveFullDataset(true);
		final String sql= sqlBuilder.getSQL();
		LOGGER.debug(sql);
		@SuppressWarnings("unchecked")
		final List<Map<String, String>> queryResult = (List<Map<String, String>>)query(sql
				, new GenericEntitiesMapper("AdditionalInfosJSON"));

		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
	
	public List<? extends SQLResponseObject> getStringFieldsEntityIdJoinedWithCustomRowMapper(
			final String tableName, final String field1, final long field1Id, final List<String> fields
			, final RowMapper<? extends SQLResponseObject> rowMapper) {
		final GenericFieldsEntityIdJoinedSQLQueryBuilder queryBuilder = new GenericFieldsEntityIdJoinedSQLQueryBuilder(
				tableName, field1, field1Id, fields);
		@SuppressWarnings("unchecked")
		final List<? extends SQLResponseObject> queryResult = (List<? extends SQLResponseObject>)query(
				queryBuilder.getSQL(), rowMapper);
		
		if (queryResult != null && !queryResult.isEmpty()) {
			return queryResult;
		}
		return null;
	}
}
