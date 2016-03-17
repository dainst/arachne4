package de.uni_koeln.arachne.dao.jdbc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.testconfig.TestUserData;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:unittest-context.xml"})
public class TestGenericSQLDao {

	@Autowired
	@InjectMocks
	private GenericSQLDao genericSQLDao;
	
	@Mock
	private UserRightsService userRightsService;
	
	@Autowired
	private DataSource datasource;
		
	private JdbcTemplate jdbcTemplate;
	
	private TestUserData testUserData;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(userRightsService.isSignedInUser()).thenReturn(true);
		when(userRightsService.getCurrentUser()).thenReturn(TestUserData.getUser());
		when(userRightsService.getSQL("marbilder")).thenReturn(" AND (DatensatzGruppeMarbilder = 'userTestGroup' "
				+ "OR DatensatzGruppeMarbilder = 'anotherTestGroup')");
		when(userRightsService.getSQL("TestTable1")).thenReturn(" AND (DatensatzGruppetestTable1 = 'userTestGroup' "
				+ "OR DatensatzGruppetestTable1 = 'anotherTestGroup')");
		
		jdbcTemplate = new JdbcTemplate(datasource);
		testUserData = new TestUserData(jdbcTemplate);
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
*/
	
	public boolean setUpTestGetStringField() {
		try {
			jdbcTemplate.execute("CREATE TABLE TestTable1("
					+ "id INT NOT NULL,"
					+ "data VARCHAR(32) NOT NULL,"
					+ "DatensatzGruppetestTable1 VARCHAR(255) NOT NULL);");
			
			jdbcTemplate.execute("INSERT INTO TestTable1("
					+ "id, data, DatensatzGruppetestTable1)"
					+ "VALUES"
					+ "(1,'some data','userTestGroup');");
			jdbcTemplate.execute("INSERT INTO TestTable1("
					+ "id, data, DatensatzGruppetestTable1)"
					+ "VALUES"
					+ "(2,'wrong group','wrongGroup');");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}
	
	public void tearDownTestGetStringField() {
		jdbcTemplate.execute("DROP table TestTable;");
	}
	
	@Test
	public void testGetStringField() {
		if (setUpTestGetStringField()) {
			// no authorization
			String value = genericSQLDao.getStringField("TestTable1", "id", 1, "data", true);
			assertNotNull(value);
			assertEquals("some data", value);
			// authorization
			value = genericSQLDao.getStringField("TestTable1", "id", 1, "data", false);
			assertNotNull(value);
			assertEquals("some data", value);
			value = genericSQLDao.getStringField("TestTable1", "id", 2, "data", false);
			assertNull(value);
		} else {
			fail();
		}
	}
	
	public boolean setUpTestGetConnectedEntities() {
		try {
			jdbcTemplate.execute("CREATE TABLE SemanticConnection("
					+ "Source INT NOT NULL,"
					+ "Target INT NOT NULL,"
					+ "ForeignKeyTarget INT NOT NULL,"
					+ "TypeTarget VARCHAR(255) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO SemanticConnection("
					+ "Source, Target, ForeignKeyTarget, TypeTarget)"
					+ "VALUES"
					+ "(1,1,1,'TestTable1');");
			jdbcTemplate.execute("INSERT INTO SemanticConnection("
					+ "Source, Target, ForeignKeyTarget, TypeTarget)"
					+ "VALUES"
					+ "(1,2,1,'TestTable2');");
			jdbcTemplate.execute("INSERT INTO SemanticConnection("
					+ "Source, Target, ForeignKeyTarget, TypeTarget)"
					+ "VALUES"
					+ "(1,3,2,'TestTable1');");
			
			jdbcTemplate.execute("CREATE TABLE TestTable1("
					+ "PS_TestTable1ID INT NOT NULL,"
					+ "Data VARCHAR(255) NOT NULL,"
					+ "DatensatzGruppetestTable1 VARCHAR(255) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO TestTable1("
					+ "PS_TestTable1ID, Data, DatensatzGruppetestTable1)"
					+ "VALUES"
					+ "(1,'data1','userTestGroup');");
			jdbcTemplate.execute("INSERT INTO TestTable1("
					+ "PS_TestTable1ID, Data, DatensatzGruppetestTable1)"
					+ "VALUES"
					+ "(2,'data2','userTestGroup');");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}
	
	public void tearDownTestGetConnectedEntities() {
		jdbcTemplate.execute("DROP table SemanticConnection");
		jdbcTemplate.execute("DROP table TestTable1");
	}
	
	@Test
	public void testGetConnectedEntities() {
		if (setUpTestGetConnectedEntities()) {
			final List<Map<String, String>> entities = genericSQLDao.getConnectedEntities("TestTable1", 1);
			assertNotNull(entities);
			assertEquals(2, entities.size());
			final Map<String, String> entity = entities.get(0);
			if (entity.get("TestTable1.Data").equals("data1")) {
				assertEquals("data2", entities.get(1).get("TestTable1.Data"));
			} else {
				assertEquals("data1", entities.get(1).get("TestTable1.Data"));
				assertEquals("data2", entity.get("TestTable1.Data"));
			}
			tearDownTestGetConnectedEntities();
		} else {
			fail();
		}
	}
	
	public boolean setUpTestGetConnectedEntityIds() {
		try {
			jdbcTemplate.execute("CREATE TABLE SemanticConnection("
					+ "Source INT NOT NULL,"
					+ "Target INT NOT NULL,"
					+ "TypeSource VARCHAR(255) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO SemanticConnection("
					+ "Source, Target, TypeSource)"
					+ "VALUES"
					+ "(1,1,'TestTable1');");
			jdbcTemplate.execute("INSERT INTO SemanticConnection("
					+ "Source, Target, TypeSource)"
					+ "VALUES"
					+ "(1,2,'TestTable2');");
			jdbcTemplate.execute("INSERT INTO SemanticConnection("
					+ "Source, Target, TypeSource)"
					+ "VALUES"
					+ "(1,3,'marbilder');");
			jdbcTemplate.execute("INSERT INTO SemanticConnection("
					+ "Source, Target, TypeSource)"
					+ "VALUES"
					+ "(2,1,'TestTable1');");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}
	
	public void tearDownTestGetConnectedEntityIds() {
		jdbcTemplate.execute("DROP table SemanticConnection");
	}
	
	@Test
	public void testGetConnectedEntityIds() {
		if (setUpTestGetConnectedEntityIds()) {
			final List<Long> ids = genericSQLDao.getConnectedEntityIds(1);
			assertNotNull(ids);
			assertEquals(2, ids.size());
			assertTrue(ids.contains(1L));
			assertTrue(ids.contains(2L));
			tearDownTestGetConnectedEntityIds();
		} else {
			fail();
		}		
	}
	
	public boolean setUpTestGetImageList() {
		try {
			testUserData.createUserTable();
			testUserData.setUpUser(TestUserData.getUser());
			
			jdbcTemplate.execute("CREATE TABLE marbilder("
					+ "PS_MARBilderID INT NOT NULL,"
					+ "DateinameMarbilder VARCHAR(16) NOT NULL,"
					+ "FS_ObjektID INT NOT NULL,"
					+ "DatensatzGruppeMarbilder varchar(255));");
			jdbcTemplate.execute("INSERT INTO marbilder("
					+ "PS_MARBilderID, DateinameMarbilder, FS_ObjektID, DatensatzGruppeMarbilder)"
					+ "VALUES"
					+ "(1,'test_image1.jpg', 1, 'userTestGroup');");
			jdbcTemplate.execute("INSERT INTO marbilder("
					+ "PS_MARBilderID, DateinameMarbilder, FS_ObjektID, DatensatzGruppeMarbilder)"
					+ "VALUES"
					+ "(2,'test_image2.jpg', 1, 'userTestGroup');");
			
			jdbcTemplate.execute("CREATE TABLE arachneentityidentification("
					+ "TableName VARCHAR(16) NOT NULL,"
					+ "ForeignKey INT NOT NULL,"
					+ "ArachneEntityID INT NOT NULL);");
			jdbcTemplate.execute("INSERT INTO arachneentityidentification("
					+ "TableName, ForeignKey, ArachneEntityID)"
					+ "VALUES"
					+ "('marbilder',1,7);");
			jdbcTemplate.execute("INSERT INTO arachneentityidentification("
					+ "TableName, ForeignKey, ArachneEntityID)"
					+ "VALUES"
					+ "('marbilder',2,8);");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}
	
	public void tearDownTestGetImageList() {
		testUserData.dropUserTable();
		
		jdbcTemplate.execute("DROP TABLE marbilder;");
		jdbcTemplate.execute("DROP TABLE arachneentityidentification;");
	}
	
	@Test
	public void testGetImageList() {
		if (setUpTestGetImageList()) {
			List<Image> images = genericSQLDao.getImageList("objekt", 1L);
			assertNotNull(images);
			assertEquals(2, images.size());
			for (final Image image: images) {
				if (image.getImageId() == 7) {
					assertEquals("test_image1", image.getImageSubtitle());
				} else {
					assertEquals("test_image2", image.getImageSubtitle());
				}
			}
			tearDownTestGetImageList();
		} else {
			fail();
		}
	}

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
			tearDownTestGetLiterature();
		} else {
			fail();
		}
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
			tearDownTestGetBookCoverPage();
		} else {
			fail();
		}
	}
}
