package de.uni_koeln.arachne.testconfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;

/**
 * Class to build an 'embedded' SQL database. Used for testing.
 * 
 * @author Reimar Grabowski
 *
 */
public class EmbeddedMysqlDatabaseBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedMysqlDatabaseBuilder.class);

    private final String baseDatabaseDir = System.getProperty("java.io.tmpdir");
    private String databaseName = "test_db_" + System.nanoTime();
    private final int port = new Random().nextInt(10000) + 3306;
    private final String username = "root";
    private final String password = "";
    private boolean foreignKeyCheck;

    private final ResourceLoader resourceLoader;
    private final ResourceDatabasePopulator databasePopulator;

    /**
     * Default constructor.
     */
    public EmbeddedMysqlDatabaseBuilder() {
        resourceLoader = new DefaultResourceLoader();
        databasePopulator = new ResourceDatabasePopulator();
        foreignKeyCheck = true;
    }

    private EmbeddedMysqlDatabase createDatabase(MysqldResource mysqldResource) {
        if (!mysqldResource.isRunning()) {
            LOG.error("MySQL instance not found... Terminating");
            throw new RuntimeException("Cannot get Datasource, MySQL instance not started.");
        }
        EmbeddedMysqlDatabase database = new EmbeddedMysqlDatabase(mysqldResource);
        database.setDriverClassName("com.mysql.jdbc.Driver");
        database.setUsername(username);
        database.setPassword(password);
        String url = "jdbc:mysql://localhost:" + port + "/" + databaseName + "?" + "createDatabaseIfNotExist=true";

        if (!foreignKeyCheck) {
            url += "&sessionVariables=FOREIGN_KEY_CHECKS=0";
        }
        LOG.debug("database url: {}", url);
        database.setUrl(url);
        return database;
    }

    private MysqldResource createMysqldResource() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("=============== Starting Embedded MySQL using these parameters ===============");
            LOG.debug("baseDatabaseDir : " + baseDatabaseDir);
            LOG.debug("databaseName : " + databaseName);
            LOG.debug("host : localhost (hardcoded)");
            LOG.debug("port : " + port);
            LOG.debug("username : root (hardcode)");
            LOG.debug("password : (no password)");
            LOG.debug("=============================================================================");
        }

        Map<String, String> databaseOptions = new HashMap<String, String>();
        databaseOptions.put(MysqldResourceI.PORT, Integer.toString(port));

        MysqldResource mysqldResource = new MysqldResource(new File(baseDatabaseDir, databaseName));
        mysqldResource.start("embedded-mysqld-thread-" + System.currentTimeMillis(), databaseOptions);

        if (!mysqldResource.isRunning()) {
            throw new RuntimeException("MySQL did not start.");
        }

        LOG.debug("MySQL started successfully @ {}", System.currentTimeMillis());
        return mysqldResource;
    }

    private void populateScripts(EmbeddedMysqlDatabase database) {
        try {
            DatabasePopulatorUtils.execute(databasePopulator, database);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            database.shutdown();
        }
    }

    /**
     * Method to add an SQL script to the database.
     * @param script The SQL script.
     * @return <code>this</code> to allow for method chaining.
     */
    public EmbeddedMysqlDatabaseBuilder addSqlScript(String script) {
        databasePopulator.addScript(resourceLoader.getResource(script));
        return this;
    }

    /**
     * Whether to enable mysql foreign key check.
     * @param foreignKeyCheck indicates whether foreign key checks should be enabled ot not.
     * @return <code>this</code> to allow for method chaining.
     */
    public EmbeddedMysqlDatabaseBuilder setForeignKeyCheck(boolean foreignKeyCheck) {
        this.foreignKeyCheck = foreignKeyCheck;
        return this;
    }

    /**
     * Setter for the database name.
     * @param databaseName the databaseName to set
     */
    public final void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Builds the database.
     * @return The database.
     */
    public EmbeddedMysqlDatabase build() {
        MysqldResource mysqldResource = createMysqldResource();
        EmbeddedMysqlDatabase database = createDatabase(mysqldResource);
        populateScripts(database);
        return database;
    }
}