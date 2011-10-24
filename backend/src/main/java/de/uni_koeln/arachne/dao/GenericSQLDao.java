package de.uni_koeln.arachne.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.ArachneDatasetMapping;
import de.uni_koeln.arachne.mapping.GenericFieldMapperLong;
import de.uni_koeln.arachne.mapping.GenericFieldMapperString;
import de.uni_koeln.arachne.sqlutil.ArachneGenericFieldSQLQueryBuilder;
import de.uni_koeln.arachne.sqlutil.GenericEntitiesSQLQueryBuilder;

/**
 * Class to retrieve referenced ids from 'cross tables'.
 */
@Repository("GenericSQLDao")
public class GenericSQLDao extends SQLDao {
	/**
	 * Retrieves a list of ids from a 'cross table' field by a specified foreign key field and corresponding id or <code>null</code>.
	 * @param tableName The 'cross table' to query.
	 * @param field1 Foreign key field for which the id is given.
	 * @param field1Id The foreign key.
	 * @param field2 The key field to be queried.
	 * @return a list of foreign ids or <code>null</code>.
	 */
	public List<Long> getIdByFieldId(String tableName, String field1, Long field1Id, String field2) {
		ArachneGenericFieldSQLQueryBuilder queryBuilder = new ArachneGenericFieldSQLQueryBuilder(tableName, field1, field1Id, field2);
		List<Long> queryResult = (List<Long>)this.executeSelectQuery(queryBuilder.getSQL(), new GenericFieldMapperLong());
		if (!queryResult.isEmpty()) {
			return queryResult;
		} else {
			return null;
		}
	}
	
	public List<String> getStringField(String tableName, String field1, Long field1Id, String field2) {
		ArachneGenericFieldSQLQueryBuilder queryBuilder = new ArachneGenericFieldSQLQueryBuilder(tableName, field1, field1Id, field2);
		List<String> queryResult = (List<String>)this.executeSelectQuery(queryBuilder.getSQL(), new GenericFieldMapperString());
		// IMPORTANT because string casting can add null strings to the list
		queryResult.remove(null);
		if (!queryResult.isEmpty()) {
			return queryResult;
		} else {
			return null;
		}
	}
	
	public List<Map<String, String>> getEntitiesById(String tableName, String field1, Long field1Id) {
		GenericEntitiesSQLQueryBuilder queryBuilder = new GenericEntitiesSQLQueryBuilder(tableName, field1, field1Id);
		List<Map<String, String>> queryResult = (List<Map<String, String>>)this.executeSelectQuery(queryBuilder.getSQL()
				, new GenericEntitesMapper());
		return queryResult;
	}
}
