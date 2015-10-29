package de.uni_koeln.arachne.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.*;
import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;
// needed to use .andDo(print()) for debugging
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.net.URL;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

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
	
	@InjectMocks
	private CatalogController controller;
	
	private MockMvc mockMvc;
	
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
		entryLeaf.setCatalog(catalog);
		
		when(catalogEntryDao.getByCatalogEntryId(1, false)).thenReturn(entry);
		when(catalogEntryDao.getByCatalogEntryId(1, true)).thenReturn(entryFull);
		when(catalogEntryDao.getByCatalogEntryId(599)).thenReturn(entryLeaf);
		when(catalogEntryDao.updateCatalogEntry(any(CatalogEntry.class))).thenAnswer(new Answer<CatalogEntry>() {

			@Override
			public CatalogEntry answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (CatalogEntry) args[0];
			}
			
		});
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
	/*
	@Test
	public void testHandleCatalogEntryCreateInCatalogEntryRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleCatalogEntryCreateRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleGetCatalogsRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleGetCatalogRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleCatalogUpdateRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleCatalogCreateRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleCatalogDestroyRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleGetCatalogByEntityRequest() {
		fail("Not yet implemented");
	}*/

}
