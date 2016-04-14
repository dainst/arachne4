package de.uni_koeln.arachne.dao.jdbc;



import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * This is a Standard SQL Query Dao.
 * The other it can execute Querys and Map them on special Dataset Instances.
 * @author Rasmus Krempel
 * @author Reimar Grabowski
 *
 */
@Repository("sqlDao")
public class SQLDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(SQLDao.class);
	
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
	protected <T> List<T> query(final String sQLQuery, final RowMapper<T> rowMapper) {
		try {
			return jdbcTemplate.query(sQLQuery,rowMapper);
		} catch (DataAccessException e) {
			LOGGER.error("Failed to execute query '" + sQLQuery + "'. Cause: ", e);
		}
		return null;
	}
	
	/**
	 * This function executes a prepared statement and maps it with a <code>RowMapper</code>
	 * @param psc A <code>PreparedStatementCreator</code>
	 * @param rowMapper The RowMapper that Maps the Result of the Query to the an Generic Object Type
	 * @return Returns a List of objects as identified in the <code>RowMapper</code> or <code>null</code>
	 */
	protected <T> List<T> query(final PreparedStatementCreator psc, final RowMapper<T> rowMapper) {
		try {
			return jdbcTemplate.query(psc, rowMapper);
		} catch (DataAccessException e) {
			LOGGER.error("Failed to execute query.'Cause: ", e);
		}
		return null;
	}
	
	/**
	 * Executes a SQL SELECT query that retrieves a single object value.
	 * @param sqlQuery The sql query string.
	 * @param rowMapper A row mapper to map the result set to an instance of the specified type.
	 * @return The string value retrieved from the field or <code>null</code> on failure.
	 */
	protected <T> T queryForObject(final String sqlQuery, final RowMapper<T> rowMapper) {
		try {
			return jdbcTemplate.queryForObject(sqlQuery, rowMapper);
		} catch (DataAccessException e) {
			LOGGER.error("Failed to execute query '" + sqlQuery + "'. Cause: ", e);
		}
		return null;
	}
	
	/**
	 * Executes a SQL SELECT query that retrieves a single object value.
	 * @param sqlQuery The sql query string.
	 * @param requiredType The type of the result entity
	 * @return The string value retrieved from the field or <code>null</code> on failure.
	 */
	protected <T> T queryForObject(final String sqlQuery, Class<T> requiredType) {
		try {
			return jdbcTemplate.queryForObject(sqlQuery, requiredType);
		} catch (DataAccessException e) {
			LOGGER.error("Failed to execute query '" + sqlQuery + "'. Cause: ", e);
		}
		return null;
	}
	
	/**
	 * Executes a SQL SELECT query that retrieves a single string value.
	 * @param sqlQuery The sql query string.
	 * @return The string value retrieved from the field or <code>null</code> on failure.
	 */
	protected String queryForString(final String sqlQuery) {
		try {
			return jdbcTemplate.queryForObject(sqlQuery, String.class);
		} catch (DataAccessException e) {
			LOGGER.error("Failed to execute query '" + sqlQuery + "'. Cause: ", e);
		}
		return null;
	}
	
	/**
	 * Executes a SQL SELECT query that retrieves a single string value.
	 * @param sqlQuery The sql query string.
	 * @return The string value retrieved from the field or <code>null</code> on failure.
	 */
	protected Long queryForLong(final String sqlQuery) {
		try {
			return jdbcTemplate.queryForObject(sqlQuery, Long.class);
		} catch (DataAccessException e) {
			LOGGER.error("Failed to execute query '" + sqlQuery + "'. Cause: ", e);
		}
		return null;
	}
	
	/**
	 * Executes a SQL SELECT query that retrieves a single string value.
	 * @param sqlQuery The sql query string.
	 * @return The string value retrieved from the field or <code>null</code> on failure.
	 */
	protected Integer queryForInt(final String sqlQuery) {
		try {
			return jdbcTemplate.queryForObject(sqlQuery, Integer.class);
		} catch (DataAccessException e) {
			LOGGER.error("Failed to execute query '" + sqlQuery + "'. Cause: ", e);
		}
		return null;
	}
	
	/**
	 * Executes a SQL SELECT query that retrieves a list.
	 * @param sqlQuery The sql query string.
	 * @param elementType 
	 * @return The retrieved list or <code>null</code> on failure.
	 */
	protected List<?> queryForList(final String sqlQuery, final Class<?> elementType) {
		try {
			return jdbcTemplate.queryForList(sqlQuery, elementType);
		} catch (DataAccessException e) {
			LOGGER.error("Failed to execute query '" + sqlQuery + "'. Cause: ", e);
		}
		return null;
	}
	
	/**
	 * Executes a SQL update method (like INSERT, DELETE, etc)
	 * @param sql The SQL query string containing bind parameters.
	 * @param args The arguments to bind.
	 * @return
	 */
	protected int update(final String sql, final Object[] args) {
		try {
			return jdbcTemplate.update(sql, args);
		} catch (DataAccessException e) {
			LOGGER.error("Failed to execute query '" + sql + "'. Cause: ", e);
		}
		return 0;
	}
	
	protected int update(final PreparedStatementCreator psc) {
		return jdbcTemplate.update(psc); 
	}
	
	protected long updateReturnKey(final PreparedStatementCreator psc) 
			throws DataAccessException, DataIntegrityViolationException {
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(psc, keyHolder);
		return keyHolder.getKey().longValue(); 
	}
}
