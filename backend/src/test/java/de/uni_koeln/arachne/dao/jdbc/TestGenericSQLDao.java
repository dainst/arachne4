package de.uni_koeln.arachne.dao.jdbc;

import de.uni_koeln.arachne.context.JointContextDefinition;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.testconfig.EmbeddedDataSourceConfig;
import de.uni_koeln.arachne.testconfig.TestUserData;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EmbeddedDataSourceConfig.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
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

	private final String testTableName;

	public TestGenericSQLDao() {
		boolean MixedCaseTableNamesSupported = System
				.getProperty("myMachineDoesNotSupportMixedCaseSQLTableNames") == null;
		if (MixedCaseTableNamesSupported) {
			testTableName = "Test_Table";
		} else {
			// contains only lower case letters since there were problems on a
			// dev machine when containing uppercase letters
			testTableName = "test_table";
		}

	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(userRightsService.isSignedInUser()).thenReturn(true);
		when(userRightsService.getCurrentUser()).thenReturn(TestUserData.getUser());
		when(userRightsService.getSQL(anyString())).thenAnswer(invocation -> " AND (DatensatzGruppe"
				+ StringUtils.capitalize(invocation.getArgumentAt(0, String.class)) + " = 'userTestGroup' "
				+ "OR DatensatzGruppe" + StringUtils.capitalize(invocation.getArgumentAt(0, String.class))
				+ " = 'anotherTestGroup')");

		jdbcTemplate = new JdbcTemplate(datasource);
		testUserData = new TestUserData(jdbcTemplate);
	}

	@After
	public void tearDown() throws Exception {
		testUserData.dropUserTable();
		jdbcTemplate.execute("DROP TABLE IF EXISTS marbilder; ");
		jdbcTemplate.execute("DROP TABLE IF EXISTS arachneentityidentification;");
		jdbcTemplate.execute("DROP TABLE IF EXISTS literaturzitat;");
		jdbcTemplate.execute("DROP TABLE IF EXISTS literatur;");
		jdbcTemplate.execute("DROP TABLE IF EXISTS buch;");
		jdbcTemplate.execute("DROP TABLE IF EXISTS arachneentityidentification;");
		jdbcTemplate.execute("DROP TABLE IF EXISTS buchseite;");
		jdbcTemplate.execute("DROP table IF EXISTS SemanticConnection");
		jdbcTemplate.execute("DROP table IF EXISTS " + testTableName + ";");
		jdbcTemplate.execute("DROP table IF EXISTS SemanticConnection");
	}

	/*
	 * @Test public void testGetStringFieldStringStringLongStringBoolean() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetStringFieldStringStringLongString() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetConnectedEntityIds() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetPathConnectedEntityIds() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetPathConnectedEntities() {
	 * fail("Not yet implemented"); }
	 */

	public boolean setUpTestGetStringFieldKeyEqualsTablename() {
		try {
			jdbcTemplate.execute("CREATE TABLE " + testTableName + "(" + "PS_" + testTableName + "ID INT NOT NULL,"
					+ "data VARCHAR(32) NOT NULL," + "DatensatzGruppe" + StringUtils.capitalize(testTableName)
					+ " VARCHAR(255) NOT NULL);");

			jdbcTemplate.execute("INSERT INTO " + testTableName + "(" + "PS_" + testTableName
					+ "ID, data, DatensatzGruppe" + StringUtils.capitalize(testTableName) + ")" + "VALUES"
					+ "(1,'some data','userTestGroup');");
			jdbcTemplate.execute("INSERT INTO " + testTableName + "(" + "PS_" + testTableName
					+ "ID, data, DatensatzGruppe" + StringUtils.capitalize(testTableName) + ")" + "VALUES"
					+ "(2,'wrong group','wrongGroup');");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Test
	public void testGetStringFieldKeyEqualsTablename() {
		if (!setUpTestGetStringFieldKeyEqualsTablename())
			fail("could not set up string field");

		// no authorization
		String value = genericSQLDao.getStringField(testTableName, testTableName, 1, "data", true);
		assertNotNull(value);
		assertEquals("some data", value);
		// authorization
		value = genericSQLDao.getStringField(testTableName, testTableName, 1, "data", false);
		assertNotNull(value);
		assertEquals("some data", value);
		value = genericSQLDao.getStringField(testTableName, testTableName, 2, "data", false);
		assertNull(value);
	}

	public boolean setUpTestGetStringField() {
		try {
			jdbcTemplate
					.execute("CREATE TABLE " + testTableName + "(" + "id INT NOT NULL," + "data VARCHAR(32) NOT NULL,"
							+ "DatensatzGruppe" + StringUtils.capitalize(testTableName) + " VARCHAR(255) NOT NULL);");

			jdbcTemplate.execute("INSERT INTO " + testTableName + "(" + "id, data, DatensatzGruppe"
					+ StringUtils.capitalize(testTableName) + ")" + "VALUES" + "(1,'some data','userTestGroup');");
			jdbcTemplate.execute("INSERT INTO " + testTableName + "(" + "id, data, DatensatzGruppe"
					+ StringUtils.capitalize(testTableName) + ")" + "VALUES" + "(2,'wrong group','wrongGroup');");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Test
	public void testGetStringField() {
		if (!setUpTestGetStringField())
			fail("could not set up string field");

		// no authorization
		String value = genericSQLDao.getStringField(testTableName, "id", 1, "data", true);
		assertNotNull(value);
		assertEquals("some data", value);
		// authorization
		value = genericSQLDao.getStringField(testTableName, "id", 1, "data", false);
		assertNotNull(value);
		assertEquals("some data", value);
		value = genericSQLDao.getStringField(testTableName, "id", 2, "data", false);
		assertNull(value);
	}

	@Test
	public void testGetStringFieldNoAuth() {
		if (!setUpTestGetStringField())
			fail("could not set up string field");

		// no authorization
		String value = genericSQLDao.getStringField(testTableName, "id", 1, "data");
		assertNotNull(value);
		assertEquals("some data", value);
	}

	public boolean setUpTestGetConnectedEntities() {
		try {
			jdbcTemplate.execute("CREATE TABLE SemanticConnection(" + "Source INT NOT NULL," + "Target INT NOT NULL,"
					+ "ForeignKeyTarget INT NOT NULL," + "TypeTarget VARCHAR(255) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO SemanticConnection(" + "Source, Target, ForeignKeyTarget, TypeTarget)"
					+ "VALUES" + "(1,1,1,'" + testTableName + "');");
			jdbcTemplate.execute("INSERT INTO SemanticConnection(" + "Source, Target, ForeignKeyTarget, TypeTarget)"
					+ "VALUES" + "(1,2,1,'TestTable2');");
			jdbcTemplate.execute("INSERT INTO SemanticConnection(" + "Source, Target, ForeignKeyTarget, TypeTarget)"
					+ "VALUES" + "(1,3,2,'" + testTableName + "');");

			jdbcTemplate.execute("CREATE TABLE " + testTableName + "(" + "PS_" + testTableName + "ID INT NOT NULL,"
					+ "Data VARCHAR(255) NOT NULL," + "DatensatzGruppe" + StringUtils.capitalize(testTableName)
					+ " VARCHAR(255) NOT NULL);");
			jdbcTemplate
					.execute("INSERT INTO " + testTableName + "(" + "PS_" + testTableName + "ID, Data, DatensatzGruppe"
							+ StringUtils.capitalize(testTableName) + ")" + "VALUES" + "(1,'data1','userTestGroup');");
			jdbcTemplate
					.execute("INSERT INTO " + testTableName + "(" + "PS_" + testTableName + "ID, Data, DatensatzGruppe"
							+ StringUtils.capitalize(testTableName) + ")" + "VALUES" + "(2,'data2','userTestGroup');");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Test
	public void testGetConnectedEntities() {
		if (!setUpTestGetConnectedEntities())
			fail("could not set up connected entities");

		final List<Map<String, String>> entities = genericSQLDao.getConnectedEntities(testTableName, 1);
		assertNotNull(entities);
		assertEquals(2, entities.size());

		final Map<String, String> entity = entities.get(0);
		if (entity.get(testTableName + ".Data").equals("data1")) {
			assertEquals("data2", entities.get(1).get(testTableName + ".Data"));
		} else {
			assertEquals("data1", entities.get(1).get(testTableName + ".Data"));
			assertEquals("data2", entity.get(testTableName + ".Data"));
		}
	}

	public boolean setUpTestGetConnectedEntitiesJoint() {
		try {
			jdbcTemplate.execute("CREATE TABLE " + testTableName + "(" + "PS_" + testTableName + "ID INT NOT NULL,"
					+ "Data VARCHAR(255) NOT NULL," + "FS_jointTable1ID INT NOT NULL," + "DatensatzGruppe"
					+ StringUtils.capitalize(testTableName) + " VARCHAR(255) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO " + testTableName + "(" + "PS_" + testTableName
					+ "ID, Data, FS_jointTable1ID, DatensatzGruppe" + StringUtils.capitalize(testTableName) + ")"
					+ "VALUES" + "(4,'data1','13','userTestGroup');");

			jdbcTemplate.execute(
					"CREATE TABLE jointTable1 (" + "PS_jointTable1ID INT NOT NULL," + "jt1Data VARCHAR(255) NOT NULL,"
							+ "FS_jointTable2ID INT NOT NULL," + "DatensatzGruppeJointTable1 VARCHAR(255) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO jointTable1 ("
					+ "PS_jointTable1ID, jt1Data, FS_jointTable2ID, DatensatzGruppeJointTable1)" + "VALUES"
					+ "(13,'jointData1','17','userTestGroup');");

			jdbcTemplate.execute("CREATE TABLE jointTable2 (" + "PS_jointTable2ID INT NOT NULL,"
					+ "jt2Data VARCHAR(255) NOT NULL," + "DatensatzGruppeJointTable2 VARCHAR(255) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO jointTable2 (" + "PS_jointTable2ID, jt2Data, DatensatzGruppeJointTable2)"
					+ "VALUES" + "(17,'jointData2','userTestGroup');");

			jdbcTemplate.execute("CREATE TABLE arachneentityidentification(" + "TableName VARCHAR(16) NOT NULL,"
					+ "ForeignKey INT NOT NULL," + "ArachneEntityID INT NOT NULL," + "isDeleted INT NOT NULL);");
			jdbcTemplate.execute(
					"INSERT INTO arachneentityidentification(" + "TableName, ForeignKey, ArachneEntityID, isDeleted)"
							+ "VALUES" + "('" + testTableName + "',4,1,0);");
		} catch (DataAccessException e) {
			e.printStackTrace();
			System.exit(0);
			return false;
		}
		return true;
	}

	// TODO add more tests as correct grouping and ordering should be tested
	// also
	@Test
	public void testGetConnectedEntitiesJoint() {
		if (!setUpTestGetConnectedEntitiesJoint())
			fail("could not set up connected joint entities");

		JointContextDefinition testDefinition = new JointContextDefinition().setType(testTableName)
				.setConnectFieldParent("PS_" + testTableName + "ID").setOrderBy(testTableName + ".Data")
				.setGroupBy("unused_groupBy").setGroupName("unused_groupName").setId("unused_id")
				.setOrderDescending(false).setDescription("some description")
				.setStandardCIDOCConnectionType("some CIDOC connection type")
				.addJoin("jointTable1", "FS_jointTable1ID", "PS_jointTable1ID")
				.addJoin("jointTable2", "FS_jointTable2ID", "PS_jointTable2ID");

		final List<Map<String, String>> result = genericSQLDao.getConnectedEntitiesJoint(testTableName, 1,
				testDefinition);

		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());

		Map<String, String> record = result.get(0);
		assertNotNull(record);
		assertFalse(record.isEmpty());
		assertEquals(12, record.size());
		assertTrue(record.containsKey("jointTable1.jt1Data"));
		assertEquals("jointData1", record.get("jointTable1.jt1Data"));
		assertTrue(record.containsKey("jointTable2.jt2Data"));
		assertEquals("jointData2", record.get("jointTable2.jt2Data"));

		// test that the jointContextDefinition was not altered
		JointContextDefinition expectedTestDefinition = new JointContextDefinition().setType(testTableName)
				.setConnectFieldParent("PS_" + testTableName + "ID").setOrderBy(testTableName + ".Data")
				.setGroupBy("unused_groupBy").setGroupName("unused_groupName").setId("unused_id")
				.setOrderDescending(false).setDescription("some description")
				.setStandardCIDOCConnectionType("some CIDOC connection type")
				.addJoin("jointTable1", "FS_jointTable1ID", "PS_jointTable1ID")
				.addJoin("jointTable2", "FS_jointTable2ID", "PS_jointTable2ID");

		assertEquals(expectedTestDefinition, testDefinition);
	}

	public boolean setUpTestGetConnectedEntityIds() {
		try {
			jdbcTemplate.execute("CREATE TABLE SemanticConnection(" + "Source INT NOT NULL," + "Target INT NOT NULL,"
					+ "TypeSource VARCHAR(255) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO SemanticConnection(" + "Source, Target, TypeSource)" + "VALUES" + "(1,1,'"
					+ testTableName + "');");
			jdbcTemplate.execute("INSERT INTO SemanticConnection(" + "Source, Target, TypeSource)" + "VALUES"
					+ "(1,2,'TestTable2');");
			jdbcTemplate.execute("INSERT INTO SemanticConnection(" + "Source, Target, TypeSource)" + "VALUES"
					+ "(1,3,'marbilder');");
			jdbcTemplate.execute("INSERT INTO SemanticConnection(" + "Source, Target, TypeSource)" + "VALUES" + "(2,1,'"
					+ testTableName + "');");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Test
	public void testGetConnectedEntityIds() {
		if (!setUpTestGetConnectedEntityIds())
			fail("could not set up connected entity ids");

		final List<Long> ids = genericSQLDao.getConnectedEntityIds(1);
		assertNotNull(ids);
		assertEquals(2, ids.size());
		assertTrue(ids.contains(1L));
		assertTrue(ids.contains(2L));
	}

	public boolean setUpTestGetImageList() {
		try {
			testUserData.createUserTable();
			testUserData.setUpUser(TestUserData.getUser());

			jdbcTemplate.execute("CREATE TABLE marbilder(" + "PS_MARBilderID INT NOT NULL,"
					+ "DateinameMarbilder VARCHAR(16) NOT NULL," + "FS_ObjektID INT NOT NULL,"
					+ "DatensatzGruppeMarbilder varchar(255));");
			jdbcTemplate.execute("INSERT INTO marbilder("
					+ "PS_MARBilderID, DateinameMarbilder, FS_ObjektID, DatensatzGruppeMarbilder)" + "VALUES"
					+ "(1,'test_image1.jpg', 1, 'userTestGroup');");
			jdbcTemplate.execute("INSERT INTO marbilder("
					+ "PS_MARBilderID, DateinameMarbilder, FS_ObjektID, DatensatzGruppeMarbilder)" + "VALUES"
					+ "(2,'test_image2.jpg', 1, 'userTestGroup');");

			jdbcTemplate.execute("CREATE TABLE arachneentityidentification(" + "TableName VARCHAR(16) NOT NULL,"
					+ "ForeignKey INT NOT NULL," + "ArachneEntityID INT NOT NULL);");
			jdbcTemplate.execute("INSERT INTO arachneentityidentification(" + "TableName, ForeignKey, ArachneEntityID)"
					+ "VALUES" + "('marbilder',1,7);");
			jdbcTemplate.execute("INSERT INTO arachneentityidentification(" + "TableName, ForeignKey, ArachneEntityID)"
					+ "VALUES" + "('marbilder',2,8);");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Test
	public void testGetImageList() {
		if (!setUpTestGetImageList())
			fail("could not set up images list");

		List<Image> images = genericSQLDao.getImageList("objekt", 1L);
		assertNotNull(images);
		assertEquals(2, images.size());
		for (final Image image : images) {
			if (image.getImageId() == 7) {
				assertEquals("test_image1", image.getImageSubtitle());
			} else {
				assertEquals("test_image2", image.getImageSubtitle());
			}
		}
	}

	public boolean setUpTestGetLiterature() {
		try {
			jdbcTemplate.execute("CREATE TABLE literaturzitat(" + "PS_literaturzitatID INT NOT NULL,"
					+ "FS_LiteraturID INT NOT NULL," + "FS_" + testTableName + "ID INT NOT NULL,"
					+ "lzdata VARCHAR(32) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO literaturzitat(" + "PS_literaturzitatID, FS_LiteraturID, FS_"
					+ testTableName + "ID, lzdata)" + "VALUES" + "(1,2,5,'some literaturzitat data');");
			jdbcTemplate.execute("INSERT INTO literaturzitat(" + "PS_literaturzitatID, FS_LiteraturID, FS_"
					+ testTableName + "ID, lzdata)" + "VALUES" + "(2,4,5,'some more literaturzitat data');");

			jdbcTemplate.execute("CREATE TABLE literatur(" + "PS_literaturID INT NOT NULL,"
					+ "ZenonID VARCHAR(16) NOT NULL," + "ldata VARCHAR(32) NOT NULL);");
			jdbcTemplate.execute("INSERT INTO literatur(" + "PS_literaturID, ZenonID, ldata)" + "VALUES"
					+ "(2,3,'some literatur data');");
			jdbcTemplate.execute("INSERT INTO literatur(" + "PS_literaturID, ZenonID, ldata)" + "VALUES"
					+ "(4,'','some more literatur data');");

			jdbcTemplate.execute("CREATE TABLE buch(" + "PS_BuchID INT NOT NULL," + "bibid VARCHAR(16) NOT NULL,"
					+ "bdata VARCHAR(32) NOT NULL);");
			jdbcTemplate
					.execute("INSERT INTO buch(" + "PS_BuchID, bibid, bdata)" + "VALUES" + "(4,3,'some buch data');");

			jdbcTemplate.execute("CREATE TABLE arachneentityidentification(" + "TableName VARCHAR(16) NOT NULL,"
					+ "ForeignKey INT NOT NULL," + "ArachneEntityID INT NOT NULL);");
			jdbcTemplate.execute("INSERT INTO arachneentityidentification(" + "TableName, ForeignKey, ArachneEntityID)"
					+ "VALUES" + "('buch',4,6);");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Test
	public void testGetLiteratureValid() {
		if (!setUpTestGetLiterature())
			fail("could not set up literature");

		final List<Map<String, String>> literature = genericSQLDao.getLiterature(testTableName, 5L);
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
	}

	public boolean setUpTestGetBookCoverPage() {
		try {
			jdbcTemplate.execute("CREATE TABLE buchseite(" + "PS_buchseiteID INT NOT NULL," + "seite INT NOT NULL,"
					+ "FS_buchID INT NOT NULL);");
			jdbcTemplate
					.execute("INSERT INTO buchseite(" + "PS_buchseiteID, seite, FS_buchID)" + "VALUES" + "(1,0,1);");
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Test
	public void testGetBookCoverPageValid() {
		if (!setUpTestGetBookCoverPage())
			fail("could not set up book cover page");

		assertEquals(Long.valueOf(1), genericSQLDao.getBookCoverPage(1L));
	}
}
