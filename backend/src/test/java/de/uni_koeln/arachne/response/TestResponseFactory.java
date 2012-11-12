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
@TestExecutionListeners( { WebContextTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class })
public class TestResponseFactory {
	private transient ResponseFactory responseFactory;
	private transient FormattedArachneEntity response;	
	
	@Before
	public void setUp() {
		final XmlConfigUtil xmlConfigUtil = new XmlConfigUtil();
		xmlConfigUtil.setServletContext(new MockServletContext());
		
		responseFactory = new ResponseFactory();
		responseFactory.setXmlConfigUtil(xmlConfigUtil);
		
		final Dataset dataset = new Dataset();
		
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
		
		response = responseFactory.createFormattedArachneEntity(dataset);
	}
	
	@After
	public void tearDown() {
		responseFactory = null;
	}
	
	@Test
	public void testCreateFormattedArachneEntity() {
		assertNotNull(response);
	}
	
	@Test
	public void testType() {
		assertEquals("test", response.getType());
	}
	
	@Test
	public void testTitle() {
		assertEquals("Title of the Test", response.getTitle());
	}
	
	@Test
	public void testSubtitle() {
		assertEquals("Subtitle of the Test", response.getSubtitle());
	}
	
	@Test
	public void testDatasectionLabel() {
		assertEquals("Testdata", ((Section)response.getSections()).getLabel());
	}
	
	@Test
	public void testFieldPrefixPostfix() {
		final Section FirstInnerSection = (Section)(((Section)response.getSections()).getContent()).get(0);
		assertEquals("Testdata prefix/postfix", FirstInnerSection.getLabel());
		
		final Field concatenatedField = ((Field)FirstInnerSection.getContent().get(0));
		assertEquals("PrefixTest=success<br/>PostfixTest=success", concatenatedField.getValue());
	}
	
	@Test
	public void testFieldSeparator() {
		final Section SecondInnerSection = (Section)(((Section)response.getSections()).getContent()).get(1);
		assertEquals("Testdata separator", SecondInnerSection.getLabel());
		
		final Field concatenatedField = ((Field)SecondInnerSection.getContent().get(0));
		assertEquals("first-second", concatenatedField.getValue());
	}
	
	@Test
	public void testLinkField() {
		final Section ThirdInnerSection = (Section)(((Section)response.getSections()).getContent()).get(2);
		assertEquals("Testdata linkField", ThirdInnerSection.getLabel());
		
		final Field concatenatedField = ((Field)ThirdInnerSection.getContent().get(0));
		assertEquals("Start<br/><a href=\"http://testserver.com/link1.html\">TestLink1</a><br/><a href=\"" +
				"http://testserver.com/link2.html\">TestLink2</a><br/>End", concatenatedField.getValue());
	}
}
