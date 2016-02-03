package de.uni_koeln.arachne.dao.jdbc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.testconfig.TestCatalogData;
import de.uni_koeln.arachne.testconfig.TestUserData;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:unittest-context.xml"})
public class TestCatalogDao {

	@Autowired
	@InjectMocks
	private CatalogDao catalogDao;
	
	@Autowired
	@InjectMocks
	private CatalogEntryDao catalogEntryDao;
	
	@Mock
	private UserRightsService userRightsService;
	
	@Autowired
	private DataSource datasource;
	
	private JdbcTemplate jdbcTemplate;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(userRightsService.isSignedInUser()).thenReturn(true);
		when(userRightsService.getCurrentUser()).thenReturn(TestUserData.getUser());
		when(userRightsService.getSQL("Catalog")).thenReturn("DatensatzGruppeCatalog = 'userTestGroup'");
		
		jdbcTemplate = new JdbcTemplate(datasource);
		new TestCatalogData(jdbcTemplate)
				.setUpCatalogEntry()
				.setUpCatalog()
				.setUpArachneEntityIdentification();
		//new TestUserData(jdbcTemplate).createUserTable();
	}

	@After
	public void tearDown() throws Exception {
		new TestCatalogData(jdbcTemplate)
				.tearDownCatalogEntry()
				.tearDownCatalog()
				.tearDownArachneEntityIdentification();
		//new TestUserData(jdbcTemplate).dropUserTable();
	}
	
	@Test
	public void testGetByIdLong() {
		final Catalog catalog = catalogDao.getById(1);
		assertNotNull(catalog);
		assertEquals(Long.valueOf(1), catalog.getId());
		assertTrue(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		final Set<Long> userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		final CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 1 test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> childEntries = rootEntry.getChildren();
		assertNotNull(childEntries);
		assertEquals(3, childEntries.size());
		assertEquals("child test label No. 1", childEntries.get(0).getLabel());
		assertTrue(childEntries.get(0).isHasChildren());
		List<CatalogEntry> childrenLevel1 = childEntries.get(0).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 2", childEntries.get(1).getLabel());
		assertFalse(childEntries.get(1).isHasChildren());
		childrenLevel1 = childEntries.get(1).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 3", childEntries.get(2).getLabel());
		assertFalse(childEntries.get(2).isHasChildren());
		childrenLevel1 = childEntries.get(2).getChildren();
		assertNull(childrenLevel1);
	}
	
	@Test
	public void testGetByIdLongBooleanIntIntFull() {
		final Catalog catalog = catalogDao.getById(1, true, 0, 0);
		assertNotNull(catalog);
		assertEquals(Long.valueOf(1), catalog.getId());
		assertTrue(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		final Set<Long> userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		final CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 1 test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> children = rootEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(3, children.size());
		assertEquals("child test label No. 3", children.get(2).getLabel());
		assertFalse(children.get(2).isHasChildren());
		assertEquals("child test label No. 2", children.get(1).getLabel());
		assertFalse(children.get(1).isHasChildren());
		List<CatalogEntry> childrenLevel1 = children.get(1).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 1", children.get(0).getLabel());
		assertTrue(children.get(0).isHasChildren());
		
		childrenLevel1 = children.get(0).getChildren();
		assertNotNull(childrenLevel1);
		assertEquals(3, childrenLevel1.size());
		assertEquals("child test label level 1 No. 1", childrenLevel1.get(0).getLabel());
		assertFalse(childrenLevel1.get(0).isHasChildren());
		assertEquals("child test label level 1 No. 2", childrenLevel1.get(1).getLabel());
		assertTrue(childrenLevel1.get(1).isHasChildren());
		assertEquals("child test label level 1 No. 3", childrenLevel1.get(2).getLabel());
		assertFalse(childrenLevel1.get(2).isHasChildren());
		
		final List<CatalogEntry> childrenLevel2 = childrenLevel1.get(1).getChildren();
		assertNotNull(childrenLevel2);
		assertEquals(1, childrenLevel2.size());
		assertEquals("child test label level 2 No. 1", childrenLevel2.get(0).getLabel());
		assertTrue(children.get(0).isHasChildren());
	}
	
	@Test
	public void testGetByIdLongBooleanIntIntLimit() {
		final Catalog catalog = catalogDao.getById(1, false, 1, 0);
		assertNotNull(catalog);
		assertEquals(Long.valueOf(1), catalog.getId());
		assertTrue(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		final Set<Long> userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		final CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 1 test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> children = rootEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(1, children.size());
		assertEquals("child test label No. 1", children.get(0).getLabel());
		assertTrue(children.get(0).isHasChildren());
		assertNull(children.get(0).getChildren());
	}
	
	@Test
	public void testGetByIdLongBooleanIntIntOffset() {
		final Catalog catalog = catalogDao.getById(1, false, 0, 1);
		assertNotNull(catalog);
		assertEquals(Long.valueOf(1), catalog.getId());
		assertTrue(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		final Set<Long> userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		final CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 1 test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> children = rootEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(2, children.size());
		assertEquals("child test label No. 2", children.get(0).getLabel());
		assertFalse(children.get(0).isHasChildren());
		assertNull(children.get(0).getChildren());
		assertEquals("child test label No. 3", children.get(1).getLabel());
		assertFalse(children.get(1).isHasChildren());
		assertNull(children.get(1).getChildren());
	}
	
	@Test
	public void testGetByIdLongBooleanIntIntLimitAndOffset() {
		final Catalog catalog = catalogDao.getById(1, false, 1, 1);
		assertNotNull(catalog);
		assertEquals(Long.valueOf(1), catalog.getId());
		assertTrue(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		final Set<Long> userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		final CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 1 test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> children = rootEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(1, children.size());
		assertEquals("child test label No. 2", children.get(0).getLabel());
		assertFalse(children.get(0).isHasChildren());
		assertNull(children.get(0).getChildren());
	}
	
	@Test
	public void testGetByUserIdLong() {
		final List<Catalog> catalogs = catalogDao.getByUserId(3, false);
		assertNotNull(catalogs);
		assertEquals(2, catalogs.size());
		
		Catalog catalog = catalogs.get(0);
		assertEquals(Long.valueOf(1), catalog.getId());
		assertTrue(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		Set<Long> userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 1 test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> childEntries = rootEntry.getChildren();
		assertNotNull(childEntries);
		assertEquals(3, childEntries.size());
		assertEquals("child test label No. 1", childEntries.get(0).getLabel());
		assertTrue(childEntries.get(0).isHasChildren());
		List<CatalogEntry> childrenLevel1 = childEntries.get(0).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 2", childEntries.get(1).getLabel());
		assertFalse(childEntries.get(1).isHasChildren());
		childrenLevel1 = childEntries.get(1).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 3", childEntries.get(2).getLabel());
		assertFalse(childEntries.get(2).isHasChildren());
		childrenLevel1 = childEntries.get(2).getChildren();
		assertNull(childrenLevel1);
		
		catalog = catalogs.get(1);
		assertEquals(Long.valueOf(2), catalog.getId());
		assertFalse(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 2 test label", rootEntry.getLabel());
		assertFalse(rootEntry.isHasChildren());
	}
	
	@Test
	public void testGetByUserIdLongBooleanIntIntFull() {
		final List<Catalog> catalogs = catalogDao.getByUserId(3, true, 0, 0);
		assertNotNull(catalogs);
		assertEquals(2, catalogs.size());
		
		Catalog catalog = catalogs.get(0);
		assertEquals(Long.valueOf(1), catalog.getId());
		assertTrue(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		Set<Long> userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 1 test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> children = rootEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(3, children.size());
		assertEquals("child test label No. 3", children.get(2).getLabel());
		assertFalse(children.get(2).isHasChildren());
		assertEquals("child test label No. 2", children.get(1).getLabel());
		assertFalse(children.get(1).isHasChildren());
		List<CatalogEntry> childrenLevel1 = children.get(1).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 1", children.get(0).getLabel());
		assertTrue(children.get(0).isHasChildren());
		
		childrenLevel1 = children.get(0).getChildren();
		assertNotNull(childrenLevel1);
		assertEquals(3, childrenLevel1.size());
		assertEquals("child test label level 1 No. 1", childrenLevel1.get(0).getLabel());
		assertFalse(childrenLevel1.get(0).isHasChildren());
		assertEquals("child test label level 1 No. 2", childrenLevel1.get(1).getLabel());
		assertTrue(childrenLevel1.get(1).isHasChildren());
		assertEquals("child test label level 1 No. 3", childrenLevel1.get(2).getLabel());
		assertFalse(childrenLevel1.get(2).isHasChildren());
		
		final List<CatalogEntry> childrenLevel2 = childrenLevel1.get(1).getChildren();
		assertNotNull(childrenLevel2);
		assertEquals(1, childrenLevel2.size());
		assertEquals("child test label level 2 No. 1", childrenLevel2.get(0).getLabel());
		assertTrue(children.get(0).isHasChildren());
		
		catalog = catalogs.get(1);
		assertEquals(Long.valueOf(2), catalog.getId());
		assertFalse(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 2 test label", rootEntry.getLabel());
		assertFalse(rootEntry.isHasChildren());
	}
	
	@Test
	public void testGetByUserIdLongBooleanIntIntLimit() {
		final List<Catalog> catalogs = catalogDao.getByUserId(3, false, 1, 0);
		assertNotNull(catalogs);
		assertEquals(2, catalogs.size());
		
		Catalog catalog = catalogs.get(0);
		assertEquals(Long.valueOf(1), catalog.getId());
		assertTrue(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		Set<Long> userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 1 test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> childEntries = rootEntry.getChildren();
		assertNotNull(childEntries);
		assertEquals(1, childEntries.size());
		assertEquals("child test label No. 1", childEntries.get(0).getLabel());
		assertTrue(childEntries.get(0).isHasChildren());
		List<CatalogEntry> childrenLevel1 = childEntries.get(0).getChildren();
		assertNull(childrenLevel1);
				
		catalog = catalogs.get(1);
		assertEquals(Long.valueOf(2), catalog.getId());
		assertFalse(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 2 test label", rootEntry.getLabel());
		assertFalse(rootEntry.isHasChildren());
	}
	
	@Test
	public void testGetByUserIdLongBooleanIntIntOffset() {
		final List<Catalog> catalogs = catalogDao.getByUserId(3, false, 0, 1);
		assertNotNull(catalogs);
		assertEquals(2, catalogs.size());
		
		Catalog catalog = catalogs.get(0);
		assertEquals(Long.valueOf(1), catalog.getId());
		assertTrue(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		Set<Long> userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 1 test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> childEntries = rootEntry.getChildren();
		assertNotNull(childEntries);
		assertEquals(2, childEntries.size());
		assertEquals("child test label No. 2", childEntries.get(0).getLabel());
		assertFalse(childEntries.get(0).isHasChildren());
		List<CatalogEntry> childrenLevel1 = childEntries.get(0).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 3", childEntries.get(1).getLabel());
		assertFalse(childEntries.get(1).isHasChildren());
		childrenLevel1 = childEntries.get(1).getChildren();
		assertNull(childrenLevel1);
		
		catalog = catalogs.get(1);
		assertEquals(Long.valueOf(2), catalog.getId());
		assertFalse(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 2 test label", rootEntry.getLabel());
		assertFalse(rootEntry.isHasChildren());
	}
	
	@Test
	public void testGetByUserIdLongBooleanIntIntLimitAndOffset() {
		final List<Catalog> catalogs = catalogDao.getByUserId(3, false, 1, 1);
		assertNotNull(catalogs);
		assertEquals(2, catalogs.size());
		
		Catalog catalog = catalogs.get(0);
		assertEquals(Long.valueOf(1), catalog.getId());
		assertTrue(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		Set<Long> userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 1 test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> childEntries = rootEntry.getChildren();
		assertNotNull(childEntries);
		assertEquals(1, childEntries.size());
		assertEquals("child test label No. 2", childEntries.get(0).getLabel());
		assertFalse(childEntries.get(0).isHasChildren());
		List<CatalogEntry> childrenLevel1 = childEntries.get(0).getChildren();
		assertNull(childrenLevel1);
				
		catalog = catalogs.get(1);
		assertEquals(Long.valueOf(2), catalog.getId());
		assertFalse(catalog.isPublic());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("userTestGroup", catalog.getDatasetGroup());
		userIds = catalog.getUserIds();
		assertNotNull(userIds);
		assertFalse(userIds.isEmpty());
		
		rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root of catalog 2 test label", rootEntry.getLabel());
		assertFalse(rootEntry.isHasChildren());
	}
	
	@Test
	public void testGetPublicCatalogIdsAndPathsByEntityId() {
		final List<Object[]> catalogData = catalogDao.getPublicCatalogIdsAndPathsByEntityId(666L);
		assertNotNull(catalogData);
		assertEquals(1, catalogData.size());
		assertEquals(Long.valueOf(1), (Long)catalogData.get(0)[0]);
		assertEquals("1/1", (String)catalogData.get(0)[1]);
	}
	
	@Test
	public void testGetPrivateCatalogIdsByEntityId() {
		final List<Long> catalogIds = catalogDao.getPrivateCatalogIdsByEntityId(666L);
		assertNotNull(catalogIds);
		assertEquals(1, catalogIds.size());
		assertEquals(Long.valueOf(2), catalogIds.get(0));
	}
	
	@Test
	public void testSaveCatalog() {
		final User user = TestUserData.getUser();
		
		CatalogEntry root = new CatalogEntry();
		root.setLabel("new root label");
		root.setText("some new text");
		
		Catalog catalog = new Catalog();
		catalog.setAuthor(user.getFirstname() + " " + user.getLastname());
		catalog.setDatasetGroup(user.getDatasetGroups().toArray(new DatasetGroup[0])[0].getName());
		catalog.setUserIds(new HashSet<Long>(Arrays.asList(3L)));
		catalog.setRoot(root);
		
		catalog = catalogDao.saveCatalog(catalog);
		assertNotNull(catalog);
		
		final Catalog savedCatalog = catalogDao.getById(catalog.getId()); 
		assertEquals(catalog, savedCatalog);
		
		root = catalog.getRoot();
		assertNotNull(root);
		assertNotNull(root.getId());
		assertEquals(catalog.getRoot(), catalogEntryDao.getById(catalog.getRoot().getId()));
		assertNull(root.getParentId());
		assertEquals(catalog.getId().toString(), root.getPath());
		assertEquals(0, root.getIndexParent());
		assertEquals("new root label", root.getLabel());
		assertEquals("some new text", root.getText());
	}
	
	@Test
	public void testSaveCatalogWithArachneEntityId() {
		final User user = TestUserData.getUser();
		
		CatalogEntry root = new CatalogEntry();
		root.setArachneEntityId(666L);
		root.setLabel("new root label");
		root.setText("some new text");
		
		Catalog catalog = new Catalog();
		catalog.setAuthor(user.getFirstname() + " " + user.getLastname());
		catalog.setDatasetGroup(user.getDatasetGroups().toArray(new DatasetGroup[0])[0].getName());
		catalog.setUserIds(new HashSet<Long>(Arrays.asList(3L)));
		catalog.setRoot(root);
		
		catalog = catalogDao.saveCatalog(catalog);
		assertNotNull(catalog);
		
		final Catalog savedCatalog = catalogDao.getById(catalog.getId()); 
		assertEquals(catalog, savedCatalog);
		
		root = catalog.getRoot();
		assertNotNull(root);
		assertNotNull(root.getId());
		assertEquals(catalog.getRoot(), catalogEntryDao.getById(catalog.getRoot().getId()));
		assertNull(root.getParentId());
		assertEquals(catalog.getId().toString(), root.getPath());
		assertEquals(0, root.getIndexParent());
		assertEquals("new root label", root.getLabel());
		assertEquals("some new text", root.getText());
	}
	
	@Test
	public void testSaveCatalogInvalidCatalogId() {
		final User user = TestUserData.getUser();
		
		CatalogEntry root = new CatalogEntry();
		root.setLabel("new root label");
		root.setText("some new text");
		
		Catalog catalog = new Catalog();
		catalog.setId(17L);
		catalog.setAuthor(user.getFirstname() + " " + user.getLastname());
		catalog.setDatasetGroup(user.getDatasetGroups().toArray(new DatasetGroup[0])[0].getName());
		catalog.setUserIds(new HashSet<Long>(Arrays.asList(3L)));
		catalog.setRoot(root);
		
		catalog = catalogDao.saveCatalog(catalog);
		assertNull(catalog);
	}
	
	@Test
	public void testSaveCatalogInvalidRootId() {
		final User user = TestUserData.getUser();
		
		CatalogEntry root = new CatalogEntry();
		root.setId(17L);
		root.setLabel("new root label");
		root.setText("some new text");
		
		Catalog catalog = new Catalog();
		catalog.setAuthor(user.getFirstname() + " " + user.getLastname());
		catalog.setDatasetGroup(user.getDatasetGroups().toArray(new DatasetGroup[0])[0].getName());
		catalog.setUserIds(new HashSet<Long>(Arrays.asList(3L)));
		catalog.setRoot(root);
		
		catalog = catalogDao.saveCatalog(catalog);
		assertNull(catalog);
	}
	
	@Test
	public void testSaveCatalogInvalidArachneEntityId() {
		final User user = TestUserData.getUser();
		
		CatalogEntry root = new CatalogEntry();
		root.setArachneEntityId(17L);
		root.setLabel("new root label");
		root.setText("some new text");
		
		Catalog catalog = new Catalog();
		catalog.setAuthor(user.getFirstname() + " " + user.getLastname());
		catalog.setDatasetGroup(user.getDatasetGroups().toArray(new DatasetGroup[0])[0].getName());
		catalog.setUserIds(new HashSet<Long>(Arrays.asList(3L)));
		catalog.setRoot(root);
		
		catalog = catalogDao.saveCatalog(catalog);
		assertNull(catalog);
	}
	
	@Test
	public void testDeleteCatalog() {
		assertTrue(catalogDao.deleteCatalog(1L));
		
		assertNull(catalogDao.getById(1L));
		
		assertNull(catalogEntryDao.getById(1L));
		assertNull(catalogEntryDao.getById(2L));
		assertNull(catalogEntryDao.getById(4L));
		assertNull(catalogEntryDao.getById(5L));
		assertNull(catalogEntryDao.getById(6L));
		assertNull(catalogEntryDao.getById(7L));
		assertNull(catalogEntryDao.getById(8L));
		assertNull(catalogEntryDao.getById(9L));
	}
}
