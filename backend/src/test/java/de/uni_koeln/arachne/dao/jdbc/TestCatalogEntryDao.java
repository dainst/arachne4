package de.uni_koeln.arachne.dao.jdbc;

import static org.junit.Assert.*;

import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import de.uni_koeln.arachne.dao.jdbc.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.testconfig.TestDataCatalog;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:unittest-context.xml"})
public class TestCatalogEntryDao {

	@Autowired
	private CatalogEntryDao catalogEntryDao;
	
	@Autowired
	private DataSource datasource;
	
	private JdbcTemplate jdbcTemplate;
	
	@Before
	public void setUp() throws Exception {
		jdbcTemplate = new JdbcTemplate(datasource);
		new TestDataCatalog(jdbcTemplate).setUpCatalogEntry();
	}

	@After
	public void tearDown() throws Exception {
		new TestDataCatalog(jdbcTemplate).tearDownCatalogEntry();
	}
	
	@Test
	public void testGetByIdLong() {
		final CatalogEntry catalogEntry = catalogEntryDao.getById(1L);
		assertNotNull(catalogEntry);
		assertEquals(Long.valueOf(1), catalogEntry.getId());
		assertEquals(1L, catalogEntry.getCatalogId());
		assertEquals("root test label", catalogEntry.getLabel());
		assertTrue(catalogEntry.isHasChildren());
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(2, children.size());
		assertEquals("child test label No. 1", children.get(0).getLabel());
		assertTrue(children.get(0).isHasChildren());
		List<CatalogEntry> childrenLevel1 = children.get(0).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 2", children.get(1).getLabel());
		assertFalse(children.get(1).isHasChildren());
		childrenLevel1 = children.get(1).getChildren();
		assertNull(childrenLevel1);
	}

	@Test
	public void testGetByIdLongBooleanIntIntFull() {
		final CatalogEntry catalogEntry = catalogEntryDao.getById(1L, true, 0, 0);
		assertNotNull(catalogEntry);
		assertEquals(Long.valueOf(1), catalogEntry.getId());
		assertEquals(1L, catalogEntry.getCatalogId());
		assertEquals("root test label", catalogEntry.getLabel());
		assertTrue(catalogEntry.isHasChildren());
		
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(2, children.size());
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
		final CatalogEntry catalogEntry = catalogEntryDao.getById(8L, false, 1, 0);
		assertNotNull(catalogEntry);
		assertEquals(Long.valueOf(8), catalogEntry.getId());
		assertEquals(1L, catalogEntry.getCatalogId());
		assertEquals("child test label No. 1", catalogEntry.getLabel());
		assertTrue(catalogEntry.isHasChildren());
		
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(1, children.size());
		assertEquals("child test label level 1 No. 1", children.get(0).getLabel());
		assertFalse(children.get(0).isHasChildren());
		
		final List<CatalogEntry> childrenLevel1 = children.get(0).getChildren();
		assertNull(childrenLevel1);
	}
	
	@Test
	public void testGetByIdLongBooleanIntIntOffset() {
		final CatalogEntry catalogEntry = catalogEntryDao.getById(8L, false, 0, 1);
		assertNotNull(catalogEntry);
		assertEquals(Long.valueOf(8), catalogEntry.getId());
		assertEquals(1L, catalogEntry.getCatalogId());
		assertEquals("child test label No. 1", catalogEntry.getLabel());
		assertTrue(catalogEntry.isHasChildren());
		
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(2, children.size());
		assertEquals("child test label level 1 No. 2", children.get(0).getLabel());
		assertTrue(children.get(0).isHasChildren());
		
		List<CatalogEntry> childrenLevel1 = children.get(0).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label level 1 No. 3", children.get(1).getLabel());
		assertFalse(children.get(1).isHasChildren());
		
		childrenLevel1 = children.get(0).getChildren();
		assertNull(childrenLevel1);
	}
	
	@Test
	public void testGetByIdLongBooleanIntIntLimitOffset() {
		final CatalogEntry catalogEntry = catalogEntryDao.getById(8L, false, 1, 1);
		assertNotNull(catalogEntry);
		assertEquals(Long.valueOf(8), catalogEntry.getId());
		assertEquals(1L, catalogEntry.getCatalogId());
		assertEquals("child test label No. 1", catalogEntry.getLabel());
		assertTrue(catalogEntry.isHasChildren());
		
		final List<CatalogEntry> children = catalogEntry.getChildren(); 
		assertNotNull(children);
		assertEquals(1, children.size());
		assertEquals("child test label level 1 No. 2", children.get(0).getLabel());
		assertTrue(children.get(0).isHasChildren());
		
		final List<CatalogEntry> childrenLevel1 = children.get(0).getChildren();
		assertNull(childrenLevel1);		
	}
/*
	@Test
	public void testGetPublicCatalogIdsAndPathsByEntityId() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPrivateCatalogIdsByEntityId() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteOrphanedCatalogEntries() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateCatalogEntry() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveCatalogEntry() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteCatalogEntry() {
		fail("Not yet implemented");
	}
*/
}
