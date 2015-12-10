package de.uni_koeln.arachne.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.*;
import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;
// needed to use .andDo(print()) for debugging
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

import de.uni_koeln.arachne.dao.hibernate.CatalogDao;
import de.uni_koeln.arachne.dao.hibernate.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.hibernate.Catalog;
import de.uni_koeln.arachne.mapping.hibernate.CatalogEntry;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.testconfig.TestUsers;

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
	
	static final String EXPECTED_CATALOG_NO_CHILDS = "{\"id\":83,\"root\":{\"id\":597,\"children\":[{\"id\":594,\""
			+ "label\":\"Vorbebauung\",\"parentId\":597,\"indexParent\":0,\"catalogId\":83,\"hasChildren\":false},"
			+ "{\"id\":593,\"label\":\"Basilica Aemilia\",\"parentId\":597,\"indexParent\":1,\"catalogId\":83,\""
			+ "hasChildren\":true}],\"label\":\"Die Basilica Aemilia auf dem Forum Romanum in Rom: Brennpunkt "
			+ "des öffentlichen Lebens\",\"text\":\"Nach der Errichtung in den 60er Jahren des 2. Jhs. v. Chr. "
			+ "durch die beiden Konsuln M. Aemilius Lepidus und M. Fulvius Nobilior wurde die Basilica mehrmals "
			+ "zerstört [...]\",\"catalogId\":83,\"hasChildren\":true},\"author\":\"Testauthor\",\"public\":false}";
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		
		when(userRightsService.getCurrentUser()).thenReturn(TestUsers.getUser(), TestUsers.getAnonymous());
		when(userRightsService.isSignedInUser()).thenReturn(true, false);
		
		final Set<User> users = new HashSet<>();
		users.add(TestUsers.getUser());
				
		URL resource = TestCatalogController.class.getResource("/WEB-INF/json/catalog.json");
		ObjectMapper mapper = new ObjectMapper();
		
		// root - children removed
		Catalog catalog = mapper.readValue(Resources.toString(resource, Charsets.UTF_8), Catalog.class);
		catalog.setUsers(users);
		CatalogEntry entry = catalog.getRoot();
		entry.setCatalog(catalog);
		entry.removeChildren();
		
		// full root entry
		catalog = mapper.readValue(Resources.toString(resource, Charsets.UTF_8), Catalog.class);
		catalog.setUsers(users);
		CatalogEntry entryFull = catalog.getRoot();
		entryFull.setCatalog(catalog);
		
		// leaf entry
		catalog = mapper.readValue(Resources.toString(resource, Charsets.UTF_8), Catalog.class);
		catalog.setUsers(users);
		CatalogEntry entryLeaf = catalog.getRoot()
				.getChildren().get(1)
				.getChildren().get(0)
				.getChildren().get(0)
				.getChildren().get(0)
				.getChildren().get(0);
		entryLeaf.setParent(catalog.getRoot()
				.getChildren().get(1)
				.getChildren().get(0)
				.getChildren().get(0)
				.getChildren().get(0));
		entryLeaf.getParent().setCatalog(catalog);
		entryLeaf.setCatalog(catalog);
		
		when(catalogEntryDao.getByCatalogEntryId(1, false, 0, 0)).thenReturn(entry);
		when(catalogEntryDao.getByCatalogEntryId(1, true, 0, 0)).thenReturn(entryFull);
		when(catalogEntryDao.getByCatalogEntryId(598)).thenReturn(entryLeaf.getParent());
		when(catalogEntryDao.getByCatalogEntryId(599)).thenReturn(entryLeaf);
		when(catalogEntryDao.getByCatalogEntryId(600)).thenReturn(null);
		when(catalogEntryDao.updateCatalogEntry(any(CatalogEntry.class))).thenAnswer(new Answer<CatalogEntry>() {

			@Override
			public CatalogEntry answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (CatalogEntry) args[0];
			}
		});

		when(catalogDao.getByUid(3, true)).thenReturn(Arrays.asList(catalog));
		when(catalogDao.getByCatalogId(83)).thenReturn(catalog);
		when(catalogDao.getByCatalogId(83, true)).thenReturn(catalog);
		when(catalogDao.saveOrUpdateCatalog(any(Catalog.class))).thenAnswer(new Answer<Catalog>() {

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
		catalogNoChilds.setUsers(users);
		final CatalogEntry root = catalogNoChilds.getRoot();
		for (CatalogEntry catalogEntry : root.getChildren()) {
			catalogEntry.removeChildren();
		}
		
		when(catalogDao.getByUid(3, false)).thenReturn(Arrays.asList(catalogNoChilds));
		when(catalogDao.getByCatalogId(83, false)).thenReturn(catalogNoChilds);
		
		when(catalogEntryDao.getPrivateCatalogIdsByEntityId(anyLong()))
				.thenReturn(new ArrayList<Long>());
		when(catalogEntryDao.getPrivateCatalogIdsByEntityId(1184191))
				.thenReturn(Arrays.asList(83L), new ArrayList<Long>());
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
				get("/catalogentry/1")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"id\":597,\""
						+ "label\":\"Die Basilica Aemilia auf dem Forum Romanum in Rom: "
						+ "Brennpunkt des öffentlichen Lebens\",\""
						+ "text\":\"Nach der Errichtung in den 60er Jahren des 2. Jhs. v. Chr. durch "
						+ "die beiden Konsuln M. Aemilius Lepidus und M. Fulvius Nobilior wurde die "
						+ "Basilica mehrmals zerstört [...]\",\"catalogId\":83,\"hasChildren\":true})"));
		
		// forbidden
		mockMvc.perform(
				get("/catalogentry/1")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleGetCatalogEntryRequestValidFull() throws Exception {
		mockMvc.perform(
				get("/catalogentry/1?full=true")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"id\":597,\"children\":[{\"id\":594,\""
						+ "label\":\"Vorbebauung\",\"parentId\":597,\"indexParent\":0,\"catalogId\":83,\""
						+ "hasChildren\":false},{\"id\":593,\"children\":[{\"id\":595,\""
						+ "children\":[{\"id\":596,\"children\":[{\"id\":598,\""
						+ "children\":[{\"id\":599,\"arachneEntityId\":1184191,\""
						+ "label\":\"Fundamente der Innensäulen\",\"text\":\"Die Fundamente der Innensäulen.\",\""
						+ "parentId\":598,\"indexParent\":0,\"catalogId\":83,\"hasChildren\":false}],\""
						+ "label\":\"Fundamente\",\"parentId\":596,\"indexParent\":0,\"catalogId\":83,\""
						+ "hasChildren\":true}],\"label\":\"Republikanische Zeit\",\"parentId\":595,\""
						+ "indexParent\":0,\"catalogId\":83,\"hasChildren\":true}],\"label\":\"Aula\",\""
						+ "parentId\":593,\"indexParent\":0,\"catalogId\":83,\"hasChildren\":true}],\""
						+ "label\":\"Basilica Aemilia\",\"parentId\":597,\"indexParent\":1,\"catalogId\":83,\""
						+ "hasChildren\":true}],\"label\":\"Die Basilica Aemilia auf dem Forum Romanum in Rom: "
						+ "Brennpunkt des öffentlichen Lebens\",\"text\":\"Nach der Errichtung in den 60er Jahren "
						+ "des 2. Jhs. v. Chr. durch die beiden Konsuln M. Aemilius Lepidus und M. Fulvius "
						+ "Nobilior wurde die Basilica mehrmals zerstört [...]\",\"catalogId\":83,\""
						+ "hasChildren\":true}"));
	}
	
	@Test
	public void testHandleGetCatalogEntryRequestInvalidNotFound() throws Exception {
		mockMvc.perform(
				get("/catalogentry/2")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testHandleUpdateCatalogEntryRequestValid() throws Exception {
		mockMvc.perform(
				put("/catalogentry/599")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 599,\"children\": [],\"arachneEntityId\": 1184191,\""
							+ "label\": \"Fundamente der Innensäulen\", \"text\": \"Die Fundamente der Innensäulen.\", \""
							+ "parentId\": 598, \"indexParent\": 0,\"catalogId\": 83}"))
				.andExpect(status().isOk());
		
		// forbidden
		mockMvc.perform(
				put("/catalogentry/599")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 599,\"children\": [],\"arachneEntityId\": 1184191,\""
							+ "label\": \"Fundamente der Innensäulen\", \"text\": \"Die Fundamente der Innensäulen.\", \""
							+ "parentId\": 598, \"indexParent\": 0,\"catalogId\": 83}"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleUpdateCatalogEntryRequestInvalidEntryId() throws Exception {
		mockMvc.perform(
				put("/catalogentry/666")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 599,\"children\": [],\"arachneEntityId\": 1184191,\""
							+ "label\": \"Fundamente der Innensäulen\", \"text\": \"Die Fundamente der Innensäulen.\", \""
							+ "parentId\": 598, \"indexParent\": 0,\"catalogId\": 83}"))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testHandleUpdateCatalogEntryRequestInvalidWrongContentId() throws Exception {
		mockMvc.perform(
				put("/catalogentry/599")
					.contentType(APPLICATION_JSON_UTF8)
					.content("{\"id\": 666,\"children\": [],\"arachneEntityId\": 1184191,\""
							+ "label\": \"Fundamente der Innensäulen\", \"text\": \"Die Fundamente der Innensäulen.\", \""
							+ "parentId\": 598, \"indexParent\": 0,\"catalogId\": 83}"))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testHandleCatalogEntryDestroyRequestValid() throws Exception {
		mockMvc.perform(
				delete("/catalogentry/599")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNoContent());
		
		// forbidden
		mockMvc.perform(
				delete("/catalogentry/599")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testHandleCatalogEntryDestroyRequestInvalidId() throws Exception {
		mockMvc.perform(
				delete("/catalogentry/600")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNoContent());
	}
	
	@Test
	public void testHandleCatalogEntryCreateRequestValid() throws Exception {
		mockMvc.perform(
				post("/catalogentry")
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
				post("/catalogentry")
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
				post("/catalogentry")
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
				post("/catalogentry")
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
				put("/catalog/84")
					.contentType(APPLICATION_JSON_UTF8)
					.content(catalogString))
				.andExpect(status().isForbidden());
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
				get("/catalogByEntity/1184191")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"catalogIds\":[83]}"));
		
		// forbidden
		mockMvc.perform(
				get("/catalogByEntity/1184191")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{}"));
	}

	@Test
	public void testHandleGetCatalogByEntityRequestInvalidId() throws Exception {
		mockMvc.perform(
				get("/catalogByEntity/1184192")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{}"));
	}
}
