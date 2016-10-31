package de.uni_koeln.arachne.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.hibernate.Connection;

/**
 * Data access class to retrieve information from the 'Verknuepfungen' table via hibernate.
 * @author Reimar Grabowski
 */
@Repository("ArachneConnectionDao")
public class ConnectionDao {

	@Autowired
    private transient SessionFactory sessionFactory;
		
	/**
	 * @param primaryId Primary id of the entry.
	 * @return an instance of the <code>ArachneConnection</code> table mapping.
	 */
	@Transactional(readOnly=true)
	public Connection getByID(final long primaryId) {
		Session session = sessionFactory.getCurrentSession();
		return (Connection) session.get(Connection.class, primaryId);
	}
	
	/**
	 * Retrieves a list of 'contexts' that are connected to <code>type</code>.
	 * @param type The table name to seek connected tables for.
	 * @return A list of 'contexts'. 
	 */
	@Transactional(readOnly=true)
	public List<String> getConnectionList(final String type) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from ArachneConnection where Teil1 = :type")
				.setString("type", type);
		
		@SuppressWarnings("unchecked")
		final List<Connection> queryResult = (List<Connection>) query.list();
		final List<String> result = new ArrayList<String>();
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
	@Transactional(readOnly=true)
	public String getTableName(final String table1, final String table2) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from Connection where Teil1 = :table1 and Teil2 = :table2")
				.setString("table1", table1)
				.setString("table2", table2);
		
		@SuppressWarnings("unchecked")
		final List<Connection> queryResult = (List<Connection>) query.list();
				
		if (queryResult.size() > 0) {
			return queryResult.get(0).getTable();
		} else {
			return null;
		}
	}
}
