package de.uni_koeln.arachne.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.ConnectionDao;

/**
 * Service class to query the 'Verknuepfungen' table.
 */
@Service
public class ConnectionService {
	
	/**
	 * Hibernate DAO class.
	 */
	@Autowired
	private ConnectionDao arachneConnectionDao; // NOPMD
	
	/**
	 * Retrieves a list of connected tables from the table 'Verknuepfungen'.
	 * @param type The name of the table connections are searched for. 
	 * @return List of connections.
	 */
	public List<String> getConnectionList(final String type) {
		return arachneConnectionDao.getConnectionList(type);
	}
	
	/**
	 * Retrieves the name of the 'cross table' connecting two tables.
	 * @param table1 First table name.
	 * @param table2 Second table name.
	 * @return The table name of the 'cross table'.
	 */
	public String getTableName(final String table1, final String table2) {
		return arachneConnectionDao.getTableName(table1, table2);
	}
}
