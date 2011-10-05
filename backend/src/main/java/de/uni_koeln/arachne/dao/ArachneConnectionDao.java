package de.uni_koeln.arachne.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.ArachneConnection;
import de.uni_koeln.arachne.mapping.ArachneEntity;

/**
 * Data access class to retrieve information from the 'Verknuepfungen' table via hibernate.
 */
@Repository("ArachneConnectionDao")
public class ArachneConnectionDao extends HibernateTemplateDao {
	/**
	 * @param primaryId Primary id of the entry.
	 * @return an instance of the <code>ArachneConnection</code> table mapping.
	 */
	public ArachneConnection getByID(Long primaryId) {
		return (ArachneConnection) hibernateTemplate.get(ArachneConnection.class, primaryId);
	}
	
	/**
	 * Retrieves a list of 'contexts' that are connected to <code>type</code>.
	 * @param type The table name to seek connected tables for. 
	 */
	public List<String> getConnectionList(String type) {
		@SuppressWarnings("unchecked")
		List<ArachneConnection> queryResult = (List<ArachneConnection>) hibernateTemplate
				.find("from ArachneConnection where Teil1 = " + "'" + type + "'");
		List<String> result = new ArrayList<String>();
		for (int i=0; i<queryResult.size(); i++) {
			 result.add(queryResult.get(i).getPart2());
		}
		return result;
	}
}
