package de.uni_koeln.arachne.dao;



import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * This is a Standard SQL Query Dao.
 * The other it can execute Querys and Map them on special Dataset Instances.
 * @author Rasmus Krempel
 *
 */
@Repository("sqlDao")
public class SQLDao {

	protected JdbcTemplate jdbcTemplate;
	
	protected DataSource dataSource;
	/**
	 * Through this Function the Datasource is Automaticly injected
	 * @param dataSource An SQl Datasource
	 */
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * This Function executes an SQL Statement and Maps it with a RowMapper
	 * @param sQLQuery The Query to be Executed
	 * @param rm The RowMapper that Maps the Result of the Query to the an Generic Object Type
	 * @return Returns a List of objects as identified in the <code>RowMapper</code> or <code>null</code>
	 */
	protected List<?> executeSelectQuery(String sQLQuery, RowMapper<?> rm) {
		if (sQLQuery.contains("SELECT")) {
			return jdbcTemplate.query(sQLQuery,rm);
		} else {
			return null;
		}
	}
}
