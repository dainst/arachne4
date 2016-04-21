package de.uni_koeln.arachne.response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.arachne.dao.jdbc.CatalogDao;
import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.service.Transl8Service;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.JSONUtil;
import de.uni_koeln.arachne.util.XmlConfigUtil;
import de.uni_koeln.arachne.util.sql.CatalogEntryInfo;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
public class TestResponseFactory { // NOPMD
	@Mock private GenericSQLDao genericSQLDao;
	@Mock private CatalogDao catalogDao;
	@Mock private Transl8Service ts;
	@Mock private JSONUtil jsonUtil;
	@InjectMocks private final ResponseFactory responseFactory = new ResponseFactory();
	
	@Mock private UserRightsService userRightsService;
	@InjectMocks private final XmlConfigUtil xmlConfigUtil = new XmlConfigUtil();
	
	private List<Long> mockIdList = null;
	
	private List<CatalogEntryInfo> mockCatalogDataList = null;
		
	private Dataset dataset = new TestData().getTestDataset();	
	
	@Before
	public void setUp() {
		xmlConfigUtil.setServletContext(new MockServletContext());
		
		responseFactory.setXmlConfigUtil(xmlConfigUtil);
				
		mockIdList = new ArrayList<Long>();
		for (long i = 1; i < 6; i++) {
			mockIdList.add(i);
		}
		when(genericSQLDao.getConnectedEntityIds(0)).thenReturn(mockIdList);
		
		mockCatalogDataList = new ArrayList<CatalogEntryInfo>();
		mockCatalogDataList.add(new CatalogEntryInfo(1L, "", 1L));
		for (int i = 2; i < 6; i++) {
			final CatalogEntryInfo mockCatalogData = 
					new CatalogEntryInfo(i, (String)mockCatalogDataList.get(i-2).getPath(), i);
			mockCatalogDataList.add(mockCatalogData);
		}
		when(catalogDao.getPublicCatalogIdsAndPathsByEntityId(0)).thenReturn(mockCatalogDataList);
		when(ts.transl8(anyString())).thenReturn("type_test");
		when(ts.transl8Facet(anyString(), anyString())).then(AdditionalAnswers.returnsSecondArg());
		when(jsonUtil.getObjectMapper()).thenReturn(new ObjectMapper());
		
		when(userRightsService.userHasAtLeastGroupID(anyInt())).thenReturn(true, false);
	}
	
	@Test
	public void testCreateFormattedArachneEntity() {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertNotNull(response);
	}
	
	@Test
	public void testCreateResponseForDeletedEntity() throws JsonParseException, JsonMappingException, IOException {
		final EntityId deletedEntityId = new EntityId("test", 0L, 0L, true, 0L);
		final String response = responseFactory.createResponseForDeletedEntityAsJsonString(deletedEntityId);
		final ObjectMapper objectMapper = new ObjectMapper();
		DeletedArachneEntity deletedEntity = objectMapper.readValue(response, DeletedArachneEntity.class);
		assertNotNull(deletedEntity);
		assertEquals("test", deletedEntity.getType());
		assertEquals(Long.valueOf(0), deletedEntity.getEntityId());
		assertEquals(Long.valueOf(0), deletedEntity.getInternalId());
		assertEquals("This entity has been deleted.", deletedEntity.getMessage());
	}
	
	@Test
	public void testType() {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"type\":\"type_test\""));
	}
	
	@Test
	public void testTitle() {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"title\":\"Title of the Test\""));
	}
	
	@Test
	public void testSubtitle() {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"subtitle\":\"Subtitle of the Test\""));
	}
	
	@Test
	public void testDatasectionLabel() {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"label\":\"Testdata\""));
	}
	
	@Test
	public void testFieldPrefixPostfix() {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"label\":\"Testdata prefix/postfix\""));
		assertTrue(response.contains("\"content\":[{\"value\":\"PrefixTest=success<hr>PostfixTest=success\"}]"));
	}
	
	@Test
	public void testFieldSeparator() {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"label\":\"Testdata separator\""));
		assertTrue(response.contains("\"content\":[{\"value\":\"first-second\"}]"));
	}
	
	@Test
	public void testLinkField() {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"label\":\"Testdata linkField\""));
		assertTrue(response.contains("\"content\":[{\"value\":\"Start<hr>"
				+ "<a href=\\\"http://testserver.com/link1.html\\\" target=\\\"_blank\\\">TestLink1</a>-TestLinkOverride-"
				+ "<a href=\\\"http://testserver.com/link2.html\\\" target=\\\"_blank\\\">TestLink2</a><hr>End\"}]"));
	}
	
	@Test
	public void testEditorSection() {
		String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		// user is an editor
		assertTrue(response.contains("\"editorSection\":{\"label\":\"Testdata Editor Section\",\"content\":"
				+ "[{\"value\":\"for editors only\"}]}"));
		// user is not an editor
		response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertFalse(response.contains("\"editorSection\":{\"label\":\"Testdata Editor Section\",\"content\":"
				+ "[{\"value\":\"for editors only\"}]}"));
	}
	
	@Test
	public void testDynamicFacets() {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"facet_kategorie\":[\"test\"]"));
		assertTrue(response.contains("\"facet_test\":[\"test facet value\"]"));
		assertTrue(response.contains("\"facet_multivaluetest\":[\"value 1\",\"value 2\",\"value 3\"]"));
	}
	
	@Test
	public void testStaticFacets() {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"facet_image\":[\"nein\"]"));
	}
	
	// TODO add test for context tag - the current context implementation makes it nearly impossible to test
}
