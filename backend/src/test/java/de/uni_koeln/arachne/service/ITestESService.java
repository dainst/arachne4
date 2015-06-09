package de.uni_koeln.arachne.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.arachne.service.ESService;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations = {"classpath:test-context.xml"}) 
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@WebAppConfiguration
public class ITestESService {
	
	@Autowired
	private transient ESService esService;

	@Before
	public void setUp() {
		esService.setServletContext(new MockServletContext("file:src/main/webapp"));
		
		try {
			Field indexName;
			indexName = ESService.class.getDeclaredField("INDEX_1");
			indexName.setAccessible(true);
			indexName.set(esService, "test_arachne4_1");
			indexName = ESService.class.getDeclaredField("INDEX_2");
			indexName.setAccessible(true);
			indexName.set(esService, "test_arachne4_2");
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test1ValidateSetting() {
		try {
			final Field settingsFile = ESService.class.getDeclaredField("SETTINGS_FILE");
			settingsFile.setAccessible(true);
			final Method getJsonFromFile = ESService.class.getDeclaredMethod("getJsonFromFile", String.class);
			getJsonFromFile.setAccessible(true);
			final String filename = (String)settingsFile.get(esService);
			final String json = (String)getJsonFromFile.invoke(esService, filename);
			assertTrue("File 'src/main/webapp" + filename + "' is not valid JSON!", isValidJson(json));
		} catch (NoSuchFieldException | NoSuchMethodException | SecurityException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void test2ValidateMapping() {
		try {
			final Field mappingFile = ESService.class.getDeclaredField("MAPPING_FILE");
			mappingFile.setAccessible(true);
			final Method getJsonFromFile = ESService.class.getDeclaredMethod("getJsonFromFile", String.class);
			getJsonFromFile.setAccessible(true);
			final String filename = (String)mappingFile.get(esService);
			final String json = (String)getJsonFromFile.invoke(esService, filename);
			assertTrue("File 'src/main/webapp" + filename + "' is not valid JSON!", isValidJson(json));
		} catch (NoSuchFieldException | NoSuchMethodException | SecurityException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void test3GetClient() {
		assertNotNull(esService.getClient());
	}
	
	@Test
	public void test4GetDataimportIndex() {
		final String index = esService.getDataImportIndex();
		assertNotEquals("NoIndex", index);
	}
	
	@Test
	public void test5UpdateSearchIndex() {
		assertEquals("test_arachne4_1", esService.updateSearchIndex());
		esService.getDataImportIndex();
		assertEquals("test_arachne4_2", esService.updateSearchIndex());
	}
	
	@Test
	public void test6DeleteIndex() {
		assertTrue("Deletion of index 'test_arachne4_2' failed!", esService.deleteIndex("test_arachne4_2"));
	}
	
	private boolean isValidJson(final String json) {
		boolean result = false;
		JsonParser parser;
		try {
			parser = new ObjectMapper().getFactory().createParser(json);
			while (parser.nextToken() != null) {}
		    result = true;
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		// check for duplicate keys
		if (result) {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
			try {
				objectMapper.readTree(json);
			} catch (IOException e) {
				e.printStackTrace();
				result = false;
			}
		}
		
		return result;
	}

}
