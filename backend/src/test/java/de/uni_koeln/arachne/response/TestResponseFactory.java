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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.arachne.dao.jdbc.CatalogDao;
import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.service.Transl8Service;
import de.uni_koeln.arachne.service.Transl8Service.Transl8Exception;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.XmlConfigUtil;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations={"classpath:test-context.xml"}) 
public class TestResponseFactory { // NOPMD
	@Mock 
	private GenericSQLDao genericSQLDao;
	
	@Mock 
	private CatalogDao catalogDao;
	
	@Mock 
	private Transl8Service ts;
	
	@Mock
	private CustomBooster customBooster;

	private final String LANG = "de";

	@SuppressWarnings("serial")
	@InjectMocks 
	private final ResponseFactory responseFactory 
			= new ResponseFactory("testAddress.com", new ArrayList<String>() {{ add("facet_test"); }}, 2);
	
	@Mock 
	private UserRightsService userRightsService;
	
	@InjectMocks 
	private final XmlConfigUtil xmlConfigUtil = new XmlConfigUtil();
	
	private List<Long> mockIdList = null;
	
	private List<CatalogEntryInfo> mockCatalogDataList = null;
		
	private Dataset dataset = TestData.getTestDataset();	
	
	private ObjectMapper jsonMapper = new ObjectMapper();
	
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
		
		when(ts.transl8(anyString(), anyString())).thenReturn("type_test");
		when(ts.transl8Facet(anyString(), anyString(), anyString())).then(AdditionalAnswers.returnsSecondArg());
		
		when(userRightsService.userHasAtLeastGroupID(anyInt())).thenReturn(true, false);
		
		when(customBooster.getCategoryBoost(anyString())).thenReturn(1.0D);
		when(customBooster.getSingleEntityBoosts(anyLong())).thenReturn(1.0D);
	}
	
	@Test
	public void testCreateFormattedArachneEntity() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
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
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"type\":\"type_test\""));
	}
	
	@Test
	public void testIds() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"ids\":[\"1\",\"123\",\"1234567890\",\"a1b2c3d4\"]"));
	}
	
	@Test
	public void testFilename() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"filename\":\"test_filename.ext\""));
	}
	
	@Test
	public void testTitle() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"title\":\"Title of the Test\""));
	}
	
	@Test
	public void testSubtitle() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"subtitle\":\"Subtitle of the Test\""));
	}
	
	@Test
	public void testDatasectionLabel() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"label\":\"type_test\""));
	}
	
	@Test
	public void testFieldPrefixPostfix() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"label\":\"type_test\""));
		assertTrue(response.contains("\"content\":[{\"value\":\"PrefixTest=success<hr>PostfixTest=success\"}]"));
	}
	
	@Test
	public void testFieldSeparator() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"label\":\"type_test\""));
		assertTrue(response.contains("\"content\":[{\"value\":\"first-second\"}]"));
	}
	
	@Test
	public void testLinkField() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"label\":\"type_test\""));
		assertTrue(response.contains("\"content\":[{\"value\":\"Start<hr>"
				+ "<a href=\\\"http://testserver.com/link1.html\\\" target=\\\"_blank\\\">type_test</a>-TestLinkOverride-"
				+ "<a href=\\\"http://testserver.com/link2.html\\\" target=\\\"_blank\\\">type_test</a><hr>End\"}]"));
	}
	
	@Test
	public void testEditorSection() throws Transl8Exception {
		String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		// user is an editor
		assertTrue(response.contains("\"editorSection\":{\"label\":\"type_test\",\"content\":"
				+ "[{\"value\":\"for editors only\"}]}"));
		// user is not an editor
		response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertFalse(response.contains("\"editorSection\":{\"label\":\"type_test\",\"content\":"
				+ "[{\"value\":\"for editors only\"}]}"));
	}
	
	@Test
	public void testImages() throws Transl8Exception, JsonProcessingException {
		final String expectedJson = jsonMapper.writeValueAsString(dataset.getImages().subList(0, 2));
		String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"imageSize\":4"));
		assertTrue(response.contains(expectedJson));
	}
	
	@Test
	public void testDynamicFacets() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"facet_kategorie\":[\"test\"]"));
		assertTrue(response.contains("\"facet_test\":[\"test facet value\"]"));
		assertTrue(response.contains("\"facet_multivaluetest\":[\"value 1\",\"value 2\",\"value 3\"]"));
		assertTrue(response.contains("\"facet_includetest\":[\"include value 1\",\"include value 2\"]"));
	}
	
	@Test
	public void testStaticFacets() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("\"facet_image\":[\"ja\"]"));
	}

	@Test
	public void testContext() throws Transl8Exception {
		final String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertTrue(response.contains("Test Context Value1-TestSeparator1-Test Context Value3<hr>Test Context Value4"
				+"<hr>Test Context Value5-TestSeparator2-Test Context Value6"
				+"-TestSeparator3-Test Context Value7"));
	}
	
	/**
	 * Tests if the suggest field is correctly added to the JSON object.
	 * @throws Transl8Exception Should never been thrown.
	 */
	@Test
	public void testSuggest() throws Transl8Exception {
		String response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		// datasetGroup = 'Arachne'
		assertTrue(response.contains("\"suggest\":{\"input\":[\"Title of the Test\",\"test facet value\"],\"weight\":146}"));
		
		// datasetGroup != 'Arachne'
		String tableName = dataset.getArachneId().getTableName();
		String datasetGroupFieldName = tableName+".DatensatzGruppe"+tableName.substring(0,1).toUpperCase()
				+tableName.substring(1);
		dataset.getFields().put(datasetGroupFieldName, "NotArachne");
		response = responseFactory.createFormattedArachneEntityAsJsonString(dataset, LANG);
		assertFalse(response.contains("\"suggest\":{\"input\":[\"Title of the Test\",\"test facet value\"],\"weight\":146}"));
	}
}
