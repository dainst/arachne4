package de.uni_koeln.arachne.dao.jdbc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.List;
import javax.sql.DataSource;

import de.uni_koeln.arachne.util.sql.CatalogEntryExtended;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.testconfig.EmbeddedDataSourceConfig;
import de.uni_koeln.arachne.testconfig.TestCatalogData;
import de.uni_koeln.arachne.testconfig.TestUserData;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=EmbeddedDataSourceConfig.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TestCatalogEntryDao {

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
		when(userRightsService.getSQL("catalog")).thenReturn(" AND (DatensatzGruppeCatalog = 'userTestGroup' "
				+ "OR DatensatzGruppeCatalog = 'anotherTestGroup')");
		
		jdbcTemplate = new JdbcTemplate(datasource);
		new TestCatalogData(jdbcTemplate)
				.setUpCatalogEntry()
				.setUpCatalog()
				.setUpArachneEntityIdentification();
	}

	@After
	public void tearDown() throws Exception {
		new TestCatalogData(jdbcTemplate)
				.tearDownCatalogEntry()
				.tearDownCatalog()
				.tearDownArachneEntityIdentification();
	}
	
	@Test
	public void testGetByIdLong() {
		final CatalogEntry catalogEntry = catalogEntryDao.getById(1L);
		assertNotNull(catalogEntry);
		assertEquals(Long.valueOf(1), catalogEntry.getId());
		assertEquals(Long.valueOf(1), catalogEntry.getCatalogId());
		assertEquals("root of catalog 1 test label", catalogEntry.getLabel());
		assertTrue(catalogEntry.hasChildren());
		
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(3, children.size());
		assertEquals("child test label No. 1", children.get(0).getLabel());
		assertNull(children.get(0).getText());
		assertEquals(Long.valueOf(666), children.get(0).getArachneEntityId());
		assertTrue(children.get(0).hasChildren());
		
		List<CatalogEntry> childrenLevel1 = children.get(0).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 2", children.get(1).getLabel());
		assertEquals("some text for child No. 2", children.get(1).getText());
		assertNull(children.get(1).getArachneEntityId());
		assertFalse(children.get(1).hasChildren());
		
		childrenLevel1 = children.get(1).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 3", children.get(2).getLabel());
		assertEquals("some text for child No. 3", children.get(2).getText());
		assertNull(children.get(2).getArachneEntityId());
		assertFalse(children.get(2).hasChildren());
		
		childrenLevel1 = children.get(2).getChildren();
		assertNull(childrenLevel1);
	}

	@Test
	public void testGetByIdLongBooleanIntIntFull() {
		final CatalogEntry catalogEntry = catalogEntryDao.getById(1L, true, 0, 0);
		assertNotNull(catalogEntry);
		assertEquals(Long.valueOf(1), catalogEntry.getId());
		assertEquals(Long.valueOf(1), catalogEntry.getCatalogId());
		assertEquals("root of catalog 1 test label", catalogEntry.getLabel());
		assertTrue(catalogEntry.hasChildren());
		
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(3, children.size());
		assertEquals("child test label No. 3", children.get(2).getLabel());
		assertFalse(children.get(2).hasChildren());
		List<CatalogEntry> childrenLevel1 = children.get(2).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 2", children.get(1).getLabel());
		assertFalse(children.get(1).hasChildren());
		childrenLevel1 = children.get(1).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 1", children.get(0).getLabel());
		assertTrue(children.get(0).hasChildren());
		
		childrenLevel1 = children.get(0).getChildren();
		assertNotNull(childrenLevel1);
		assertEquals(3, childrenLevel1.size());
		assertEquals("child test label level 1 No. 1", childrenLevel1.get(0).getLabel());
		assertFalse(childrenLevel1.get(0).hasChildren());
		assertEquals("child test label level 1 No. 2", childrenLevel1.get(1).getLabel());
		assertTrue(childrenLevel1.get(1).hasChildren());
		assertEquals("child test label level 1 No. 3", childrenLevel1.get(2).getLabel());
		assertFalse(childrenLevel1.get(2).hasChildren());
		
		final List<CatalogEntry> childrenLevel2 = childrenLevel1.get(1).getChildren();
		assertNotNull(childrenLevel2);
		assertEquals(1, childrenLevel2.size());
		assertEquals("child test label level 2 No. 1", childrenLevel2.get(0).getLabel());
		assertTrue(children.get(0).hasChildren());
	}
	
	@Test
	public void testGetByIdLongBooleanIntIntLimit() {
		final CatalogEntry catalogEntry = catalogEntryDao.getById(8L, false, 1, 0);
		assertNotNull(catalogEntry);
		assertEquals(Long.valueOf(8), catalogEntry.getId());
		assertEquals(Long.valueOf(1), catalogEntry.getCatalogId());
		assertEquals("child test label No. 1", catalogEntry.getLabel());
		assertTrue(catalogEntry.hasChildren());
		
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(1, children.size());
		assertEquals("child test label level 1 No. 1", children.get(0).getLabel());
		assertFalse(children.get(0).hasChildren());
		
		final List<CatalogEntry> childrenLevel1 = children.get(0).getChildren();
		assertNull(childrenLevel1);
	}
	
	@Test
	public void testGetByIdLongBooleanIntIntOffset() {
		final CatalogEntry catalogEntry = catalogEntryDao.getById(8L, false, -1, 1);
		assertNotNull(catalogEntry);
		assertEquals(Long.valueOf(8), catalogEntry.getId());
		assertEquals(Long.valueOf(1), catalogEntry.getCatalogId());
		assertEquals("child test label No. 1", catalogEntry.getLabel());
		assertTrue(catalogEntry.hasChildren());
		
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(2, children.size());
		assertEquals("child test label level 1 No. 2", children.get(0).getLabel());
		assertTrue(children.get(0).hasChildren());
		
		List<CatalogEntry> childrenLevel1 = children.get(0).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label level 1 No. 3", children.get(1).getLabel());
		assertFalse(children.get(1).hasChildren());
		
		childrenLevel1 = children.get(0).getChildren();
		assertNull(childrenLevel1);
	}
		
	@Test
	public void testGetByIdLongBooleanIntIntLimitOffset() {
		final CatalogEntry catalogEntry = catalogEntryDao.getById(8L, false, 1, 1);
		assertNotNull(catalogEntry);
		assertEquals(Long.valueOf(8), catalogEntry.getId());
		assertEquals(Long.valueOf(1), catalogEntry.getCatalogId());
		assertEquals("child test label No. 1", catalogEntry.getLabel());
		assertTrue(catalogEntry.hasChildren());
		
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(1, children.size());
		assertEquals("child test label level 1 No. 2", children.get(0).getLabel());
		assertTrue(children.get(0).hasChildren());
		
		final List<CatalogEntry> childrenLevel1 = children.get(0).getChildren();
		assertNull(childrenLevel1);		
	}
	
	@Test
	public void testGetByEntityId() {
		final List<CatalogEntry> catalogEntries = catalogEntryDao.getByEntityId(666L);
		assertNotNull(catalogEntries);
		assertEquals(3, catalogEntries.size());

		for (CatalogEntry catalogEntry : catalogEntries) {
			final String label = catalogEntry.getLabel();
			if (catalogEntry.getId() == 8) {
				assertEquals("child test label No. 1", label);
			} else if (catalogEntry.getId() == 3) {
				assertEquals("root of catalog 2 test label", label);
				assertEquals("arachneentity test", catalogEntry.getText());
			}
		}
	}

	@Test
	public void testGetEntryInfoByEntityId() {
		final List<CatalogEntryExtended> catalogEntries = catalogEntryDao.getEntryInfoByEntityId(666L);
		assertNotNull(catalogEntries);
		assertEquals(2, catalogEntries.size());

		for (CatalogEntryExtended catalogEntry : catalogEntries) {
			final String label = catalogEntry.getEntry().getLabel();
			if (catalogEntry.getEntry().getId() == 8) {
				assertEquals("child test label No. 1", label);
				assertEquals("root of catalog 1 test label", catalogEntry.getCatalogTitle());
			} else {
				assertEquals("root of catalog 2 test label", label);
				assertEquals("arachneentity test", catalogEntry.getEntry().getText());
			}
		}
	}
	
	@Test
	public void testGetChildrenByParentId() {
		final List<CatalogEntry> catalogEntries = 
				catalogEntryDao.getChildrenByParentId(8L, catalogEntryDao::mapCatalogEntryNoChilds);
		
		assertNotNull(catalogEntries);
		assertEquals(3, catalogEntries.size());
		assertEquals(Long.valueOf(4), catalogEntries.get(0).getId());
		assertEquals(Long.valueOf(6), catalogEntries.get(1).getId());
		assertEquals(Long.valueOf(5), catalogEntries.get(2).getId());
	}

	@Test
	public void testSaveCatalogEntryNoInteractions() throws Exception {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setCatalogId(2L);
		catalogEntry.setParentId(3L);
		catalogEntry.setArachneEntityId(667L);
		catalogEntry.setLabel("root of catalog 2 test label");
		catalogEntry.setText("some text");
        catalogEntry.setTotalChildren(0);
		
		catalogEntry = catalogEntryDao.saveCatalogEntry(catalogEntry);
		
		final CatalogEntry savedCatalogEntry = catalogEntryDao.getById(catalogEntry.getId());
        assertEquals(catalogEntry.getTotalChildren(), savedCatalogEntry.getTotalChildren());
		assertEquals(catalogEntry, savedCatalogEntry);
		assertEquals(Long.valueOf(2), savedCatalogEntry.getCatalogId());
		assertEquals(Long.valueOf(3), savedCatalogEntry.getParentId());
		assertEquals(Long.valueOf(667), savedCatalogEntry.getArachneEntityId());
		assertEquals("2/3", savedCatalogEntry.getPath());
		assertEquals("root of catalog 2 test label", savedCatalogEntry.getLabel());
		assertEquals("some text", savedCatalogEntry.getText());
	}
	
	@Test
	public void testSaveCatalogEntryNewRoot() throws Exception {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(null);
		catalogEntry.setLabel("root of catalog 1 test label");
		catalogEntry.setText("some text");
        catalogEntry.setTotalChildren(0);
		
		catalogEntry = catalogEntryDao.saveCatalogEntry(catalogEntry);
				
		final CatalogEntry savedCatalogEntry = catalogEntryDao.getById(catalogEntry.getId());
		assertNotNull(savedCatalogEntry);
        assertEquals(catalogEntry.getTotalChildren(), savedCatalogEntry.getTotalChildren());
		assertEquals(catalogEntry, savedCatalogEntry);
		assertEquals("1", savedCatalogEntry.getPath());
		
		assertNull(catalogEntryDao.getById(1L));
		assertNull(catalogEntryDao.getById(2L));
		assertNull(catalogEntryDao.getById(4L));
		assertNull(catalogEntryDao.getById(5L));
		assertNull(catalogEntryDao.getById(6L));
		assertNull(catalogEntryDao.getById(7L));
		assertNull(catalogEntryDao.getById(8L));
	}
	
	@Test
	public void testSaveCatalogEntryNewRootTooLargeIndexParent() throws Exception {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(null);
		catalogEntry.setIndexParent(17);
		catalogEntry.setLabel("root of catalog 1 test label");
		catalogEntry.setText("some text");
        catalogEntry.setTotalChildren(0);
		
		catalogEntry = catalogEntryDao.saveCatalogEntry(catalogEntry);
				
		final CatalogEntry savedCatalogEntry = catalogEntryDao.getById(catalogEntry.getId());
		assertNotNull(savedCatalogEntry);
        assertEquals(catalogEntry.getTotalChildren(), savedCatalogEntry.getTotalChildren());
		assertEquals(catalogEntry, savedCatalogEntry);
		assertEquals(0, savedCatalogEntry.getIndexParent());		
		
		assertNull(catalogEntryDao.getById(1L));
		assertNull(catalogEntryDao.getById(2L));
		assertNull(catalogEntryDao.getById(4L));
		assertNull(catalogEntryDao.getById(5L));
		assertNull(catalogEntryDao.getById(6L));
		assertNull(catalogEntryDao.getById(7L));
		assertNull(catalogEntryDao.getById(8L));
	}
		
	@Test
	public void testSaveCatalogEntryFirstOnThisLevel() throws Exception {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(8L);
		catalogEntry.setArachneEntityId(667L);
		catalogEntry.setLabel("child test label level 1 No. 0");
		catalogEntry.setText("some text");
		
		catalogEntry = catalogEntryDao.saveCatalogEntry(catalogEntry);
		
		final List<CatalogEntry> catalogEntries = catalogEntryDao.getChildrenByParentId(
				catalogEntry.getParentId(), catalogEntryDao::mapCatalogEntryNoChilds);
		assertNotNull(catalogEntries);
		assertEquals(4, catalogEntries.size());
		assertEquals(Long.valueOf(catalogEntry.getId()), catalogEntries.get(0).getId());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals("1/1/8", catalogEntries.get(0).getPath());
		assertEquals(Long.valueOf(4), catalogEntries.get(1).getId());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
		assertEquals(Long.valueOf(6), catalogEntries.get(2).getId());
		assertEquals(2, catalogEntries.get(2).getIndexParent());
		assertEquals(Long.valueOf(5), catalogEntries.get(3).getId());
		assertEquals(3, catalogEntries.get(3).getIndexParent());
	}
	
	@Test
	public void testSaveCatalogEntryThirdOnThisLevel() throws Exception {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(8L);
		catalogEntry.setArachneEntityId(667L);
		catalogEntry.setIndexParent(2);
		catalogEntry.setLabel("child test label level 1 No. 0");
		catalogEntry.setText("some text");
		
		catalogEntry = catalogEntryDao.saveCatalogEntry(catalogEntry);
		
		final List<CatalogEntry> catalogEntries = catalogEntryDao.getChildrenByParentId(
				catalogEntry.getParentId(), catalogEntryDao::mapCatalogEntryNoChilds);
		assertNotNull(catalogEntries);
		assertEquals(4, catalogEntries.size());
		assertEquals(Long.valueOf(4), catalogEntries.get(0).getId());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals(Long.valueOf(6), catalogEntries.get(1).getId());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
		assertEquals(Long.valueOf(catalogEntry.getId()), catalogEntries.get(2).getId());
		assertEquals(2, catalogEntries.get(2).getIndexParent());
		assertEquals(Long.valueOf(5), catalogEntries.get(3).getId());
		assertEquals(3, catalogEntries.get(3).getIndexParent());
	}
	
	@Test
	public void testSaveCatalogEntryLastOnThisLevel() throws Exception {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(8L);
		catalogEntry.setArachneEntityId(667L);
		catalogEntry.setIndexParent(3);
		catalogEntry.setLabel("child test label level 1 No. 0");
		catalogEntry.setText("some text");
		
		catalogEntry = catalogEntryDao.saveCatalogEntry(catalogEntry);
		
		final List<CatalogEntry> catalogEntries = catalogEntryDao.getChildrenByParentId(
				catalogEntry.getParentId(), catalogEntryDao::mapCatalogEntryNoChilds);
		assertNotNull(catalogEntries);
		assertEquals(4, catalogEntries.size());
		assertEquals(Long.valueOf(4), catalogEntries.get(0).getId());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals(Long.valueOf(6), catalogEntries.get(1).getId());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
		assertEquals(Long.valueOf(5), catalogEntries.get(2).getId());
		assertEquals(2, catalogEntries.get(2).getIndexParent());
		assertEquals(Long.valueOf(catalogEntry.getId()), catalogEntries.get(3).getId());
		assertEquals(3, catalogEntries.get(3).getIndexParent());
	}
	
	@Test
	public void testSaveCatalogEntryLastOnThisLevelTooLargeIndexParent() throws Exception {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(8L);
		catalogEntry.setArachneEntityId(667L);
		catalogEntry.setIndexParent(17);
		catalogEntry.setLabel("child test label level 1 No. 0");
		catalogEntry.setText("some text");
		
		catalogEntry = catalogEntryDao.saveCatalogEntry(catalogEntry);
		
		final List<CatalogEntry> catalogEntries = catalogEntryDao.getChildrenByParentId(
				catalogEntry.getParentId(), catalogEntryDao::mapCatalogEntryNoChilds);
		assertNotNull(catalogEntries);
		assertEquals(4, catalogEntries.size());
		assertEquals(Long.valueOf(4), catalogEntries.get(0).getId());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals(Long.valueOf(6), catalogEntries.get(1).getId());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
		assertEquals(Long.valueOf(5), catalogEntries.get(2).getId());
		assertEquals(2, catalogEntries.get(2).getIndexParent());
		assertEquals(Long.valueOf(catalogEntry.getId()), catalogEntries.get(3).getId());
		assertEquals(3, catalogEntries.get(3).getIndexParent());
	}
	
	@Test
	public void testSaveCatalogEntryInvalidCatalogDoesNotExist() throws Exception {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setCatalogId(4L);
		catalogEntry.setParentId(8L);
		catalogEntry.setArachneEntityId(667L);
		catalogEntry.setIndexParent(17);
		catalogEntry.setLabel("cannot create this entry");
		catalogEntry.setText("catalog does not exist");
		
		catalogEntry = catalogEntryDao.saveCatalogEntry(catalogEntry);
		
		assertNull(catalogEntry);
	}
	
	@Test
	public void testSaveCatalogEntryInvalidArachneEntiyDoesNotExist() throws Exception {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(8L);
		catalogEntry.setArachneEntityId(668L);
		catalogEntry.setIndexParent(17);
		catalogEntry.setLabel("cannot create this entry");
		catalogEntry.setText("arachne entity does not exist");
		
		catalogEntry = catalogEntryDao.saveCatalogEntry(catalogEntry);
		
		assertNull(catalogEntry);
	}
	
	@Test
	public void testUpdateCatalogEntryNoInteractions() {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(8L);
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(1L);
		catalogEntry.setArachneEntityId(667L);
		catalogEntry.setLabel("some new label");
		catalogEntry.setText("some text");
        catalogEntry.setTotalChildren(3);
		catalogEntry.setAllSuccessors(3);

		catalogEntry = catalogEntryDao.updateCatalogEntry(catalogEntry);

        assertEquals(catalogEntry.getTotalChildren(), catalogEntryDao.getById(8L).getTotalChildren());
		assertEquals(catalogEntry, catalogEntryDao.getById(8L));
		assertEquals("1/1", catalogEntry.getPath());
	}
	
	@Test
	public void testUpdateCatalogEntryRoot() {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(1L);
		catalogEntry.setCatalogId(1L);
		catalogEntry.setLabel("updated root of catalog 1 test label");
		catalogEntry.setText("some text");
        catalogEntry.setTotalChildren(3);
        catalogEntry.setAllSuccessors(5);

		catalogEntry = catalogEntryDao.updateCatalogEntry(catalogEntry);

        assertEquals(catalogEntry.getTotalChildren(), catalogEntryDao.getById(1L).getTotalChildren());
		assertEquals(catalogEntry, catalogEntryDao.getById(1L));
		assertEquals("1", catalogEntry.getPath());
	}
	
	@Test
	public void testUpdateCatalogEntryUpdateIndexParent() {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(4L);
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(8L);
		catalogEntry.setIndexParent(1);
		catalogEntry.setLabel("child test label level 1 No. 1");
        catalogEntry.setTotalChildren(0);

        CatalogEntry savedCatalogEntry = catalogEntryDao.updateCatalogEntry(catalogEntry);

        assertEquals(catalogEntry.getTotalChildren(), catalogEntryDao.getById(4L).getTotalChildren());
		assertEquals(savedCatalogEntry, catalogEntryDao.getById(4L));
		
		final List<CatalogEntry> catalogEntries = catalogEntryDao.getById(catalogEntry.getParentId()).getChildren();
		assertEquals(3, catalogEntries.size());
		assertEquals(Long.valueOf(6), catalogEntries.get(0).getId());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals(Long.valueOf(4), catalogEntries.get(1).getId());
		assertEquals("1/8", catalogEntries.get(1).getPath());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
		assertEquals(Long.valueOf(5), catalogEntries.get(2).getId());
		assertEquals(2, catalogEntries.get(2).getIndexParent());
	}
	
	@Test
	public void testUpdateCatalogEntryUpdateTooLargeIndexParent() {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(4L);
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(8L);
		catalogEntry.setIndexParent(1000);
		catalogEntry.setLabel("child test label level 1 No. 1");
        catalogEntry.setTotalChildren(0);

        CatalogEntry savedCatalogEntry = catalogEntryDao.updateCatalogEntry(catalogEntry);

        assertEquals(catalogEntry.getTotalChildren(), catalogEntryDao.getById(4L).getTotalChildren());
        assertEquals(savedCatalogEntry, catalogEntryDao.getById(4L));
		
		final List<CatalogEntry> catalogEntries = catalogEntryDao.getById(catalogEntry.getParentId()).getChildren();
		assertEquals(3, catalogEntries.size());
		assertEquals(Long.valueOf(6), catalogEntries.get(0).getId());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals(Long.valueOf(5), catalogEntries.get(1).getId());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
		assertEquals(Long.valueOf(4), catalogEntries.get(2).getId());
		assertEquals(2, catalogEntries.get(2).getIndexParent());
	}
	
	@Test
	public void testUpdateCatalogEntryUpdateParentId() {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(4L);
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(1L);
		catalogEntry.setIndexParent(0);
		catalogEntry.setLabel("child test label level 1 No. 1");
        catalogEntry.setTotalChildren(0);

        CatalogEntry savedCatalogEntry = catalogEntryDao.updateCatalogEntry(catalogEntry);

        assertEquals(catalogEntry.getTotalChildren(), catalogEntryDao.getById(4L).getTotalChildren());
        assertEquals(savedCatalogEntry, catalogEntryDao.getById(4L));
		
		List<CatalogEntry> catalogEntries = catalogEntryDao.getById(catalogEntry.getParentId()).getChildren();
		assertEquals(4, catalogEntries.size());
		assertEquals(Long.valueOf(4), catalogEntries.get(0).getId());
		assertEquals("1/1", catalogEntries.get(0).getPath());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals(Long.valueOf(8), catalogEntries.get(1).getId());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
		assertEquals(Long.valueOf(2), catalogEntries.get(2).getId());
		assertEquals(2, catalogEntries.get(2).getIndexParent());
		assertEquals(Long.valueOf(9), catalogEntries.get(3).getId());
		assertEquals(3, catalogEntries.get(3).getIndexParent());
		
		catalogEntries = catalogEntryDao.getById(8L).getChildren();
		assertEquals(2, catalogEntries.size());
		assertEquals(Long.valueOf(6), catalogEntries.get(0).getId());
		assertEquals(Long.valueOf(5), catalogEntries.get(1).getId());
	}
	
	@Test
	public void testUpdateCatalogEntryUpdateParentIdInvalidParentId() {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(4L);
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(17L);
		catalogEntry.setIndexParent(0);
		catalogEntry.setLabel("child test label level 1 No. 1");
        catalogEntry.setTotalChildren(0);
		
		final CatalogEntry savedCatalogEntry = catalogEntryDao.updateCatalogEntry(catalogEntry);
		
		assertNull(savedCatalogEntry);
		assertNotEquals(catalogEntry, catalogEntryDao.getById(4L));
	}
	
	@Test
	public void testUpdateCatalogEntryUpdateParentIdInvalidParentIsPartOfDifferentCatalog() {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(4L);
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(3L);
		catalogEntry.setIndexParent(0);
		catalogEntry.setLabel("child test label level 1 No. 1");
        catalogEntry.setTotalChildren(0);
		
		final CatalogEntry savedCatalogEntry = catalogEntryDao.updateCatalogEntry(catalogEntry);
		
		assertNull(savedCatalogEntry);
		assertNotEquals(catalogEntry, catalogEntryDao.getById(4L));
	}
	
	@Test
	public void testUpdateCatalogEntryUpdateParentIdAndIndexParent() {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(6L);
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(1L);
		catalogEntry.setIndexParent(2);
		catalogEntry.setLabel("child test label level 1 No. 2");
        catalogEntry.setTotalChildren(1);
        catalogEntry.setAllSuccessors(1);

        CatalogEntry savedCatalogEntry = catalogEntryDao.updateCatalogEntry(catalogEntry);

        assertEquals(catalogEntry.getTotalChildren(), catalogEntryDao.getById(6L).getTotalChildren());
        assertEquals(savedCatalogEntry, catalogEntryDao.getById(6L));
		
		List<CatalogEntry> catalogEntries = catalogEntryDao.getById(catalogEntry.getParentId()).getChildren();
		assertEquals(4, catalogEntries.size());
		assertEquals(Long.valueOf(8), catalogEntries.get(0).getId());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals(Long.valueOf(2), catalogEntries.get(1).getId());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
		assertEquals(Long.valueOf(6), catalogEntries.get(2).getId());
		assertEquals("1/1", catalogEntries.get(0).getPath());
		assertEquals(2, catalogEntries.get(2).getIndexParent());
		assertEquals(Long.valueOf(9), catalogEntries.get(3).getId());
		assertEquals(3, catalogEntries.get(3).getIndexParent());
		
		catalogEntries = catalogEntryDao.getById(8L).getChildren();
		assertEquals(2, catalogEntries.size());
		assertEquals(Long.valueOf(4), catalogEntries.get(0).getId());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals(Long.valueOf(5), catalogEntries.get(1).getId());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
	}
	
	@Test
	public void testUpdateCatalogEntryUpdateParentIdAndIndexParentTooLargeIndexParent() {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(6L);
		catalogEntry.setCatalogId(1L);
		catalogEntry.setParentId(1L);
		catalogEntry.setIndexParent(17);
		catalogEntry.setLabel("child test label level 1 No. 2");
		catalogEntry.setTotalChildren(1);
        catalogEntry.setAllSuccessors(1);

        CatalogEntry savedCatalogEntry = catalogEntryDao.updateCatalogEntry(catalogEntry);

        assertEquals(catalogEntry.getTotalChildren(), catalogEntryDao.getById(6L).getTotalChildren());
        assertEquals(savedCatalogEntry, catalogEntryDao.getById(6L));
		
		List<CatalogEntry> catalogEntries = catalogEntryDao.getById(catalogEntry.getParentId()).getChildren();
		assertEquals(4, catalogEntries.size());
		assertEquals(Long.valueOf(8), catalogEntries.get(0).getId());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals(Long.valueOf(2), catalogEntries.get(1).getId());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
		assertEquals(Long.valueOf(9), catalogEntries.get(2).getId());
		assertEquals(2, catalogEntries.get(2).getIndexParent());
		assertEquals(Long.valueOf(6), catalogEntries.get(3).getId());
		assertEquals(3, catalogEntries.get(3).getIndexParent());
		
		catalogEntries = catalogEntryDao.getById(8L).getChildren();
		assertEquals(2, catalogEntries.size());
		assertEquals(Long.valueOf(4), catalogEntries.get(0).getId());
		assertEquals(0, catalogEntries.get(0).getIndexParent());
		assertEquals(Long.valueOf(5), catalogEntries.get(1).getId());
		assertEquals(1, catalogEntries.get(1).getIndexParent());
	}
	
	@Test
	public void testUpdateCatalogEntryUpdateCatalogIdInvalid() {
		CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(5L);
		catalogEntry.setCatalogId(2L);
		catalogEntry.setParentId(8L);
		catalogEntry.setIndexParent(2);
		catalogEntry.setLabel("child test label level 1 No. 2");
        catalogEntry.setTotalChildren(0);
		
		catalogEntry = catalogEntryDao.updateCatalogEntry(catalogEntry);
		assertNull(catalogEntry);
		assertNotEquals(catalogEntry, catalogEntryDao.getById(5L));
	}
	
	@Test
	public void testDelete() {
		assertTrue(catalogEntryDao.delete(8L));
		assertNull(catalogEntryDao.getById(8L));
		assertNull(catalogEntryDao.getById(4L));
		assertNull(catalogEntryDao.getById(5L));
		assertNull(catalogEntryDao.getById(6L));
		assertNull(catalogEntryDao.getById(7L));
	}
}
