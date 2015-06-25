package de.uni_koeln.arachne.controller;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.mockito.Mockito.*;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.common.collect.Multimap;

import de.uni_koeln.arachne.service.SearchService;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.search.SearchParameters;

@RunWith(MockitoJUnitRunner.class)
public class TestSearchController {
	
	@Mock
	private SearchService searchService;
	
	@InjectMocks
	private SearchController controller = new SearchController(50, 20); // TODO get from application.properties
	
	private MockMvc mockMvc;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		
		final TestData testData = new TestData();
		
		when(searchService.buildDefaultSearchRequest(any(SearchParameters.class), any(Multimap.class)))
		.thenReturn(null);
		
		when(searchService.executeSearchRequest(eq((SearchRequestBuilder)null), anyInt(), anyInt(), any(Multimap.class)))
		.thenReturn(testData.getDefaultSearchResult());
	}

	// TODO flesh out the tests
	
	@Test
	public void testSearchDefaultNoParameters() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testSearchLimit() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("limit", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testSearchOffset() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("offset", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testSearchFq() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("fq", "facet_test:test")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testSearchFl() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("fl", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testSearchSort() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("sort", "title")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testSearchDesc() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("desc", "true")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testSearchBbox() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("bbox[]", "0,0,1,1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testSearchGhprec() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("ghprec", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testSearchSortFacet() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("sortfacet", "facet_test")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testSearchLimitMissingQueryString() throws Exception {	
		mockMvc.perform(
				get("/search")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
	}

}
