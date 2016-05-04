package de.uni_koeln.arachne.testconfig;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class TestCatalogData {

	private JdbcTemplate jdbcTemplate;
	
	public TestCatalogData(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public TestCatalogData setUpCatalog() throws DataAccessException {
		jdbcTemplate.execute("CREATE TABLE catalog("
				+ "id int(10) PRIMARY KEY AUTO_INCREMENT,"
				+ "root_id int(10),"
				+ "author varchar(255) NOT NULL,"
				+ "public tinyint(1) NOT NULL,"
				+ "DatensatzGruppeCatalog varchar(255) NOT NULL);");
		jdbcTemplate.execute("INSERT INTO catalog("
				+ "id, root_id, author, public, DatensatzGruppeCatalog)"
				+ "VALUES"
				+ "(1,1,'testuser',1,'userTestGroup');");
		jdbcTemplate.execute("INSERT INTO catalog("
				+ "id, root_id, author, public, DatensatzGruppeCatalog)"
				+ "VALUES"
				+ "(2,3,'testuser',0,'userTestGroup');");
		
		jdbcTemplate.execute("CREATE TABLE catalog_benutzer("
				+ "catalog_id INT NOT NULL,"
				+ "uid INT NOT NULL)");
		jdbcTemplate.execute("INSERT INTO catalog_benutzer("
				+ "catalog_id, uid)"
				+ "VALUES"
				+ "(1,3)");
		jdbcTemplate.execute("INSERT INTO catalog_benutzer("
				+ "catalog_id, uid)"
				+ "VALUES"
				+ "(2,3)");
		jdbcTemplate.execute("INSERT INTO catalog_benutzer("
				+ "catalog_id, uid)"
				+ "VALUES"
				+ "(1,4)");
		return this;
	}
	
	public TestCatalogData tearDownCatalog() throws DataAccessException {
		jdbcTemplate.execute("DROP TABLE catalog;");
		jdbcTemplate.execute("DROP TABLE catalog_benutzer;");
		return this;
	}
	
	public TestCatalogData setUpCatalogEntry() throws DataAccessException {
		jdbcTemplate.execute("CREATE TABLE catalog_entry("
				+ "id INT AUTO_INCREMENT PRIMARY KEY,"
				+ "catalog_id INT NOT NULL,"
				+ "parent_id INT DEFAULT NULL,"
				+ "arachne_entity_id INT DEFAULT NULL,"
				+ "index_parent INT DEFAULT NULL,"
				+ "path VARCHAR(255) DEFAULT NULL,"
				+ "label VARCHAR(255),"
				+ "text TEXT DEFAULT NULL,"
				+ "last_modified TIMESTAMP NOT NULL,"
				+ "creation TIMESTAMP NOT NULL);");
		
		// catalog 1
		
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, path, label, last_modified, creation)"
				+ "VALUES"
				+ "(1,1,'1','root of catalog 1 test label','2000-01-01','2000-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, arachne_entity_id, path, label, last_modified, creation)"
				+ "VALUES"
				+ "(8,1,1,0,666,'1/1','child test label No. 1','2000-01-01','2000-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, label, text, last_modified, creation)"
				+ "VALUES"
				+ "(2,1,1,1,'child test label No. 2','some text for child No. 2','2000-01-01','2000-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, label, text, last_modified, creation)"
				+ "VALUES"
				+ "(9,1,1,2,'child test label No. 3','some text for child No. 3','2000-01-01','2000-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, path, label, last_modified, creation)"
				+ "VALUES"
				+ "(4,1,8,0,'1/8','child test label level 1 No. 1','2000-01-01','2000-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, label, last_modified, creation)"
				+ "VALUES"
				+ "(5,1,8,2,'child test label level 1 No. 3','2000-01-01','2000-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, label, last_modified, creation)"
				+ "VALUES"
				+ "(6,1,8,1,'child test label level 1 No. 2','2000-01-01','2000-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, label, last_modified, creation)"
				+ "VALUES"
				+ "(7,1,6,'child test label level 2 No. 1','2000-01-01','2000-01-01');");
		
		// catalog 2
		
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, arachne_entity_id, path, label, text, last_modified, creation)"
				+ "VALUES"
				+ "(3,2,666,'2','root of catalog 2 test label','arachneentity test','2000-01-01','2000-01-01');");
		return this;
	}
	
	public TestCatalogData tearDownCatalogEntry() throws DataAccessException {
		jdbcTemplate.execute("DROP TABLE catalog_entry;");
		return this;
	}
	
	public TestCatalogData setUpArachneEntityIdentification() throws DataAccessException {
		jdbcTemplate.execute("CREATE TABLE arachneentityidentification("
				+ "ArachneEntityID BIGINT(20) NOT NULL,"
				+ "TableName VARCHAR(255) NOT NULL,"
				+ "ForeignKey BIGINT(20) NOT NULL,"
				+ "isDeleted TINYINT(4) NOT NULL)");
		jdbcTemplate.execute("INSERT INTO arachneentityidentification("
				+ "ArachneEntityID, TableName, ForeignKey, isDeleted)"
				+ "VALUES"
				+ "(666,'TestTable',1,0);");
		jdbcTemplate.execute("INSERT INTO arachneentityidentification("
				+ "ArachneEntityID, TableName, ForeignKey, isDeleted)"
				+ "VALUES"
				+ "(667,'TestTable',2,0);");
		
		jdbcTemplate.execute("CREATE TABLE arachneentitydegrees("
				+ "ArachneEntityID BIGINT(20) NOT NULL,"
				+ "Degree INT(11) NOT NULL)");
		jdbcTemplate.execute("INSERT INTO arachneentitydegrees("
				+ "ArachneEntityID, Degree)"
				+ "VALUES"
				+ "(666,4);");
		jdbcTemplate.execute("INSERT INTO arachneentitydegrees("
				+ "ArachneEntityID, Degree)"
				+ "VALUES"
				+ "(667,5);");
		return this;
	}
	
	public TestCatalogData tearDownArachneEntityIdentification() throws DataAccessException {
		jdbcTemplate.execute("DROP TABLE arachneentityidentification;");
		jdbcTemplate.execute("DROP TABLE arachneentitydegrees;");
		return this;
	}
}
