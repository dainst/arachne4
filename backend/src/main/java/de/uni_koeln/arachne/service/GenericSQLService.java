package de.uni_koeln.arachne.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.GenericSQLDao;
import de.uni_koeln.arachne.mapping.UserAdministration;

@Service
public class GenericSQLService {
	@Autowired
	protected GenericSQLDao genericSQLDao; // NOPMD
	
	public List<Long> getIdByFieldId(final String tableName, final String field1, final Long field1Id, final String field2) {
		return genericSQLDao.getIdByFieldId(tableName, field1, field1Id, field2);
	}
	
	public List<String> getStringField(final String tableName, final String field1, final Long field1Id
			, final String field2, final UserAdministration currentUser) {
		return genericSQLDao.getStringField(tableName, field1, field1Id, field2, currentUser);
	}
	
	public List<Map<String, String>> getConnectedEntities(final String contextType, final Long entityId) {
		return genericSQLDao.getConnectedEntities(contextType, entityId);
	}
	
	public List<Long> getConnectedEntityIds(final Long entityId) {
		return genericSQLDao.getConnectedEntityIds(entityId);
	}
	
	public List<List<String>> getStringFields(final String tableName, final String field1, final Long field1Id, final List<String> fields) {
		return genericSQLDao.getStringFields(tableName, field1, field1Id, fields);
	}
	
	public List<Map<String, String>> getEntitiesById(final String tableName, final String field1, final Long field1Id) {
		return genericSQLDao.getEntitiesById(tableName, field1, field1Id);
	}

	public List<Map<String, String>> getEntitiesEntityIdJoinedById(final String tableName, final String field1, final Long field1Id) {
		return genericSQLDao.getEntitiesEntityIdJoinedById(tableName, field1, field1Id);
	}
	
	public List<? extends SQLResponseObject> getStringFieldsWithCustomRowmapper(final String tableName, final String field1
			, final Long field1Id, final List<String> fields, final RowMapper<? extends SQLResponseObject> rowMapper) {
		return genericSQLDao.getStringFieldsWithCustomRowMapper(tableName, field1, field1Id, fields, rowMapper);
	}
	
	public List<? extends SQLResponseObject> getStringFieldsEntityIdJoinedWithCustomRowmapper(final String tableName, final String field1
			, final Long field1Id, final List<String> fields, final RowMapper<? extends SQLResponseObject> rowMapper) {
		return genericSQLDao.getStringFieldsEntityIdJoinedWithCustomRowMapper(tableName, field1, field1Id, fields, rowMapper);
	}
}
