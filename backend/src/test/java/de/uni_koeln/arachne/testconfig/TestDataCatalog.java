package de.uni_koeln.arachne.testconfig;

import org.springframework.jdbc.core.JdbcTemplate;

public class TestDataCatalog {

	private JdbcTemplate jdbcTemplate;
	
	public TestDataCatalog(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setUpCatalog() throws Exception {
		jdbcTemplate.execute("CREATE TABLE catalog("
				+ "id INT NOT NULL,"
				+ "root_id INT NOT NULL,"
				+ "author VARCHAR(255) NOT NULL,"
				+ "public TINYINT(1) NOT NULL,"
				+ "DatenSatzGruppeCatalog VARCHAR(255) NOT NULL);");
		jdbcTemplate.execute("INSERT INTO catalog("
				+ "id, root_id, author, public, DatensatzGruppeCatalog)"
				+ "VALUES"
				+ "(1,1,'testuser',1,'Arachne');");
	}
	
	public void tearDownCatalog() throws Exception {
		jdbcTemplate.execute("DROP TABLE catalog;");
	}
	
	public void setUpCatalogEntry() throws Exception {
		jdbcTemplate.execute("CREATE TABLE catalog_entry("
				+ "id INT NOT NULL,"
				+ "catalog_id INT NOT NULL,"
				+ "parent_id INT DEFAULT NULL,"
				+ "arachneentityid INT DEFAULT NULL,"
				+ "index_parent INT DEFAULT NULL,"
				+ "path VARCHAR(255) DEFAULT NULL,"
				+ "label VARCHAR(255),"
				+ "text TEXT DEFAULT NULL,"
				+ "last_modified TIMESTAMP NOT NULL,"
				+ "creation TIMESTAMP NOT NULL);");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, label, last_modified, creation)"
				+ "VALUES"
				+ "(1,1,'root test label','1970-01-01','1970-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, label, last_modified, creation)"
				+ "VALUES"
				+ "(2,1,1,2,'child test label No. 2','1970-01-01','1970-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, label, last_modified, creation)"
				+ "VALUES"
				+ "(8,1,1,1,'child test label No. 1','1970-01-01','1970-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, label, last_modified, creation)"
				+ "VALUES"
				+ "(3,2,'not part of catalog test label','1970-01-01','1970-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, label, last_modified, creation)"
				+ "VALUES"
				+ "(4,1,8,0,'child test label level 1 No. 1','1970-01-01','1970-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, label, last_modified, creation)"
				+ "VALUES"
				+ "(5,1,8,2,'child test label level 1 No. 3','1970-01-01','1970-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, index_parent, label, last_modified, creation)"
				+ "VALUES"
				+ "(6,1,8,1,'child test label level 1 No. 2','1970-01-01','1970-01-01');");
		jdbcTemplate.execute("INSERT INTO catalog_entry("
				+ "id, catalog_id, parent_id, label, last_modified, creation)"
				+ "VALUES"
				+ "(7,1,6,'child test label level 2 No. 1','1970-01-01','1970-01-01');");
	}
	
	public void tearDownCatalogEntry() throws Exception {
		jdbcTemplate.execute("DROP TABLE catalog_entry;");
	}
}
