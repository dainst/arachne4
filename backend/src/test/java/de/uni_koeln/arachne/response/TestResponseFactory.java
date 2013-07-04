package de.uni_koeln.arachne.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import de.uni_koeln.arachne.testconfig.WebContextTestExecutionListener;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.XmlConfigUtil;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
@TestExecutionListeners({WebContextTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class })
public class TestResponseFactory { // NOPMD
	private transient ResponseFactory responseFactory = null;
	private transient Dataset dataset = null;	
	
	@Before
	public void setUp() {
		final XmlConfigUtil xmlConfigUtil = new XmlConfigUtil();
		xmlConfigUtil.setServletContext(new MockServletContext());
		
		responseFactory = new ResponseFactory();
		responseFactory.setXmlConfigUtil(xmlConfigUtil);
		
		dataset = new Dataset();
		
		dataset.setArachneId(new EntityId("test", 0L, 0L, false));
				
		dataset.setFields("test.Title", "Title of the Test");
		dataset.setFields("test.Subtitle", "Subtitle of the Test");
		
		dataset.setFields("test.DataPrefix", "success");
		dataset.setFields("test.DataPostfix", "PostfixTest");
		
		dataset.setFields("test.DataSeparatorBefore", "first");
		dataset.setFields("test.DataSeparatorAfter", "second");
		
		dataset.setFields("test.DataLink1", "http://testserver.com/link1.html");
		dataset.setFields("test.DataLink2", "link2");
		dataset.setFields("test.DataNoLink1", "Start");
		dataset.setFields("test.DataNoLink2", "End");
		
		dataset.setFields("test.facetTest", "TestFacet");
		
	}
	
	@After
	public void tearDown() {
		responseFactory = null;
	}
	
	@Test
	public void testCreateFormattedArachneEntity() {
		final FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(dataset);
		assertNotNull(response);
	}
	
	@Test
	public void testCreateResponseForDeletedEntity() {
		final EntityId deletedEntityId = new EntityId("test", 0L, 0L, true);
		final DeletedArachneEntity response = (DeletedArachneEntity)responseFactory.createResponseForDeletedEntity(deletedEntityId);
		assertNotNull(response);
		assertEquals("test", response.getType());
		assertEquals(Long.valueOf(0), response.getEntityId());
		assertEquals(Long.valueOf(0), response.getInternalId());
		assertEquals("This entity has been deleted.", response.getMessage());
	}
	
	@Test
	public void testType() {
		final FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(dataset);
		assertEquals("test", response.getType());
	}
	
	@Test
	public void testTitle() {
		final FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(dataset);
		assertEquals("Title of the Test", response.getTitle());
	}
	
	@Test
	public void testSubtitle() {
		final FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(dataset);
		assertEquals("Subtitle of the Test", response.getSubtitle());
	}
	
	@Test
	public void testDatasectionLabel() {
		final FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(dataset);
		assertEquals("Testdata", ((Section)response.getSections()).getLabel());
	}
	
	@Test
	public void testFieldPrefixPostfix() {
		final FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(dataset);
		final Section FirstInnerSection = (Section)(((Section)response.getSections()).getContent()).get(0);
		assertEquals("Testdata prefix/postfix", FirstInnerSection.getLabel());
		
		final Field concatenatedField = ((Field)FirstInnerSection.getContent().get(0));
		assertEquals("PrefixTest=success<br/>PostfixTest=success", concatenatedField.getValue());
	}
	
	@Test
	public void testFieldSeparator() {
		final FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(dataset);
		final Section SecondInnerSection = (Section)(((Section)response.getSections()).getContent()).get(1);
		assertEquals("Testdata separator", SecondInnerSection.getLabel());
		
		final Field concatenatedField = ((Field)SecondInnerSection.getContent().get(0));
		assertEquals("first-second", concatenatedField.getValue());
	}
	
	@Test
	public void testLinkField() {
		final FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(dataset);
		final Section ThirdInnerSection = (Section)(((Section)response.getSections()).getContent()).get(2);
		assertEquals("Testdata linkField", ThirdInnerSection.getLabel());
		
		final Field concatenatedField = ((Field)ThirdInnerSection.getContent().get(0));
		assertEquals("Start<br/><a href=\"http://testserver.com/link1.html\">TestLink1</a><br/><a href=\"" +
				"http://testserver.com/link2.html\">TestLink2</a><br/>End", concatenatedField.getValue());
	}
	
	/*
	@Test
	public void testFacets() {
		final FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(dataset);
		assertEquals("kategorie", response.getFacets().get(0).getName());
		assertEquals("test", response.getFacets().get(0).getValues().get(0));
		
		assertEquals("ifEmptyFacet", response.getFacets().get(1).getName());
		assertEquals("TestFacet", response.getFacets().get(1).getValues().get(0));
	}*/
	
	// TODO add test for context tag - the current context implementation makes it nearly impossible to test
}
