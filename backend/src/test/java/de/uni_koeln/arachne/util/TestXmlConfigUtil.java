package de.uni_koeln.arachne.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;

import de.uni_koeln.arachne.context.ContextImageDescriptor;
import de.uni_koeln.arachne.response.AbstractContent;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Section;
import de.uni_koeln.arachne.util.sql.TableConnectionDescription;
import de.uni_koeln.arachne.testconfig.TestData;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
public class TestXmlConfigUtil {
	
	private transient TestData testData;
	
	private transient XmlConfigUtil xmlConfigUtil;
	
	@Before
	public void initXMLConfig() {
		xmlConfigUtil = new XmlConfigUtil();
		xmlConfigUtil.setServletContext(new MockServletContext("file:src/test/resources"));
		
		testData = new TestData();
	}
	
	@After
	public void releaseXMLConfig() {
		xmlConfigUtil = null;
		testData = null;
	}
	
	@Test
	public void testGetDocumentValid() {
		// uncached
		assertNotNull(xmlConfigUtil.getDocument("test"));
		
		// cached
		assertNotNull(xmlConfigUtil.getDocument("test"));
	}
	
	@Test
	public void testGetDocumentInvalid() {		
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
		
		final Section section = xmlConfigUtil.getContentFromContext(context, namespace, testData.getTestDataset()); 
		
		assertNotNull(section);
		assertFalse(section.getContent().isEmpty());
		assertEquals(1, section.getContent().size());
		assertEquals("Test Context Value1-TestSeparator1-Test Context Value3<hr>Test Context Value4"
				+"<hr>Test Context Value5-TestSeparator2-Test Context Value6"
				+"-TestSeparator3-Test Context Value7", section.getContent().get(0).toString());
	}
	
	@Test
	public void testGetContentFromSections() {
		Document testDocument = xmlConfigUtil.getDocument("test");
		final Namespace namespace = testDocument.getRootElement().getNamespace();
		final List<Element> sections = testDocument.getRootElement().getChild("display", namespace)
				.getChild("datasections", namespace).getChild("section", namespace).getChildren("section", namespace);
		
		final Dataset dataset = testData.getTestDataset();
		
		final List<String> expected = new ArrayList<String>(3);
		expected.add("Testdata prefix/postfix: PrefixTest=success<hr>PostfixTest=success");
		expected.add("Testdata separator: first-second");
		expected.add("Testdata value edit: correctly replaced<hr>correctly trimmed");
		expected.add("Testdata linkField: Start<hr><a href=\"http://testserver.com/link1.html\""
				+ " target=\"_blank\">TestLink1</a>-TestLinkOverride-"
				+ "<a href=\"http://testserver.com/link2.html\" target=\"_blank\">TestLink2</a>"
				+ "<hr>End");
				
		for (int i = 0; i < 4; i++) {
			final AbstractContent content = xmlConfigUtil.getContentFromSections(sections.get(i), namespace, dataset);
			assertNotNull(content);
			assertFalse(content.toString().isEmpty());
			assertEquals(expected.get(i), content.toString());
		}
	}
		
	@Test
	public void testGetContextImagesNamesValid() {
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
	}
	
	@Test
	public void testGetContextImagesNamesInvalid() {
		List<ContextImageDescriptor> contextImageDescriptors = xmlConfigUtil.getContextImagesNames("test");
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
	public void testGetExplicitContextualizersValid() {
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
	}
	
	@Test
	public void testGetExplicitContextualizersInvalid() {		
		// uncached
		List<String> contextualizers = xmlConfigUtil.getExplicitContextualizers("unkowntype");
		assertNotNull(contextualizers);
		assertTrue(contextualizers.isEmpty());
		
		// cached
		contextualizers = xmlConfigUtil.getExplicitContextualizers("unkowntype");
		assertNotNull(contextualizers);
		assertTrue(contextualizers.isEmpty());
	}

	@Test
	public void testGetFacetsFromXMLFile() {
		Set<String> facets = xmlConfigUtil.getFacetsFromXMLFile("test");
		
		assertNotNull(facets);
		assertFalse(facets.isEmpty());
		assertEquals(3, facets.size());
		assertTrue(facets.contains("kategorie"));
		assertTrue(facets.contains("test"));
		assertTrue(facets.contains("multivaluetest"));
	}
	
	@Test
	public void testGetIfEmptyFromField() {
		Document testDocument = xmlConfigUtil.getDocument("test");
		final Namespace namespace = testDocument.getRootElement().getNamespace();
		final Element section = testDocument.getRootElement().getChild("display", namespace)
				.getChild("title", namespace).getChild("section", namespace);
				
		final Dataset dataset = testData.getTestDataset();
		
		StringBuilder ifEmptySB = xmlConfigUtil.getIfEmptyFromField(section, namespace, dataset);
		assertNull(ifEmptySB);
		
		ifEmptySB = xmlConfigUtil.getIfEmptyFromField(section.getChild("field", namespace), namespace, dataset);
		assertNotNull(ifEmptySB);
		assertEquals("Title of the Test", ifEmptySB.toString());		
	}
	
	@Test
	public void testGetSubcategoriesValid() {
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
	}
	
	@Test
	public void testGetSubcategoriesInvalid() {
		// uncached
		List<TableConnectionDescription> subCategories = xmlConfigUtil.getSubCategories("unknowntype");
		assertNotNull(subCategories);
		assertTrue(subCategories.isEmpty());
		
		// cached
		subCategories = xmlConfigUtil.getSubCategories("unknowntype");
		assertNotNull(subCategories);
		assertTrue(subCategories.isEmpty());
	}
}
