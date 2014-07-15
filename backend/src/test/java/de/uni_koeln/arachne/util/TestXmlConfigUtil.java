package de.uni_koeln.arachne.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import de.uni_koeln.arachne.sqlutil.TableConnectionDescription;
import de.uni_koeln.arachne.testconfig.WebContextTestExecutionListener;


@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
@TestExecutionListeners( { WebContextTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class })
public class TestXmlConfigUtil {
	private transient XmlConfigUtil xmlConfigUtil;
	
	private void initTestXMLConfig() {
		xmlConfigUtil = new XmlConfigUtil();
		xmlConfigUtil.setServletContext(new MockServletContext("file:src/test/resources"));
	}
	
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
		assertNotNull(xmlConfigUtil.getDocument("sammler"));
		assertNotNull(xmlConfigUtil.getDocument("sammlungen"));
		assertNotNull(xmlConfigUtil.getDocument("sarkophag"));
		assertNotNull(xmlConfigUtil.getDocument("surfacetreatment"));
		assertNotNull(xmlConfigUtil.getDocument("surfacetreatmentaction"));
		assertNotNull(xmlConfigUtil.getDocument("topographie"));
		assertNotNull(xmlConfigUtil.getDocument("typus"));
				
		xmlConfigUtil = null;
	}
	
	@Test
	public void testGetDocument() {
		initTestXMLConfig();
		
		assertNotNull(xmlConfigUtil.getDocument("test"));
		assertNull(xmlConfigUtil.getDocument("unknowntype"));
		
		xmlConfigUtil = null;
	}
	
	@Test
	public void testGetSubcategories() {
		initTestXMLConfig();
		
		List<TableConnectionDescription> subCategories = xmlConfigUtil.getSubCategories("test");
		assertNotNull(subCategories);
		
		TableConnectionDescription tableConnectionDescription = subCategories.get(0);
		assertNotNull(tableConnectionDescription);
		assertEquals("test", tableConnectionDescription.getTable1());
		// TODO update when the corresponding querybuilder is replaced
		//assertEquals("PSTest_ID", tableConnectionDescription.getField1());
		assertEquals("PrimaryKey", tableConnectionDescription.getField1());
		assertEquals("testSubTable1", tableConnectionDescription.getTable2());
		assertEquals("testSubField", tableConnectionDescription.getField2());
		
		tableConnectionDescription = subCategories.get(1);
		assertNotNull(tableConnectionDescription);
		assertEquals("test", tableConnectionDescription.getTable1());
		assertEquals("testParentField", tableConnectionDescription.getField1());
		assertEquals("testSubTable2", tableConnectionDescription.getTable2());
		// TODO update when the corresponding querybuilder is replaced
		//assertEquals("PSTestSubTable2_ID", tableConnectionDescription.getField2());
		assertEquals("PrimaryKey", tableConnectionDescription.getField2());
		
		subCategories = xmlConfigUtil.getSubCategories("testnotavailable");
		assertNotNull(subCategories);
		assertTrue(subCategories.isEmpty());
	}
}
