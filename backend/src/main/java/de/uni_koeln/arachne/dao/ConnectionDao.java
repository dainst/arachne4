package de.uni_koeln.arachne.dao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.Connection;

/**
 * Data access class to retrieve information from the 'Verknuepfungen' table via hibernate.
 */
@Repository("ArachneConnectionDao")
public class ConnectionDao extends AbstractHibernateTemplateDao {
	/**
	 * @param primaryId Primary id of the entry.
	 * @return an instance of the <code>ArachneConnection</code> table mapping.
	 */
	public Connection getByID(Long primaryId) {
		return (Connection) hibernateTemplate.get(Connection.class, primaryId);
	}
	
	/**
	 * Retrieves a list of 'contexts' that are connected to <code>type</code>.
	 * @param type The table name to seek connected tables for. 
	 */
	public List<String> getConnectionList(String type) {
		@SuppressWarnings("unchecked")
		List<Connection> queryResult = (List<Connection>) hibernateTemplate
				.find("from ArachneConnection where Teil1 = '" + type + "'");
		List<String> result = new ArrayList<String>();
		for (int i=0; i<queryResult.size(); i++) {
			 result.add(queryResult.get(i).getPart2());
		}
		return result;
	}
	
	/**
	 * Retrieves the name of the 'cross table' connecting two tables. If no table exists
	 * <code>null</code> is returned.
	 * @param table1 First table name.
	 * @param table2 Second table name.
	 * @return The table name of the 'cross table'.
	 */
	public String getTableName(String table1, String table2) {
		@SuppressWarnings("unchecked")
		List<Connection> queryResult = (List<Connection>) hibernateTemplate
				.find("from Connection where Teil1 = '" + table1 + "' and Teil2 = '" + table2 + "'");
		if (queryResult.size() > 0) {
			return queryResult.get(0).getTable();
		} else {
			return null;
		}
	}
}
