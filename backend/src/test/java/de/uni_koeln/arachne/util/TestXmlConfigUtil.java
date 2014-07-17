package de.uni_koeln.arachne.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
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

import de.uni_koeln.arachne.context.ContextImageDescriptor;
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
	
	@Before
	public void initTestXMLConfig() {
		xmlConfigUtil = new XmlConfigUtil();
		xmlConfigUtil.setServletContext(new MockServletContext("file:src/test/resources"));
	}
	
	@After
	public void releaseXMLConfig() {
		xmlConfigUtil = null;
	}
	
	@Test
	public void testGetContextImagesNames() {
		// uncached
		List<ContextImageDescriptor> contextImageDescriptors = xmlConfigUtil.getContextImagesNames("test");
		
		assertNotNull(contextImageDescriptors);
		assertFalse(contextImageDescriptors.isEmpty());
		
		assertEquals("testalways", contextImageDescriptors.get(0).getContextName());
		assertEquals("always", contextImageDescriptors.get(0).getContextImageUsage());
		
		assertEquals("testifempty", contextImageDescriptors.get(1).getContextName());
		assertEquals("ifEmpty", contextImageDescriptors.get(1).getContextImageUsage());
		
		// cached
		contextImageDescriptors = xmlConfigUtil.getContextImagesNames("test");
		
		assertNotNull(contextImageDescriptors);
		assertFalse(contextImageDescriptors.isEmpty());
		
		assertEquals("testalways", contextImageDescriptors.get(0).getContextName());
		assertEquals("always", contextImageDescriptors.get(0).getContextImageUsage());
		
		assertEquals("testifempty", contextImageDescriptors.get(1).getContextName());
		assertEquals("ifEmpty", contextImageDescriptors.get(1).getContextImageUsage());
		
		// uncached
		contextImageDescriptors = xmlConfigUtil.getContextImagesNames("unknowntype");
		assertNotNull(contextImageDescriptors);
		assertTrue(contextImageDescriptors.isEmpty());

		// cached
		contextImageDescriptors = xmlConfigUtil.getContextImagesNames("unknowntype");
		assertNotNull(contextImageDescriptors);
		assertTrue(contextImageDescriptors.isEmpty());
	}
	
	@Test
	public void testGetDocument() {
		// uncached
		assertNotNull(xmlConfigUtil.getDocument("test"));
		
		// cached
		assertNotNull(xmlConfigUtil.getDocument("test"));
		
		// uncached
		assertNull(xmlConfigUtil.getDocument("unknowntype"));
		
		// cached
		assertNull(xmlConfigUtil.getDocument("unknowntype"));
	}
	
	@Test
	public void testGetExplicitContextualizers() {
		// uncached
		List<String> contextualizers = xmlConfigUtil.getExplicitContextualizers("test");
		
		assertNotNull(contextualizers);
	}
	
	@Test
	public void testGetSubcategories() {
		// uncached
		List<TableConnectionDescription> subCategories = xmlConfigUtil.getSubCategories("test");
		assertNotNull(subCategories);
		assertFalse(subCategories.isEmpty());
		
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
		
		// cached
		subCategories = xmlConfigUtil.getSubCategories("test");
		assertNotNull(subCategories);
		assertFalse(subCategories.isEmpty());
		
		tableConnectionDescription = subCategories.get(0);
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
		
		// uncached
		subCategories = xmlConfigUtil.getSubCategories("unknowntype");
		assertNotNull(subCategories);
		assertTrue(subCategories.isEmpty());
		
		// cached
		subCategories = xmlConfigUtil.getSubCategories("unknowntype");
		assertNotNull(subCategories);
		assertTrue(subCategories.isEmpty());
	}
}
