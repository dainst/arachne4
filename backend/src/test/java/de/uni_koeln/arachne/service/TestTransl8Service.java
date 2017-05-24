package de.uni_koeln.arachne.service;

import static org.junit.Assert.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;

import de.uni_koeln.arachne.service.Transl8Service.Transl8Exception;
import de.uni_koeln.arachne.util.network.ArachneRestTemplate;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-context.xml"})
public class TestTransl8Service {
	private final String LANG = "de";

	@Autowired
	private Transl8Service transl8Service;
	
	@Autowired
	private ArachneRestTemplate restTemplate;
	
	@Value("${transl8Url}")
	private String transl8URL;
	
	private MockRestServiceServer mockServer;
	
	private static final String transl8Response = "{"
			+ "\"test_key1\":\"testvalue1\", "
			+ "\"test_key2\":\"testvalue2\", "
			+ "\"facet_test_key1\":\"testfacetvalue1\", "
			+ "\"facet_test_key2\":\"testfacetvalue2\", "
			+ "\"facet_kategorie_key1\":\"testcategoryvalue1\", "
			+ "\"facet_kategorie_key2\":\"testcategoryvalue2\""
			+ "}";
	
	@Before
	public void setUp() {
		mockServer = MockRestServiceServer.createServer(restTemplate);
	}
	
	// Clear translations after every test
	@After
	public void tearDown() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		mockServer = null;
				
		Field lang = Transl8Service.class.getDeclaredField("languages");
		lang.setAccessible(true);
				
		Field field = Transl8Service.class.getDeclaredField("translationsAvailable");
		field.setAccessible(true);
		final Map<String, Boolean> map = new HashMap<String, Boolean>();
		@SuppressWarnings("unchecked")
		final String key = ((List<String>) lang.get(transl8Service)).get(0);
		map.put(key, false);
		
		field.set(transl8Service, map);
		
		field = Transl8Service.class.getDeclaredField("translationMap");
		field.setAccessible(true);
		field.set(transl8Service, new HashMap<String, String>());
		
		field = Transl8Service.class.getDeclaredField("categoryMap");
		field.setAccessible(true);
		field.set(transl8Service, new HashMap<String, String>());
	}

	@Test
	public void testTransl8() throws Transl8Exception {
		mockServer.expect(
				requestTo(transl8URL))
				.andRespond(withSuccess(transl8Response, MediaType.APPLICATION_JSON));
		
		// must get translations from transl8 
		String result = transl8Service.transl8("test_key1", LANG);
		
		mockServer.verify();
		
		assertEquals("testvalue1", result);
		
		// translations already available
		assertEquals("testvalue2", transl8Service.transl8("test_key2", LANG));
		
		//  no translation available
		assertEquals("test_key3", transl8Service.transl8("test_key3", LANG));
	}
	
	@Test
	public void testTransl8Facet() throws Transl8Exception {
		mockServer.expect(
				requestTo(transl8URL))
				.andRespond(withSuccess(transl8Response, MediaType.APPLICATION_JSON));
		
		// must get translations from transl8 
		String result = transl8Service.transl8Facet("test", "key1", LANG);
		
		mockServer.verify();
		
		assertEquals("testfacetvalue1", result);
		
		// translations already available
		assertEquals("testfacetvalue2", transl8Service.transl8Facet("test", "key2", LANG));
		
		// no translation available
		assertEquals("key3", transl8Service.transl8Facet("test", "key3", LANG));
	}

	@Test
	public void testCategoryLookUp() throws Transl8Exception {
		mockServer.expect(
				requestTo(transl8URL))
				.andRespond(withSuccess(transl8Response, MediaType.APPLICATION_JSON));
		
		// must get translations from transl8 
		String result = transl8Service.categoryLookUp("testcategoryvalue1", LANG);
		
		mockServer.verify();
		
		assertEquals("key1", result);
		
		// translations already available
		assertEquals("key2", transl8Service.categoryLookUp("testcategoryvalue2", LANG));
		
		// no translation available
		assertEquals("testcategoryvalue3", transl8Service.categoryLookUp("testcategoryvalue3", LANG));
	}
	
	@Test(expected = Transl8Service.Transl8Exception.class)
	public void exceptionsEnabled() throws Transl8Exception {
		mockServer.expect(
				requestTo(transl8URL))
				.andRespond(withServerError());
		
		transl8Service.transl8("foobar", LANG);
		
		mockServer.verify();
	}
}
