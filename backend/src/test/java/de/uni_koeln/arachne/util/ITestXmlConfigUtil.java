package de.uni_koeln.arachne.util;

import static org.junit.Assert.*;

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


@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
@TestExecutionListeners( { WebContextTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class })
public class ITestXmlConfigUtil {
	private transient XmlConfigUtil xmlConfigUtil;
	
	@Test
	public void testForDocumentCompleteness() {
		xmlConfigUtil = new XmlConfigUtil();
		xmlConfigUtil.setServletContext(new MockServletContext("file:src/main/webapp"));
		
		assertNotNull(xmlConfigUtil.getDocument("bauwerk"));
		assertNotNull(xmlConfigUtil.getDocument("bauwerksteil"));
		assertNotNull(xmlConfigUtil.getDocument("befund"));
		assertNotNull(xmlConfigUtil.getDocument("buch"));
		assertNotNull(xmlConfigUtil.getDocument("buchseite"));
		assertNotNull(xmlConfigUtil.getDocument("einschluss"));
		assertNotNull(xmlConfigUtil.getDocument("fabric"));
		assertNotNull(xmlConfigUtil.getDocument("fabricdescription"));
		assertNotNull(xmlConfigUtil.getDocument("gruppen"));
		assertNotNull(xmlConfigUtil.getDocument("gruppierung"));
		assertNotNull(xmlConfigUtil.getDocument("individualvessel"));
		assertNotNull(xmlConfigUtil.getDocument("inschrift"));
		assertNotNull(xmlConfigUtil.getDocument("isolatedsherd"));
		assertNotNull(xmlConfigUtil.getDocument("literatur"));
		assertNotNull(xmlConfigUtil.getDocument("mainabstract"));
		assertNotNull(xmlConfigUtil.getDocument("marbilder"));
		assertNotNull(xmlConfigUtil.getDocument("modell3d"));
		assertNotNull(xmlConfigUtil.getDocument("morphology"));
		assertNotNull(xmlConfigUtil.getDocument("niton"));
		assertNotNull(xmlConfigUtil.getDocument("objekt"));
		assertNotNull(xmlConfigUtil.getDocument("ort"));
		assertNotNull(xmlConfigUtil.getDocument("person"));
		assertNotNull(xmlConfigUtil.getDocument("quantities"));
		assertNotNull(xmlConfigUtil.getDocument("realien"));
		assertNotNull(xmlConfigUtil.getDocument("relief"));
		assertNotNull(xmlConfigUtil.getDocument("reproduktion"));
		assertNotNull(xmlConfigUtil.getDocument("rezeption"));
		assertNotNull(xmlConfigUtil.getDocument("sammlungen"));
		assertNotNull(xmlConfigUtil.getDocument("sarkophag"));
		assertNotNull(xmlConfigUtil.getDocument("surfacetreatment"));
		assertNotNull(xmlConfigUtil.getDocument("surfacetreatmentaction"));
		assertNotNull(xmlConfigUtil.getDocument("topographie"));
		assertNotNull(xmlConfigUtil.getDocument("typus"));
				
		xmlConfigUtil = null;
	}
}
