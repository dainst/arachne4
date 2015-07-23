package de.uni_koeln.arachne.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.DataImportService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.XmlConfigUtil;
import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;

@RunWith(MockitoJUnitRunner.class)
public class TestAdminController {

	@Mock
	private UserRightsService userRightsService;
	
	@Mock
	private XmlConfigUtil xmlConfigUtil;
	
	@Mock
	private DataImportService dataImportService;
	
	@InjectMocks
	private AdminController controller;

	private MockMvc mockMvc;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		
		final User testUser = new User();
		testUser.setGroupID(UserRightsService.MIN_ADMIN_ID);
		when(userRightsService.getCurrentUser()).thenReturn(testUser);
		
		final List<String> testDocs = Arrays.asList("testDoc1", "testDoc2", "testDoc3");
		when(xmlConfigUtil.getXMLConfigDocumentList()).thenReturn(testDocs);
		
		final List<String> testIncs = Arrays.asList("testDoc1_inc", "testDoc2_inc", "testDoc3_inc");
		when(xmlConfigUtil.getXMLIncludeElementList()).thenReturn(testIncs);
		
		when(dataImportService.isRunning()).thenReturn(false, true);
		when(dataImportService.getElapsedTime()).thenReturn(10000l);
		when(dataImportService.getCount()).thenReturn(10000l);
		when(dataImportService.getIndexedDocuments()).thenReturn(5000l);
		when(dataImportService.getEstimatedTimeRemaining()).thenReturn(10000l);
		when(dataImportService.getAverageDPS()).thenReturn(100d);
	}

	@Test
	public void testCacheGet() throws Exception {
		mockMvc.perform(
			get("/admin/cache")
				.contentType(APPLICATION_JSON_UTF8))
			.andExpect(status().isOk())
			.andExpect(content().contentType(APPLICATION_JSON_UTF8))
			.andExpect(content().json("{cachedDocuments:[testDoc1, testDoc2, testDoc3],"
					+ "cachedIncludeElements:[testDoc1_inc, testDoc2_inc, testDoc3_inc]}"));
	}
	
	@Test
	public void testCacheDelete() throws Exception {
		mockMvc.perform(
				delete("/admin/cache")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{message:\"Cache\",status:\"cleared\"}"));
	}
	
	@Test
	public void testDataimportGet() throws Exception {
		// dataimport not running
		mockMvc.perform(
				get("/admin/dataimport")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{message:\"Dataimport\", status:\"idle\"}"));
		
		// dataimport in progress
		mockMvc.perform(
				get("/admin/dataimport")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{message:\"Dataimport\", status:\"running\", count:10000,"
						+ "indexedDocuments:5000, documentsPerSecond:100, elapsedTime:\"0:10 minutes\","
						+ "estimatedTimeRemaining:\"166:40 minutes\"}"));
	}

	@Test
	public void testDataimportPostStart() throws Exception {
		// dataimport not running
		mockMvc.perform(
				post("/admin/dataimport").param("command", "start")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{message:\"Dataimport\", status:\"started\"}"));
		
		// dataimport in progress
		mockMvc.perform(
				post("/admin/dataimport").param("command", "start")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{message:\"Dataimport\", status:\"already running\"}"));
	}
	
	@Test
	public void testDataimportPostStop() throws Exception {
		// dataimport not running
		mockMvc.perform(
				post("/admin/dataimport").param("command", "stop")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{message:\"Dataimport\", status:\"not running\"}"));
		
		// dataimport in progress
		mockMvc.perform(
				post("/admin/dataimport").param("command", "stop")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{message:\"Dataimport\", status:\"aborting\"}"));
	}
}
