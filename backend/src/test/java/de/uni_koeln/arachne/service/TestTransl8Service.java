package de.uni_koeln.arachne.service;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import de.uni_koeln.arachne.service.Transl8Service.Transl8Exception;


@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-context.xml"})
@TestPropertySource(properties = "transl8enabled=true")
public class TestTransl8Service {

	private final String LANG = "de";

	@Autowired
	private Transl8Service transl8Service;

	// Clear translations after every test
	@After
	public void tearDown() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		Field lang = Transl8Service.class.getDeclaredField("supportedLanguages");
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

		// key [type_literatur] -> value test for de and en:
		assertEquals("Literatur", transl8Service.transl8("type_literatur", "de"));
		assertEquals("Literature", transl8Service.transl8("type_literatur", "en"));
	}

	@Test
	public void testTransl8Facet() throws Transl8Exception {

		// facetName as part of key ["facet_kategorie_bauwerk"] + key -> value test for de and en:
		assertEquals("Bauwerke", transl8Service.transl8Facet("kategorie","bauwerk", "de"));
		assertEquals("Building", transl8Service.transl8Facet("kategorie","bauwerk", "en"));
	}

	@Test
	public void testCategoryLookUp() throws Transl8Exception {

		// reverse value -> category_key test for de and en:
		assertEquals("bauwerk", transl8Service.categoryLookUp("Bauwerke", "de"));
		assertEquals("bauwerk", transl8Service.categoryLookUp("Building", "en"));
	}

	/*

	@Test
	??? extractLanguage ???

	not necessary?

	@Test(expected = Transl8Service.Transl8Exception.class)
	public void exceptionsEnabled() throws Transl8Exception {

		transl8Service.transl8("foobar", LANG);

	}
	*/
}
