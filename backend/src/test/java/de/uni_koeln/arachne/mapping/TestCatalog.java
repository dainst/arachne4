package de.uni_koeln.arachne.mapping;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCatalog {
	
	public static Catalog catalog;
	
	@BeforeClass
	public static void setUp() throws JsonParseException, JsonMappingException, IOException{
		Resource resource = new ClassPathResource("de/uni_koeln/arachne/mapping/catalog.json");		
		ObjectMapper mapper = new ObjectMapper();
		catalog = mapper.readValue(resource.getFile(), Catalog.class);
	}
	
	@Test
	public void test1Deserialization(){		
		assertNotNull(catalog);
		assertNotNull(catalog.getCatalogEntries());
		assertNull(catalog.getCatalogEntries().get(0).getCatalog());
	}
	
	@Test
	public void test2SetCatalog(){
		List<CatalogEntry> temp = new ArrayList<CatalogEntry>();
		temp.addAll(catalog.getCatalogEntries());
		catalog.setCatalogEntries(null);
		Iterator<CatalogEntry> iter = temp.iterator();
	    while (iter.hasNext()) {	    	
	    	CatalogEntry entry = iter.next();		
			entry.setCatalog(catalog);					
			catalog.addToCatalogEntries(entry);				
	    }
	    assertEquals(catalog.getCatalogEntries().size(), 6);
	    for (CatalogEntry entry : catalog.getCatalogEntries()){
	    	assertSame(catalog, entry.getCatalog());
	    }
	}
	
	@Test	
	public void test3GeneratePath(){
		for (final CatalogEntry entry : catalog.getCatalogEntriesWithoutParents()) {	
			assertNotNull(entry.getCatalog());
			entry.generatePath();
		}
		for (final CatalogEntry entry : catalog.getCatalogEntries()) {
			assertNotNull(entry.getPath());
			assertTrue(entry.getPath().endsWith(entry.getId().toString()));
			assertTrue(entry.getPath().startsWith(catalog.getId().toString()));
			assertTrue(entry.getPath().contains("/"));
		}
	}
	
	@Test
	public void test4RemoveFromCatalog(){
		List<CatalogEntry> temp = new ArrayList<CatalogEntry>();
		temp.addAll(catalog.getCatalogEntriesWithoutParents());
		Iterator<CatalogEntry> iter = temp.iterator();
	    while (iter.hasNext()) {	    	
	    	CatalogEntry entry = iter.next();		
			entry.removeFromCatalog();	
	    }
	    assertEquals(catalog.getCatalogEntries().size(), 0);
	}

}
