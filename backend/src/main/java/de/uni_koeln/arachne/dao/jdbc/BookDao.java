package de.uni_koeln.arachne.dao.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;


/**
 * @author Daniel M. de Oliveira
 */
@Repository("BookDao")
public class BookDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookDao.class);

    protected transient DataSource dataSource;
    private transient JdbcTemplate jdbcTemplate;

    /**
     * @param dataSource
     */
    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }


    /**
     * @param arachneEntityId
     * @return The folder name for the book or <code>null</code> on failure.
     */
    public String getTEIFolderName(final String arachneEntityId) {
        try {
            return jdbcTemplate.queryForObject("SELECT Verzeichnis FROM buch LEFT JOIN arachneentityidentification " +
                    "ON buch.PS_BuchID=arachneentityidentification.ForeignKey WHERE arachneentityidentification.TableName='buch' " +
                    "AND arachneentityidentification.ArachneEntityID='"+arachneEntityId+"'", String.class);
        } catch (DataAccessException e) {
            LOGGER.error("Failed to execute query '" + arachneEntityId + "'. Cause: ", e);
            return null;
        }
    }
}
