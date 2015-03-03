package de.uni_koeln.arachne.util;

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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations = {"classpath:test-context.xml"}) 
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ITestESClientUtil {
	
	@Autowired
	private transient ESClientUtil esClientUtil;

	@Before
	public void setUp() {
		esClientUtil.setServletContext(new MockServletContext("file:src/main/webapp"));
		
		try {
			Field indexName;
			indexName = ESClientUtil.class.getDeclaredField("INDEX_1");
			indexName.setAccessible(true);
			indexName.set(esClientUtil, "test_arachne4_1");
			indexName = ESClientUtil.class.getDeclaredField("INDEX_2");
			indexName.setAccessible(true);
			indexName.set(esClientUtil, "test_arachne4_2");
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test1ValidateSetting() {
		try {
			final Field settingsFile = ESClientUtil.class.getDeclaredField("SETTINGS_FILE");
			settingsFile.setAccessible(true);
			final Method getJsonFromFile = ESClientUtil.class.getDeclaredMethod("getJsonFromFile", String.class);
			getJsonFromFile.setAccessible(true);
			final String filename = (String)settingsFile.get(esClientUtil);
			final String json = (String)getJsonFromFile.invoke(esClientUtil, filename);
			assertTrue("File 'src/main/webapp" + filename + "' is not valid JSON!", isValidJson(json));
		} catch (NoSuchFieldException | NoSuchMethodException | SecurityException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void test2ValidateMapping() {
		try {
			final Field mappingFile = ESClientUtil.class.getDeclaredField("MAPPING_FILE");
			mappingFile.setAccessible(true);
			final Method getJsonFromFile = ESClientUtil.class.getDeclaredMethod("getJsonFromFile", String.class);
			getJsonFromFile.setAccessible(true);
			final String filename = (String)mappingFile.get(esClientUtil);
			final String json = (String)getJsonFromFile.invoke(esClientUtil, filename);
			assertTrue("File 'src/main/webapp" + filename + "' is not valid JSON!", isValidJson(json));
		} catch (NoSuchFieldException | NoSuchMethodException | SecurityException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void test3GetClient() {
		assertNotNull(esClientUtil.getClient());
	}
	
	@Test
	public void test4GetDataimportIndex() {
		final String index = esClientUtil.getDataImportIndex();
		assertNotEquals("NoIndex", index);
	}
	
	@Test
	public void test5UpdateSearchIndex() {
		assertEquals("test_arachne4_1", esClientUtil.updateSearchIndex());
		esClientUtil.getDataImportIndex();
		assertEquals("test_arachne4_2", esClientUtil.updateSearchIndex());
	}
	
	@Test
	public void test6DeleteIndex() {
		assertTrue("Deletion of index 'test_arachne4_2' failed!", esClientUtil.deleteIndex("test_arachne4_2"));
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
