package de.uni_koeln.arachne.response.link;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.uni_koeln.arachne.response.Dataset;

public class TestSimpleExternalLinkResolver {
	
	public Dataset dataset1, dataset2, dataset3, dataset4;
	public SimpleExternalLinkResolver resolver;
	
	@Before
	public void setUp() {
		
		dataset1 = new Dataset();
		dataset1.setFields("PrimaryKey", "23");
		dataset1.setFields("GattungAllgemein", "Sarkophag");
		dataset1.setFields("category", "objekt");
		
		dataset2 = new Dataset();
		dataset2.setFields("PrimaryKey", "42");
		dataset2.setFields("GattungAllgemein", "Sarkophag");
		dataset2.setFields("category", "moped");
		
		dataset3 = new Dataset();
		dataset3.setFields("PrimaryKey", "7");
		dataset3.setFields("GattungAllgemein", "Moped");
		dataset3.setFields("category", "objekt");
		
		dataset4 = new Dataset();
		dataset4.setFields("PrimaryKey", "56");
		dataset4.setFields("category", "moped");
		
		resolver = new SimpleExternalLinkResolver();
		resolver.setLabel("Sarkophagbrowser");
		resolver.setLinkPattern("http://arachne.uni-koeln.de/browser/index.php?view[layout]=sarkophag_item&sarkophag[jump_to_id]=%s");		
		Map<String,String> criteria = new HashMap<String,String>();
		resolver.setCriteria(criteria);
		List<String> patternFields = new ArrayList<String>();
		patternFields.add("PrimaryKey");
		resolver.setPatternFields(patternFields);
		
	}
	
	@Test
	public void testResolveOneCriterion() {		

		resolver.getCriteria().put("GattungAllgemein", "Sarkophag");
		
		ExternalLink link1 = resolver.resolve(dataset1);
		ExternalLink link2 = resolver.resolve(dataset2);
		ExternalLink link3 = resolver.resolve(dataset3);
		ExternalLink link4 = resolver.resolve(dataset4);
		
		assertNotNull(link1);
		assertEquals("Sarkophagbrowser", link1.getLabel());
		assertEquals("http://arachne.uni-koeln.de/browser/index.php?view[layout]=sarkophag_item&sarkophag[jump_to_id]=23", link1.getUrl());
		
		assertNotNull(link2);
		assertEquals("Sarkophagbrowser", link2.getLabel());
		assertEquals("http://arachne.uni-koeln.de/browser/index.php?view[layout]=sarkophag_item&sarkophag[jump_to_id]=42", link2.getUrl());
		
		assertNull(link3);
		
		assertNull(link4);
		
	}
	
	@Test
	public void testResolveOr() {

		resolver.getCriteria().put("GattungAllgemein", "Sarkophag");
		resolver.getCriteria().put("category", "objekt");
		
		ExternalLink link1 = resolver.resolve(dataset1);
		ExternalLink link2 = resolver.resolve(dataset2);
		ExternalLink link3 = resolver.resolve(dataset3);
		ExternalLink link4 = resolver.resolve(dataset4);
		
		assertNotNull(link1);
		assertEquals("Sarkophagbrowser", link1.getLabel());
		assertEquals("http://arachne.uni-koeln.de/browser/index.php?view[layout]=sarkophag_item&sarkophag[jump_to_id]=23", link1.getUrl());
		
		assertNotNull(link2);
		assertEquals("Sarkophagbrowser", link2.getLabel());
		assertEquals("http://arachne.uni-koeln.de/browser/index.php?view[layout]=sarkophag_item&sarkophag[jump_to_id]=42", link2.getUrl());
		
		assertNotNull(link3);
		assertEquals("Sarkophagbrowser", link3.getLabel());
		assertEquals("http://arachne.uni-koeln.de/browser/index.php?view[layout]=sarkophag_item&sarkophag[jump_to_id]=7", link3.getUrl());
		
		assertNull(link4);
		
	}
	
	@Test
	public void testResolveAnd() {

		resolver.getCriteria().put("GattungAllgemein", "Sarkophag");
		resolver.getCriteria().put("category", "objekt");
		resolver.setMatchAllCriteria(true);
		
		ExternalLink link1 = resolver.resolve(dataset1);
		ExternalLink link2 = resolver.resolve(dataset2);
		ExternalLink link3 = resolver.resolve(dataset3);
		ExternalLink link4 = resolver.resolve(dataset4);
		
		assertNotNull(link1);
		assertEquals("Sarkophagbrowser", link1.getLabel());
		assertEquals("http://arachne.uni-koeln.de/browser/index.php?view[layout]=sarkophag_item&sarkophag[jump_to_id]=23", link1.getUrl());
		
		assertNull(link2);
		
		assertNull(link3);
		
		assertNull(link4);
		
	}
	
	@Test
	public void testResolveComma() {

		resolver.getCriteria().put("PrimaryKey", "23,56");
		
		ExternalLink link1 = resolver.resolve(dataset1);
		ExternalLink link2 = resolver.resolve(dataset2);
		ExternalLink link3 = resolver.resolve(dataset3);
		ExternalLink link4 = resolver.resolve(dataset4);
		
		assertNotNull(link1);
		assertEquals("Sarkophagbrowser", link1.getLabel());
		assertEquals("http://arachne.uni-koeln.de/browser/index.php?view[layout]=sarkophag_item&sarkophag[jump_to_id]=23", link1.getUrl());
		
		assertNull(link2);
		
		assertNull(link3);
		
		assertNotNull(link4);
		assertEquals("Sarkophagbrowser", link4.getLabel());
		assertEquals("http://arachne.uni-koeln.de/browser/index.php?view[layout]=sarkophag_item&sarkophag[jump_to_id]=56", link4.getUrl());
		
	}

}
