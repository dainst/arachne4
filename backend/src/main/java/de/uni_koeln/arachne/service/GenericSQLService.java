package de.uni_koeln.arachne.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.GenericSQLDao;

@Service
public class GenericSQLService {
	@Autowired
	protected GenericSQLDao genericSQLDao;
	
	public List<Long> getIdByFieldId(String tableName, String field1, Long field1Id, String field2) {
		return genericSQLDao.getIdByFieldId(tableName, field1, field1Id, field2);
	}
	
	public List<String> getStringField(String tableName, String field1, Long field1Id, String field2) {
		return genericSQLDao.getStringField(tableName, field1, field1Id, field2);
	}
	
	public List<List<String>> getStringFields(String tableName, String field1, Long field1Id, List<String> fields) {
		return genericSQLDao.getStringFields(tableName, field1, field1Id, fields);
	}
	
	public List<Map<String, String>> getEntitiesById(String tableName, String field1, Long field1Id) {
		return genericSQLDao.getEntitiesById(tableName, field1, field1Id);
	}
}
