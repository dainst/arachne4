package de.uni_koeln.arachne.dao.jdbc;

import static org.junit.Assert.*;

import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.testconfig.TestDataCatalog;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:unittest-context.xml"})
public class TestCatalogDao {

	@Autowired
	private CatalogDao catalogDao;
	
	@Autowired
	private DataSource datasource;
	
	private JdbcTemplate jdbcTemplate;
	
	@Before
	public void setUp() throws Exception {
		jdbcTemplate = new JdbcTemplate(datasource);
		TestDataCatalog testDataCatalog = new TestDataCatalog(jdbcTemplate);
		testDataCatalog.setUpCatalog();
		testDataCatalog.setUpCatalogEntry();
	}

	@After
	public void tearDown() throws Exception {
		TestDataCatalog testDataCatalog = new TestDataCatalog(jdbcTemplate);
		testDataCatalog.tearDownCatalog();
		testDataCatalog.tearDownCatalogEntry();
	}
	
	@Test
	public void testGetById() {
		final Catalog catalog = catalogDao.getById(1);
		assertNotNull(catalog);
		assertEquals(1L, catalog.getId());
		assertEquals("testuser", catalog.getAuthor());
		assertEquals("Arachne", catalog.getDatasetGroup());
		
		final CatalogEntry rootEntry = catalog.getRoot();
		assertNotNull(rootEntry);
		assertEquals("root test label", rootEntry.getLabel());
		assertTrue(rootEntry.isHasChildren());
		
		final List<CatalogEntry> childEntries = rootEntry.getChildren();
		assertNotNull(childEntries);
		assertEquals(2, childEntries.size());
		assertEquals("child test label No. 1", childEntries.get(0).getLabel());
		assertTrue(childEntries.get(0).isHasChildren());
		List<CatalogEntry> childrenLevel1 = childEntries.get(0).getChildren();
		assertNull(childrenLevel1);
		assertEquals("child test label No. 2", childEntries.get(1).getLabel());
		assertFalse(childEntries.get(1).isHasChildren());
		childrenLevel1 = childEntries.get(1).getChildren();
		assertNull(childrenLevel1);
	}
}
