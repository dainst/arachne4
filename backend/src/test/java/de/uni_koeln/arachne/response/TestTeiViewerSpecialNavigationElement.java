package de.uni_koeln.arachne.response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTeiViewerSpecialNavigationElement {
	
	private transient TeiViewerSpecialNavigationElement testItem;

	@Before
	public void setUp() {
		testItem = new TeiViewerSpecialNavigationElement();
	}
	
	@After
	public void teadDown() {
		testItem = null;
	}
	
	@Test
	public void testMatches() {
		Assert.assertTrue(testItem.matches("manifest:antiquitues_of_ionia", null));
		Assert.assertFalse(testItem.matches("searchterm", null));
	}
	
	@Test
	public void testGetResult() {
		Assert.assertNotNull(testItem.getResult("manifest:antiquitues_of_ionia", null));
	}
}
