package de.uni_koeln.arachne.dao;



import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.response.ResponseFactory;

/**
 * This is a Standard SQL Query Dao.
 * The other it can execute Querys and Map them on special Dataset Instances.
 * @author Rasmus Krempel
 *
 */
@Repository("sqlDao")
public class SQLDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFactory.class);
	
	protected transient JdbcTemplate jdbcTemplate;
	
	protected transient DataSource dataSource;
	/**
	 * Through this Function the Datasource is Automaticly injected
	 * @param dataSource An SQl Datasource
	 */
	@Autowired
	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * This Function executes an SQL Statement and Maps it with a RowMapper
	 * @param sQLQuery The Query to be Executed
	 * @param rowMapper The RowMapper that Maps the Result of the Query to the an Generic Object Type
	 * @return Returns a List of objects as identified in the <code>RowMapper</code> or <code>null</code>
	 */
	protected List<?> executeSelectQuery(final String sQLQuery, final RowMapper<?> rowMapper) {
		if (sQLQuery.contains("SELECT")) {
			try {
				return jdbcTemplate.query(sQLQuery,rowMapper);
			} catch (DataAccessException e) {
				LOGGER.error("DataAccessException: " + e.getRootCause());
				return null;
			}
		} else {
			return null;
		}
	}
}
