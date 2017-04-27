package de.uni_koeln.arachne.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.*;
import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;
// needed to use .andDo(print()) for debugging
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import de.uni_koeln.arachne.util.sql.CatalogEntryExtended;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import de.uni_koeln.arachne.dao.jdbc.CatalogDao;
import de.uni_koeln.arachne.dao.jdbc.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.testconfig.TestUserData;

@RunWith(MockitoJUnitRunner.class)
public class TestCatalogController {

	@Mock
	private UserRightsService userRightsService;
	
	@Mock
	private CatalogEntryDao catalogEntryDao;
	
	@Mock
	private CatalogDao catalogDao;
	
	@InjectMocks
	private CatalogController controller;
	
	private MockMvc mockMvc;
	
	static final String EXPECTED_CATALOG_NO_CHILDS = "{\"id\":83,\"root\":{\"id\":597,\"label\":\"Die Basilica Aemilia "
			+ "auf dem Forum Romanum in Rom: Brennpunkt des öffentlichen Lebens\",\"text\":\"Nach der Errichtung in den"
			+ " 60er Jahren des 2. Jhs. v. Chr. durch die beiden Konsuln M. Aemilius Lepidus und M. Fulvius Nobilior "
			+ "wurde die Basilica mehrmals zerstört [...]\",\"catalogId\":83,\"totalChildren\":2},\"author\":"
			+ "\"Testauthor\",\"public\":false}";

	static final String EXPECTED_CATALOG_LIST = "{\"entry\":{\"id\":599,\"arachneEntityId\":1184191,\"label\":" +
			"\"Fundamente der Innensäulen\",\"text\":\"Die Fundamente der Innensäulen.\",\"parentId\":598," +
			"\"indexParent\":0,\"catalogId\":83,\"totalChildren\":0},\"catalogTitle\":\"Die Basilica Aemilia auf dem " +
			"Forum Romanum in Rom: Brennpunkt des öffentlichen Lebens\",\"catalogAuthor\":\"Testauthor\",\"public\":false}";
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		
		when(userRightsService.getCurrentUser()).thenReturn(TestUserData.getUser(), TestUserData.getAnonymous());
		when(userRightsService.isSignedInUser()).thenReturn(true, false);
		
		final Set<Long> users = new HashSet<Long>();
		users.add(TestUserData.getUser().getId());
				
		URL resource = TestCatalogController.class.getResource("/WEB-INF/json/catalog.json");
		ObjectMapper mapper = new ObjectMapper();
		
		// root - children removed
		Catalog catalog = mapper.readValue(Resources.toString(resource, Charsets.UTF_8), Catalog.class);
		catalog.setUserIds(users);
		CatalogEntry entry = catalog.getRoot();
		entry.setCatalogId(catalog.getId());
		entry.setTotalChildren(entry.getChildren().size());
		entry.setChildren(null);
		
		// full root entry
		catalog = mapper.readValue(Resources.toString(resource, Charsets.UTF_8), Catalog.class);
		catalog.setUserIds(users);
		CatalogEntry entryFull = catalog.getRoot();
		entryFull.setCatalogId(catalog.getId());
		
		// leaf entry
		catalog = mapper.readValue(Resources.toString(resource, Charsets.UTF_8), Catalog.class);
		catalog.setUserIds(users);
		CatalogEntry entryLeaf = catalog.getRoot()
				.getChildren().get(1)
				.getChildren().get(0)
				.getChildren().get(0)
				.getChildren().get(0)
				.getChildren().get(0);
		CatalogEntry parent = catalog.getRoot()
				.getChildren().get(1)
				.getChildren().get(0)
				.getChildren().get(0)
				.getChildren().get(0);
		parent.setCatalogId(catalog.getId());
		entryLeaf.setCatalogId(catalog.getId());
		
		when(catalogEntryDao.getById(1, false, -1, 0)).thenReturn(entry);
		when(catalogEntryDao.getById(1, true, -1, 0)).thenReturn(entryFull);
		when(catalogEntryDao.getById(598)).thenReturn(parent);
		when(catalogEntryDao.getById(599)).thenReturn(entryLeaf);
		when(catalogEntryDao.getById(600)).thenReturn(null);
		when(catalogEntryDao.updateCatalogEntry(any(CatalogEntry.class))).thenAnswer(new Answer<CatalogEntry>() {

			@Override
			public CatalogEntry answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (CatalogEntry) args[0];
			}
		});

		when(catalogDao.getByUserId(3, true, -1, 0)).thenReturn(Arrays.asList(catalog));
		when(catalogDao.getById(83)).thenReturn(catalog);
		when(catalogDao.getById(83, true, -1, 0)).thenReturn(catalog);
		when(catalogDao.updateCatalog(any(Catalog.class))).thenAnswer(new Answer<Catalog>() {

			@Override
			public Catalog answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (Catalog) args[0];
			}
		});
		
		when(catalogDao.saveCatalog(any(Catalog.class))).thenAnswer(new Answer<Catalog>() {

			@Override
			public Catalog answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (Catalog) args[0];
			}
		});
		
		// catalog children removed
		final Catalog catalogNoChilds = mapper.readValue(
				Resources.toString(resource, Charsets.UTF_8), Catalog.class);
		catalogNoChilds.setUserIds(users);
		final CatalogEntry root = catalogNoChilds.getRoot();
		for (CatalogEntry catalogEntry : root.getChildren()) {
            final List<CatalogEntry> children = catalogEntry.getChildren();
			final int childCount = (children != null) ? children.size() : 0;
            catalogEntry.setTotalChildren(childCount);
			catalogEntry.setChildren(null);
		}
		
		when(catalogDao.getByUserId(3, false, -1, 0)).thenReturn(Arrays.asList(catalogNoChilds));
		when(catalogDao.getById(83, false, -1, 0)).thenReturn(catalogNoChilds);

		final CatalogEntryExtended info = new CatalogEntryExtended(
				entryLeaf,
				catalog.getRoot().getLabel(),
				catalog.getAuthor(),
				catalog.getProjectId(),
				catalog.isPublic());
		when(catalogEntryDao.getEntryInfoByEntityId(1184191))
				.thenReturn(Arrays.asList(info), new ArrayList<CatalogEntryExtended>());

	}
	
	private String getCatalogAsJSONString()
			throws IOException, JsonParseException, JsonMappingException, JsonProcessingException {
		final URL resource = TestCatalogController.class.getResource("/WEB-INF/json/catalog.json");
		final ObjectMapper mapper = new ObjectMapper();
		final Catalog expectedCatalog = mapper.readValue(Resources.toString(resource, Charsets.UTF_8)
				, Catalog.class);
		final String expectedCatalogString = mapper.writeValueAsString(expectedCatalog);
		return expectedCatalogString;
	}
	
	@Test
	public void testHandleGetCatalogEntryRequestValid() throws Exception {
		mockMvc.perform(
				get("/catalog/entry/1")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"id\":597,\""
						+ "label\":\"Die Basilica Aemilia auf dem Forum Romanum in Rom: "
						+ "Brennpunkt des öffentlichen Lebens\",\""
						+ "text\":\"Nach der Errichtung in den 60er Jahren des 2. Jhs. v. Chr. durch "
						+ "die beiden Konsuln M. Aemilius Lepidus und M. Fulvius Nobilior wurde die "
						+ "Basilica mehrmals zerstört [...]\",\"catalogId\":83,\"totalChildren\":2})"));
		
		// forbidden
		mockMvc.perform(
				get("/catalog/entry/1")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleGetCatalogEntryRequestValidFull() throws Exception {
		mockMvc.perform(
				get("/catalog/entry/1?full=true")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"id\":597,\"children\":[{\"id\":594,\""
						+ "label\":\"Vorbebauung\",\"parentId\":597,\"indexParent\":0,\"catalogId\":83,\""
						+ "totalChildren\":0},{\"id\":593,\"children\":[{\"id\":595,\""
						+ "children\":[{\"id\":596,\"children\":[{\"id\":598,\""
						+ "children\":[{\"id\":599,\"arachneEntityId\":1184191,\""
						+ "label\":\"Fundamente der Innensäulen\",\"text\":\"Die Fundamente der Innensäulen.\",\""
						+ "parentId\":598,\"indexParent\":0,\"catalogId\":83,\"totalChildren\":0}],\""
						+ "label\":\"Fundamente\",\"parentId\":596,\"indexParent\":0,\"catalogId\":83,\""
						+ "totalChildren\":1}],\"label\":\"Republikanische Zeit\",\"parentId\":595,\""
						+ "indexParent\":0,\"catalogId\":83,\"totalChildren\":1}],\"label\":\"Aula\",\""
						+ "parentId\":593,\"indexParent\":0,\"catalogId\":83,\"totalChildren\":1}],\""
						+ "label\":\"Basilica Aemilia\",\"parentId\":597,\"indexParent\":1,\"catalogId\":83,\""
						+ "totalChildren\":1}],\"label\":\"Die Basilica Aemilia auf dem Forum Romanum in Rom: "
						+ "Brennpunkt des öffentlichen Lebens\",\"text\":\"Nach der Errichtung in den 60er Jahren "
						+ "des 2. Jhs. v. Chr. durch die beiden Konsuln M. Aemilius Lepidus und M. Fulvius "
						+ "Nobilior wurde die Basilica mehrmals zerstört [...]\",\"catalogId\":83,\""
						+ "totalChildren\":2}"));
	}
	
	@Test
	public void testHandleGetCatalogEntryRequestInvalidNotFound() throws Exception {
		mockMvc.perform(
				get("/catalog/entry/2")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testHandleUpdateCatalogEntryRequestValid() throws Exception {
		mockMvc.perform(
				put("/catalog/entry/599")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 599,\"children\": [],\"arachneEntityId\": 1184191,\""
							+ "label\": \"Fundamente der Innensäulen\", \"text\": \"Die Fundamente der Innensäulen.\", \""
							+ "parentId\": 598, \"indexParent\": 0,\"catalogId\": 83}"))
				.andExpect(status().isOk());
		
		// forbidden
		mockMvc.perform(
				put("/catalog/entry/599")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 599,\"children\": [],\"arachneEntityId\": 1184191,\""
							+ "label\": \"Fundamente der Innensäulen\", \"text\": \"Die Fundamente der Innensäulen.\", \""
							+ "parentId\": 598, \"indexParent\": 0,\"catalogId\": 83}"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleUpdateCatalogEntryRequestInvalidEntryId() throws Exception {
		mockMvc.perform(
				put("/catalog/entry/666")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 599,\"children\": [],\"arachneEntityId\": 1184191,\""
							+ "label\": \"Fundamente der Innensäulen\", \"text\": \"Die Fundamente der Innensäulen.\", \""
							+ "parentId\": 598, \"indexParent\": 0,\"catalogId\": 83}"))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testHandleUpdateCatalogEntryRequestInvalidWrongContentId() throws Exception {
		mockMvc.perform(
				put("/catalog/entry/599")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 666,\"children\": [],\"arachneEntityId\": 1184191,\""
							+ "label\": \"Fundamente der Innensäulen\", \"text\": \"Die Fundamente der Innensäulen.\", \""
							+ "parentId\": 598, \"indexParent\": 0,\"catalogId\": 83}"))
				.andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void testHandleCatalogEntryDestroyRequestValid() throws Exception {
		mockMvc.perform(
				delete("/catalog/entry/599")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNoContent());
		
		// forbidden
		mockMvc.perform(
				delete("/catalog/entry/599")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleCatalogEntryDestroyRequestInvalidId() throws Exception {
		mockMvc.perform(
				delete("/catalog/entry/600")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNoContent());
	}
	
	@Test
	public void testHandleCatalogEntryCreateRequestValid() throws Exception {
		mockMvc.perform(
				post("/catalog/entry")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 599, \""
							+ "label\": \"Test Label\",\""
							+ "text\": \"Test Text.\",\""
							+ "parentId\": 598,\""
							+ "indexParent\": 0,\""
							+ "catalogId\": 83}"))
				.andExpect(status().isOk());
		
		// forbidden
		mockMvc.perform(
				post("/catalog/entry")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 599, \""
							+ "label\": \"Test Label\",\""
							+ "text\": \"Test Text.\",\""
							+ "parentId\": 598,\""
							+ "indexParent\": 0,\""
							+ "catalogId\": 83}"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleCatalogEntryCreateRequestInvalidParent() throws Exception {
		mockMvc.perform(
				post("/catalog/entry")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 599, \""
							+ "label\": \"Test Label\",\""
							+ "text\": \"Test Text.\",\""
							+ "parentId\": 600,\""
							+ "indexParent\": 0,\""
							+ "catalogId\": 83}"))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testHandleCatalogEntryCreateRequestInvalidParentIdMissing() throws Exception {
		mockMvc.perform(
				post("/catalog/entry")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 599, \""
							+ "label\": \"Test Label\",\""
							+ "text\": \"Test Text.\",\""
							+ "indexParent\": 0,\""
							+ "catalogId\": 83}"))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testHandleGetCatalogsRequestValid() throws Exception {
		final String expectedResult = '[' + EXPECTED_CATALOG_NO_CHILDS + ']';
		
		mockMvc.perform(
				get("/catalog")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(expectedResult));
		
		// forbidden
		mockMvc.perform(
				get("/catalog")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isForbidden());
	}

	@Test
	public void testHandleGetCatalogsRequestValidFull() throws Exception {
		final String expectedResult = '[' + getCatalogAsJSONString() + ']'; 
		
		mockMvc.perform(
				get("/catalog?full=true")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(expectedResult));
	}
	
	@Test
	public void testHandleGetCatalogRequestValid() throws Exception {				
		mockMvc.perform(
				get("/catalog/83")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(EXPECTED_CATALOG_NO_CHILDS));
		
		// forbidden
		mockMvc.perform(
				get("/catalog/83")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleGetCatalogRequestValidFull() throws Exception {
		final String expectedCatalogString = getCatalogAsJSONString();
		
		mockMvc.perform(
				get("/catalog/83?full=true")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(expectedCatalogString));
	}

	@Test
	public void testHandleGetCatalogRequestInvalidId() throws Exception {
		mockMvc.perform(
				get("/catalog/84")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testHandleCatalogUpdateRequestValid() throws Exception {
		final String catalogString = getCatalogAsJSONString();
		
		mockMvc.perform(
				put("/catalog/83")
					.contentType(APPLICATION_JSON_UTF8)
					.content(catalogString))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(catalogString));
		
		// forbidden
		mockMvc.perform(
				put("/catalog/83")
					.contentType(APPLICATION_JSON_UTF8)
					.content(catalogString))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleCatalogUpdateRequestInvalidId() throws Exception {
		final String catalogString = getCatalogAsJSONString();
		
		mockMvc.perform(
				put("/catalog/84") // <- non existent catalogue
					.contentType(APPLICATION_JSON_UTF8)
					.content(catalogString))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testHandleCatalogCreateRequestValid() throws Exception {
		final URL contentRes = TestCatalogController.class.getResource("/WEB-INF/json/catalogId183.json");
		final ObjectMapper mapper = new ObjectMapper();
		final Catalog contentCatalog = mapper.readValue(Resources.toString(contentRes, Charsets.UTF_8)
				, Catalog.class);
		final String contentCatalogString = mapper.writeValueAsString(contentCatalog);
		
		final URL responseRes = TestCatalogController.class.getResource("/WEB-INF/json/catalogId183Response.json");
		final Catalog responseCatalog = mapper.readValue(Resources.toString(responseRes, Charsets.UTF_8)
				, Catalog.class);
		final String responseCatalogString = mapper.writeValueAsString(responseCatalog);
		
		mockMvc.perform(
				post("/catalog")
					.contentType(APPLICATION_JSON_UTF8)
					.content(contentCatalogString))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(responseCatalogString));
		
		// forbidden
		mockMvc.perform(
				post("/catalog")
					.contentType(APPLICATION_JSON_UTF8)
					.content(contentCatalogString))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleCatalogDestroyRequestValid() throws Exception {
		mockMvc.perform(
				delete("/catalog/83")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNoContent());
		
		// forbidden
		mockMvc.perform(
				delete("/catalog/83")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleCatalogDestroyRequestInvalidId() throws Exception {
		mockMvc.perform(
				delete("/catalog/84")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNoContent());
	}

	@Test
	public void testHandleGetCatalogByEntityRequestValid() throws Exception {
		mockMvc.perform(
				get("/catalog/list/1184191")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("[" + EXPECTED_CATALOG_LIST + "]"));
		
		// forbidden
		mockMvc.perform(
				get("/catalog/list/1184191")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("[]"));
	}

	@Test
	public void testHandleGetCatalogByEntityRequestInvalidId() throws Exception {
		mockMvc.perform(
				get("/catalog/list/1184192")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("[]"));
	}
}