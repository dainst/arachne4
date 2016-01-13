package de.uni_koeln.arachne.dao.jdbc;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:unittest-context.xml"})
public class TestGenericSQLDao {

	@Autowired
	private GenericSQLDao genericSQLDao;
	
	@Autowired
	private DataSource datasource;
	
	private JdbcTemplate jdbcTemplate;
	
	@Before
	public void setUp() throws Exception {
		jdbcTemplate = new JdbcTemplate(datasource);
	}

	
/*
	@Test
	public void testGetStringFieldStringStringLongStringBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStringFieldStringStringLongString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetConnectedEntities() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetConnectedEntityIds() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPathConnectedEntityIds() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPathConnectedEntities() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetImageList() {
		fail("Not yet implemented");
	}
*/
	public boolean setUpTestGetLiterature() {
		try {
			jdbcTemplate.execute("CREATE TABLE literaturzitat("
					+ "PS_literaturzitatID INT NOT NULL,"
					+ "FS_LiteraturID INT NOT NULL,"
					+ "FS_TesttableID INT NOT NULL,"
					+ "lzdata VARCHAR(32) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO literaturzitat("
					+ "PS_literaturzitatID, FS_LiteraturID, FS_TesttableID, lzdata)"
					+ "VALUES"
					+ "(1,2,5,'some literaturzitat data');");
			jdbcTemplate.execute("INSERT INTO literaturzitat("
					+ "PS_literaturzitatID, FS_LiteraturID, FS_TesttableID, lzdata)"
					+ "VALUES"
					+ "(2,4,5,'some more literaturzitat data');");
			
			jdbcTemplate.execute("CREATE TABLE literatur("
					+ "PS_literaturID INT NOT NULL,"
					+ "ZenonID VARCHAR(16) NOT NULL,"
					+ "ldata VARCHAR(32) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO literatur("
					+ "PS_literaturID, ZenonID, ldata)"
					+ "VALUES"
					+ "(2,3,'some literatur data');");
			jdbcTemplate.execute("INSERT INTO literatur("
					+ "PS_literaturID, ZenonID, ldata)"
					+ "VALUES"
					+ "(4,'','some more literatur data');");
			
			jdbcTemplate.execute("CREATE TABLE buch("
					+ "PS_BuchID INT NOT NULL,"
					+ "bibid VARCHAR(16) NOT NULL,"
					+ "bdata VARCHAR(32) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO buch("
					+ "PS_BuchID, bibid, bdata)"
					+ "VALUES"
					+ "(4,3,'some buch data');");
			
			jdbcTemplate.execute("CREATE TABLE arachneentityidentification("
					+ "TableName VARCHAR(16) NOT NULL,"
					+ "ForeignKey INT NOT NULL,"
					+ "ArachneEntityID INT NOT NULL);");
			jdbcTemplate.execute("INSERT INTO arachneentityidentification("
					+ "TableName, ForeignKey, ArachneEntityID)"
					+ "VALUES"
					+ "('buch',4,6);");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}
	
	public void tearDownTestGetLiterature() {
		jdbcTemplate.execute("DROP TABLE literaturzitat;");
		jdbcTemplate.execute("DROP TABLE literatur;");
		jdbcTemplate.execute("DROP TABLE buch;");
		jdbcTemplate.execute("DROP TABLE arachneentityidentification;");
	}
	
	@Test
	public void testGetLiteratureValid() {
		if (setUpTestGetLiterature()) {
			final List<Map<String, String>> literature = genericSQLDao.getLiterature("testtable", 5L);
			assertNotNull(literature);
			assertFalse(literature.isEmpty());
			for (Map<String, String> literaturMap : literature) {
				assertFalse(literaturMap.isEmpty());
				if (literaturMap.containsKey("arachneentityidentification.ArachneEntityID")) {
					assertEquals("some literaturzitat data", literaturMap.get("literaturzitat.lzdata"));
					assertEquals("some literatur data", literaturMap.get("literatur.ldata"));
					assertEquals("some buch data", literaturMap.get("buch.bdata"));
					assertEquals("6", literaturMap.get("arachneentityidentification.ArachneEntityID"));
				} else {
					assertEquals("some more literaturzitat data", literaturMap.get("literaturzitat.lzdata"));
					assertEquals("some more literatur data", literaturMap.get("literatur.ldata"));
				}
			}
		} else {
			fail();
		}
		tearDownTestGetLiterature();
	}
	
	public boolean setUpTestGetBookCoverPage() {
		try {
			jdbcTemplate.execute("CREATE TABLE buchseite("
					+ "PS_buchseiteID INT NOT NULL,"
					+ "seite INT NOT NULL,"
					+ "FS_buchID INT NOT NULL);");
			jdbcTemplate.execute("INSERT INTO buchseite("
					+ "PS_buchseiteID, seite, FS_buchID)"
					+ "VALUES"
					+ "(1,0,1);");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}
	
	public void tearDownTestGetBookCoverPage() {
		jdbcTemplate.execute("DROP TABLE buchseite;");
	}
	
	@Test
	public void testGetBookCoverPageValid() {
		if (setUpTestGetBookCoverPage()) {
			assertEquals(Long.valueOf(1), genericSQLDao.getBookCoverPage(1L));
		} else {
			fail();
		}
		tearDownTestGetBookCoverPage();
	}
}
