package de.uni_koeln.arachne.service;

import static org.junit.Assert.*;
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

		// reverse: value -> category_key test for de and en:
		assertEquals("bauwerk", transl8Service.categoryLookUp("Bauwerke", "de"));
		assertEquals("bauwerk", transl8Service.categoryLookUp("Building", "en"));
	}

	@Test
	public void extractLanguage() {

		assertEquals("de", transl8Service.extractLanguage(LANG));
		assertEquals("en", transl8Service.extractLanguage("en"));
		assertEquals("en", transl8Service.extractLanguage("xyz"));		// expected: returns DEFAULT_LANG (en)
	}
}
