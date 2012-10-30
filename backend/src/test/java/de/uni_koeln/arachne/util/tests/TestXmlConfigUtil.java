package de.uni_koeln.arachne.util.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import de.uni_koeln.arachne.testconfig.WebContextTestExecutionListener;
import de.uni_koeln.arachne.util.XmlConfigUtil;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
@TestExecutionListeners( { WebContextTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class })
public class TestXmlConfigUtil {
	private transient XmlConfigUtil xmlConfigUtil;
	
	@Before
	public void setUp() {
		xmlConfigUtil = new XmlConfigUtil();
		xmlConfigUtil.setServletContext(new MockServletContext("file:src/main/webapp"));
	}
	
	@Test
	public void testGetDocument() {
		assertNotNull(xmlConfigUtil.getDocument("bauwerk"));
		assertNotNull(xmlConfigUtil.getDocument("bauwerksteil"));
		assertNotNull(xmlConfigUtil.getDocument("buch"));
		assertNotNull(xmlConfigUtil.getDocument("buchseite"));
		assertNotNull(xmlConfigUtil.getDocument("gruppen"));
		assertNotNull(xmlConfigUtil.getDocument("inschrift"));
		assertNotNull(xmlConfigUtil.getDocument("literatur"));
		assertNotNull(xmlConfigUtil.getDocument("marbilder"));
		assertNotNull(xmlConfigUtil.getDocument("objekt"));
		assertNotNull(xmlConfigUtil.getDocument("ort"));
		assertNotNull(xmlConfigUtil.getDocument("person"));
		assertNotNull(xmlConfigUtil.getDocument("realien"));
		assertNotNull(xmlConfigUtil.getDocument("relief"));
		assertNotNull(xmlConfigUtil.getDocument("reproduktion"));
		assertNotNull(xmlConfigUtil.getDocument("sammler"));
		assertNotNull(xmlConfigUtil.getDocument("sammlungen"));
		assertNotNull(xmlConfigUtil.getDocument("sarkophag"));
		assertNotNull(xmlConfigUtil.getDocument("topographie"));
		assertNotNull(xmlConfigUtil.getDocument("typus"));
		
		assertNull(xmlConfigUtil.getDocument("unknowntype"));
	}
}
