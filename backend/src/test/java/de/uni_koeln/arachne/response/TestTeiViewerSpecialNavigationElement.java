package de.uni_koeln.arachne.response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import de.uni_koeln.arachne.testconfig.WebContextTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
@TestExecutionListeners( { WebContextTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class })
public class TestTeiViewerSpecialNavigationElement {
	
	@Autowired
	private transient TeiViewerSpecialNavigationElement testItem;
	
	@Test
	public void testMatches() {
		Assert.assertTrue(testItem.matches("15048", null));
		Assert.assertFalse(testItem.matches("searchterm", null));
	}
	
	@Test
	public void testGetResult() {
		final boolean matches = testItem.matches("15048", null);
		
		Assert.assertTrue(matches);
		if(matches) {
			final AbstractSpecialNavigationElement result = testItem.getResult("15048", null);
			Assert.assertNotNull(result);
			Assert.assertEquals("http://arachne.uni-koeln.de/Tei-Viewer/cgi-bin/teiviewer.php?manifest=antiquities_of_ionia_1", 
					result.getLink());
		}
	}
}
