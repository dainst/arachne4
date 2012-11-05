package de.uni_koeln.arachne.response;

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
		
	@Before
	public void setUp() {
		final XmlConfigUtil xmlConfigUtil = new XmlConfigUtil();
		xmlConfigUtil.setServletContext(new MockServletContext());
		
		responseFactory = new ResponseFactory();
		responseFactory.setXmlConfigUtil(xmlConfigUtil);
	}
	
	@After
	public void tearDown() {
		responseFactory = null;
	}
	
	@Test
	public void testCreateFormattedArachneEntity() {
		final Dataset dataset = new Dataset();
		
		dataset.setArachneId(new EntityId("test", 0L, 0L, false));
		
		final FormattedArachneEntity response = responseFactory.createFormattedArachneEntity(dataset);
		assertNotNull(response);
	}
}
