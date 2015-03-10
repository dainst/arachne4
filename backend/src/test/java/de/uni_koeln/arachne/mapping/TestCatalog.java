package de.uni_koeln.arachne.mapping;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCatalog {

	public static Catalog catalog;

	@BeforeClass
	public static void setUp() throws JsonParseException, JsonMappingException,
			IOException {
		URL resource = TestCatalog.class
				.getResource("/WEB-INF/json/catalog.json");
		ObjectMapper mapper = new ObjectMapper();
		catalog = mapper.readValue(
				Resources.toString(resource, Charsets.UTF_8), Catalog.class);
	}

	@Test
	public void test1Deserialization() {
		assertNotNull(catalog);
		assertNotNull(catalog.getRoot());
		assertNotNull(catalog.getRoot().getChildren());
		assertNull(catalog.getCatalogEntries());
	}

	@Test
	public void test2SetCatalog() {

		catalog.addToCatalogEntries(catalog.getRoot());
		catalog.getRoot().setCatalog(catalog);

		assertEquals(catalog.getCatalogEntries().size(), 7);
		for (CatalogEntry entry : catalog.getCatalogEntries()) {
			assertSame(catalog, entry.getCatalog());
		}
	}

	@Test
	public void test3GeneratePath() {

		catalog.getRoot().generatePath();

		for (final CatalogEntry entry : catalog.getCatalogEntries()) {
			assertNotNull(entry.getPath());
			assertTrue(entry.getPath().endsWith(entry.getId().toString()));
			assertTrue(entry.getPath().startsWith(catalog.getId().toString()));
			assertTrue(entry.getPath().contains("/"));
		}
	}

	@Test
	public void test4RemoveFromCatalog() {

		catalog.getRoot().removeFromCatalog();

		assertEquals(catalog.getCatalogEntries().size(), 0);
	}

}
