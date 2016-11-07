package de.uni_koeln.arachne.response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.arachne.util.sql.CatalogEntryInfo;
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
import de.uni_koeln.arachne.service.Transl8Service.Transl8Exception;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.JSONUtil;
import de.uni_koeln.arachne.util.XmlConfigUtil;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
public class TestResponseFactory { // NOPMD
	@Mock private GenericSQLDao genericSQLDao;
	@Mock private CatalogDao catalogDao;
	@Mock private Transl8Service ts;
	@Mock private JSONUtil jsonUtil;
	@InjectMocks private final ResponseFactory responseFactory = new ResponseFactory("testAddress.com");
	
	@Mock private UserRightsService userRightsService;
	@InjectMocks private final XmlConfigUtil xmlConfigUtil = new XmlConfigUtil();
	
	private List<Long> mockIdList = null;
	
	private List<CatalogEntryInfo> mockCatalogDataList = null;
		
	private Dataset dataset = new TestData().getTestDataset();	
	
	@Before
	public void setUp() throws Transl8Exception {
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
	public void testCreateFormattedArachneEntity() throws Transl8Exception {
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
	public void testType() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"type\":\"type_test\""));
	}
	
	@Test
	public void testIds() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"ids\":[\"1\",\"123\",\"1234567890\",\"a1b2c3d4\"]"));
	}
	
	@Test
	public void testFilename() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"filename\":\"test_filename.ext\""));
	}
	
	@Test
	public void testTitle() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"title\":\"Title of the Test\""));
	}
	
	@Test
	public void testSubtitle() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"subtitle\":\"Subtitle of the Test\""));
	}
	
	@Test
	public void testDatasectionLabel() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"label\":\"Testdata\""));
	}
	
	@Test
	public void testFieldPrefixPostfix() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"label\":\"Testdata prefix/postfix\""));
		assertTrue(response.contains("\"content\":[{\"value\":\"PrefixTest=success<hr>PostfixTest=success\"}]"));
	}
	
	@Test
	public void testFieldSeparator() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"label\":\"Testdata separator\""));
		assertTrue(response.contains("\"content\":[{\"value\":\"first-second\"}]"));
	}
	
	@Test
	public void testLinkField() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"label\":\"Testdata linkField\""));
		assertTrue(response.contains("\"content\":[{\"value\":\"Start<hr>"
				+ "<a href=\\\"http://testserver.com/link1.html\\\" target=\\\"_blank\\\">TestLink1</a>-TestLinkOverride-"
				+ "<a href=\\\"http://testserver.com/link2.html\\\" target=\\\"_blank\\\">TestLink2</a><hr>End\"}]"));
	}
	
	@Test
	public void testEditorSection() throws Transl8Exception {
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
	public void testDynamicFacets() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		System.out.println(response);
		assertTrue(response.contains("\"facet_kategorie\":[\"test\"]"));
		assertTrue(response.contains("\"facet_test\":[\"test facet value\"]"));
		assertTrue(response.contains("\"facet_multivaluetest\":[\"value 1\",\"value 2\",\"value 3\"]"));
		assertTrue(response.contains("\"facet_includetest\":[\"include value 1\",\"include value 2\"]"));
	}
	
	@Test
	public void testStaticFacets() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset);
		assertTrue(response.contains("\"facet_image\":[\"nein\"]"));
	}

	// TODO add test for context tag - the current context implementation makes it nearly impossible to test
}
