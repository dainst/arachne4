package de.uni_koeln.arachne.dao.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.service.SimpleSQLService;

import javax.sql.DataSource;

// TODO JavaDoc class description.
/**
 * 
 * 
 * @author Daniel M. de Oliveira
 * @author Reimar Grabowski
 */
@Repository("BookDao")
public class BookDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookDao.class);

    @Autowired
    private transient SimpleSQLService simpleSQLService;

    /**
     * @param arachneEntityId
     * @return The folder name for the book or <code>null</code> on failure.
     */
    public String getTEIFolderName(final String arachneEntityId) {
    	// disabled for missing rights management
        /* try {
            // TODO move the SQL generation to SQLFactory especially considering rights management 
        	return simpleSQLService.getJDBCTemplate().queryForObject("SELECT Verzeichnis FROM buch LEFT JOIN arachneentityidentification " +
                    "ON buch.PS_BuchID=arachneentityidentification.ForeignKey WHERE arachneentityidentification.TableName='buch' " +
                    "AND arachneentityidentification.ArachneEntityID='"+arachneEntityId+"'", String.class);
        } catch (DataAccessException e) {
            LOGGER.error("Failed to execute query '" + arachneEntityId + "'. Cause: ", e);
            return null;
        } */
    	return null;
    }
}
