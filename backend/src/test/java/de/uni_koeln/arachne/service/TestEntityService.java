package de.uni_koeln.arachne.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.ResponseFactory;
import de.uni_koeln.arachne.service.Transl8Service.Transl8Exception;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

@RunWith(MockitoJUnitRunner.class)
public class TestEntityService {

	private transient EntityId testId;

	private final String LANG = "de";

	@Mock
	private EntityIdentificationService entityIdentificationService;
	
	@Mock
	private SingleEntityDataService singleEntityDataService;
	
	@Mock
	private UserRightsService userRightsService;
	
	@Mock
	private ESService esService;
	
	@Mock
	private ImageService imageService;
	
	@Mock
	private ContextService contextService;
	
	@Mock
	private ResponseFactory responseFactory;
	
	@InjectMocks
	private EntityService entityService = new EntityService(false, new String[] {"boost","connectedEntities","degree","fields"});
	
	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		final Dataset testDataset = TestData.getTestDataset();
				
		when(userRightsService.isDataimporter()).thenReturn(false, false, true);
		when(userRightsService.userHasDatasetGroup(null)).thenReturn(false, true);
		
		testId = TestData.getTestDataset().getArachneId();
		when(singleEntityDataService.getSingleEntityByArachneId(testId)).thenReturn(testDataset);
		
		when(responseFactory.createFormattedArachneEntityAsJsonString(testDataset, LANG))
			.thenReturn(TestData.jsonString);
		when(responseFactory.createFormattedArachneEntityAsJson(testDataset, LANG))
		.thenReturn(TestData.getTestJson());
		
		when(entityIdentificationService.getId(anyLong())).thenReturn(null);
		when(entityIdentificationService.getId(anyString(), anyLong())).thenReturn(null);
		when(entityIdentificationService.getId(0l)).thenReturn(testId);
		when(entityIdentificationService.getId(2l)).thenReturn(TestData.deletedEntity);
		when(entityIdentificationService.getId("test", 0l)).thenReturn(testId);
		
		when(esService.getDocumentFromCurrentIndex(anyLong(), anyString(), any(String[].class), anyString()))
			.thenReturn(new TypeWithHTTPStatus<String>(null, HttpStatus.NOT_FOUND));
		
		when(esService.getDocumentFromCurrentIndex(0l, null, new String[] {"boost","connectedEntities","degree","fields"}, LANG))
			.thenReturn(new TypeWithHTTPStatus<String>(HttpStatus.FORBIDDEN), new TypeWithHTTPStatus<String>("Test Doc", HttpStatus.OK));
		when(esService.getDocumentFromCurrentIndex(0l, "test", new String[] {"boost","connectedEntities","degree","fields"}, LANG))
			.thenReturn(new TypeWithHTTPStatus<String>("Test Doc", HttpStatus.OK));
	}

	@Test
	public void testGetEntityFromIndex() throws Transl8Exception {
		// get by entityId (forbidden)
		TypeWithHTTPStatus<String> result = entityService.getEntityFromIndex(0l, null, LANG);
		assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
		assertNull(result.getValue());

		// get by entityId
		result = entityService.getEntityFromIndex(0l, null, LANG);
		assertEquals(HttpStatus.OK, result.getStatus());
		assertNotNull(result.getValue());

		// get by category and interal key
		result = entityService.getEntityFromIndex(0l, "test", LANG);
		assertEquals(HttpStatus.OK, result.getStatus());
		assertNotNull(result.getValue());

		// entity does not exist (get by entityId)
		result = entityService.getEntityFromIndex(1l, null, LANG);
		assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
		assertNull(result.getValue());

		// entity does not exist (get by category and interal key)
		result = entityService.getEntityFromIndex(0l, "testo", LANG);
		assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
		assertNull(result.getValue());
				
		// entity is deleted (get by entityId)
		result = entityService.getEntityFromIndex(2l, null, LANG);
		assertEquals(HttpStatus.OK, result.getStatus());
		assertNull(result.getValue());
	}
	
	@Test
	public void testGetEntityFromDB() throws Transl8Exception {
		// get by entityId (forbidden)
		TypeWithHTTPStatus<String> result = entityService.getEntityFromDB(0l, null, LANG);
		assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
		assertNull(result.getValue());
		
		// get by entityId
		result = entityService.getEntityFromDB(0l, null, LANG);
		assertEquals(HttpStatus.OK, result.getStatus());
		assertNotNull(result.getValue());
		
		// get by category and interal key
		result = entityService.getEntityFromDB(0l, "test", LANG);
		assertEquals(HttpStatus.OK, result.getStatus());
		assertNotNull(result.getValue());
		
		// entity does not exist (get by entityId)
		result = entityService.getEntityFromDB(1l, null, LANG);
		assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
		assertNull(result.getValue());

		// entity does not exist (get by category and interal key)
		result = entityService.getEntityFromDB(0l, "testo", LANG);
		assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
		assertNull(result.getValue());
		
		// entity is deleted (get by entityId)
		result = entityService.getEntityFromDB(2l, null, LANG);
		assertEquals(HttpStatus.OK, result.getStatus());
		assertNull(result.getValue());
	}

	@Test
	public void testGetFormattedEntityByIdAsJsonString() throws Transl8Exception {
		// forbidden - neither has the user the correct datasetgroup nor is the dataimport calling the method
		assertEquals("forbidden", entityService.getFormattedEntityByIdAsJsonString(testId, LANG));
		
		// No need to test the concrete return value here as it is the one we inject into the ResponseFactory
		// user has the correct dataset group
		assertNotNull(entityService.getFormattedEntityByIdAsJsonString(testId, LANG));
		// method is called by the dataimporter
		assertNotNull(entityService.getFormattedEntityByIdAsJsonString(testId, LANG));
	}

	@Test
	public void testGetFormattedEntityByIdAsJson() throws Transl8Exception {
		// forbidden - neither has the user the correct datasetgroup nor is the dataimport calling the method
		assertNull(entityService.getFormattedEntityByIdAsJson(testId, LANG));

		// No need to test the concrete return value here as it is the one we inject into the ResponseFactory
		// user has the correct dataset group
		assertNotNull(entityService.getFormattedEntityByIdAsJson(testId, LANG));
		// method is called by the dataimporter
		assertNotNull(entityService.getFormattedEntityByIdAsJson(testId, LANG));
	}

}
