package de.uni_koeln.arachne.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
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

import de.uni_koeln.arachne.context.AbstractLink;
import de.uni_koeln.arachne.context.ArachneLink;
import de.uni_koeln.arachne.context.Context;
import de.uni_koeln.arachne.context.ContextImageDescriptor;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Section;
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
	public void initXMLConfig() {
		xmlConfigUtil = new XmlConfigUtil();
		xmlConfigUtil.setServletContext(new MockServletContext("file:src/test/resources"));
	}
	
	@After
	public void releaseXMLConfig() {
		xmlConfigUtil = null;
	}
	
	private Dataset getTestDataSet() {
		final Dataset dataset = new Dataset();
		
		dataset.setArachneId(new EntityId("test", 0L, 0L, false));
				
		dataset.setFields("test.Title", "Title of the Test");
		
		final Dataset linkDataset = new Dataset();
		
		linkDataset.setArachneId(new EntityId("testContext", 0L, 1L, false));
				
		linkDataset.setFields("testContext.value", "Test Context Value");
		
		final ArachneLink link = new ArachneLink();
		link.setEntity1(dataset);
		link.setEntity2(linkDataset);
		
		final List<AbstractLink> contexts = new ArrayList<AbstractLink>();
		contexts.add(link);
		
		final Context context = new Context("testContext", dataset, contexts);
		dataset.addContext(context);		
		
		return dataset;
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
	public void testGetContentFromContext() {
		Document testDocument = xmlConfigUtil.getDocument("test");
		final Namespace namespace = testDocument.getRootElement().getNamespace();
		final Element context = testDocument.getRootElement().getChild("display", namespace)
				.getChild("datasections", namespace).getChild("section", namespace).getChild("context", namespace);
		
		final Section section = xmlConfigUtil.getContentFromContext(context, getTestDataSet(), namespace); 
		
		assertNotNull(section);
		assertFalse(section.getContent().isEmpty());
		assertEquals(1, section.getContent().size());
		assertEquals("Test Context Value", section.getContent().get(0).toString());
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
	public void testGetExplicitContextualizers() {
		// uncached
		List<String> contextualizers = xmlConfigUtil.getExplicitContextualizers("test");
		
		assertNotNull(contextualizers);
		assertFalse(contextualizers.isEmpty());
		assertEquals(3, contextualizers.size());
		assertEquals("testcontextualizer1", contextualizers.get(0));
		assertEquals("testcontextualizer2", contextualizers.get(1));
		assertEquals("testcontextualizer3", contextualizers.get(2));
		
		// cached
		contextualizers = xmlConfigUtil.getExplicitContextualizers("test");

		assertNotNull(contextualizers);
		assertFalse(contextualizers.isEmpty());
		assertEquals(3, contextualizers.size());
		assertEquals("testcontextualizer1", contextualizers.get(0));
		assertEquals("testcontextualizer2", contextualizers.get(1));
		assertEquals("testcontextualizer3", contextualizers.get(2));
		
		// uncached
		contextualizers = xmlConfigUtil.getExplicitContextualizers("unkowntype");
		assertNotNull(contextualizers);
		assertTrue(contextualizers.isEmpty());
		
		// cached
		contextualizers = xmlConfigUtil.getExplicitContextualizers("unkowntype");
		assertNotNull(contextualizers);
		assertTrue(contextualizers.isEmpty());
	}

	@Test
	public void testGetFacetsFromXMLFile() {
		List<String> facets = xmlConfigUtil.getFacetsFromXMLFile("test");
		System.out.println(facets);
		assertNotNull(facets);
		assertFalse(facets.isEmpty());
		assertEquals(2, facets.size());
		assertEquals("kategorie", facets.get(0));
		assertEquals("test", facets.get(1));
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
