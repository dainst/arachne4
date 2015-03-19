package de.uni_koeln.arachne.dao.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.DatasetMapper;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.sql.SQLFactory;
import de.uni_koeln.arachne.util.sql.SimpleTableEntityQueryBuilder;
import de.uni_koeln.arachne.util.sql.SingleEntitySubTablesQueryBuilder;
import de.uni_koeln.arachne.util.sql.TableConnectionDescription;
/**
 * Querys the DataBase and retrives the Result as Key/Value Map
 */
@Repository("arachneDataMapDao")
public class DataMapDao extends SQLDao {
		
	private static final Logger LOGGER = LoggerFactory.getLogger(DataMapDao.class);
	
	@Autowired
	private transient SQLFactory sqlFactory;
	
	/**
	 * Gets a map of values by Id
	 * @param arachneId instance of <code>ArachneId</code> 
	 * @return a Simple representation of a Map<String,String> or <code>null</code>.
	 */
	public Map<String, String> getById(final EntityId arachneId) {			

		final String sql = sqlFactory.getSingleEntityQuery(arachneId);
		
		LOGGER.debug(sql);
		
		@SuppressWarnings("unchecked")
		final List<Map<String,String>> temp = (List<Map<String, String>>) this.query(sql, new DatasetMapper());
		if (temp != null && !temp.isEmpty()) {
			return temp.get(0);
		}
		return null;
	}
	
	/**
	 * Gets a map of values by PrimaryKey and TableName
	 * @param primaryKey Primary key within given table
	 * @param tableName Tablename
	 * @return a Simple representation of a Map<String,String> or <code>null</code>.
	 */
	public Map<String, String> getByPrimaryKeyAndTable(final Integer primaryKey, final String tableName) {			

		final SimpleTableEntityQueryBuilder queryBuilder = new SimpleTableEntityQueryBuilder(tableName, primaryKey);
		final String sql = queryBuilder.getSQL();
		
		LOGGER.debug(sql);

		@SuppressWarnings("unchecked")
		final List<Map<String,String>> temp = (List<Map<String, String>>) this.query(sql, new DatasetMapper());
		if (temp != null && !temp.isEmpty()) {
			return temp.get(0);
		}
		return null;
	}
	

	/**
	 * Gets a subdataset for a main dataset (Objekt -> Objektplastik) by using <code>ArachneSingleEntitySubTablesQueryBuilder</code> for query Building
	 * @param dataset Dataset for which the subdataset should be retrieved
	 * @param tableConnectionDescription instance of <code>TableConnectionDescription</code> which represents the Connection between the Dataset and the Subdataset 
	 * @return <code>Map<String,String></code> that contains the Description of the Subdataset, caution! The Subdataset is NOT automatically appended to the Dataset.
	 */
	public Map<String, String> getBySubDataset(final Dataset dataset, final TableConnectionDescription tableConnectionDescription ) {
		
		final SingleEntitySubTablesQueryBuilder queryBuilder = new SingleEntitySubTablesQueryBuilder(dataset
				,tableConnectionDescription);

		final String sql = queryBuilder.getSQL();
		LOGGER.debug(sql);
		@SuppressWarnings("unchecked")
		final List<Map<String,String>> temp = (List<Map<String, String>>) this.query(sql, new DatasetMapper());
		
		Map<String,String> map;  
		if (temp == null || temp.isEmpty()) {
			map = new HashMap<String,String>();
		} else {
			map = temp.get(0);
		}
		return map;	
	}
}
