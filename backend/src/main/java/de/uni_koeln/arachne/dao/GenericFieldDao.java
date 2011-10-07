package de.uni_koeln.arachne.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.GenericFieldMapperLong;
import de.uni_koeln.arachne.sqlutil.ArachneGenericFieldSQLQueryBuilder;

/**
 * Class to retrieve referenced ids from 'cross tables'.
 */
@Repository("GenericFieldDao")
public class GenericFieldDao extends SQLDao {
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
}
