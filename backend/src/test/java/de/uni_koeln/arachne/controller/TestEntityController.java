package de.uni_koeln.arachne.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import de.uni_koeln.arachne.service.ESService;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

@RunWith(MockitoJUnitRunner.class)
public class TestEntityController {

	@Mock
	private ESService esService;
	
	@Mock
	private EntityService entityService;
	
	@InjectMocks
	private EntityController controller;
	
	private MockMvc mockMvc;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		
		when(esService.getCount()).thenReturn(666l, -1l);
		
		when(entityService.getEntityFromIndex(1l, null))
			.thenReturn(new TypeWithHTTPStatus<String>("{test:value}", HttpStatus.OK));
		when(entityService.getEntityFromIndex(1l, "test"))
			.thenReturn(new TypeWithHTTPStatus<String>("{test:value}", HttpStatus.OK));
		when(entityService.getEntityFromDB(1l, null))
			.thenReturn(new TypeWithHTTPStatus<String>("{test:value}", HttpStatus.OK));
		when(entityService.getEntityFromDB(1l, "test"))
			.thenReturn(new TypeWithHTTPStatus<String>("{test:value}", HttpStatus.OK));
	}

	@Test
	public void testHandleGetEntityCountRequest() throws Exception {
		// working
		mockMvc.perform(
				get("/entity/count")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{entityCount:666}"));
		
		// esService fails to retrieve the count
		mockMvc.perform(
				get("/entity/count")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is5xxServerError())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{entityCount:-1}"));
	}
	
	@Test
	public void testHandleGetEntityIdRequestIndex() throws Exception {
		mockMvc.perform(
				get("/entity/1")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{test:value}"));
	}
	
	@Test
	public void testHandleGetEntityIdRequestDB() throws Exception {		
		mockMvc.perform(
				get("/entity/1")
					.param("live", "true")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().string("{test:value}"));
		
	}
	
	@Test
	public void testHandleGetCategoryIdRequestIndex() throws Exception {
		mockMvc.perform(
				get("/entity/test/1")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{test:value}"));
	}
	
	@Test
	public void testHandleGetCategoryIdRequestDB() throws Exception {
		mockMvc.perform(
				get("/entity/test/1")
					.param("live", "true")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().string("{test:value}"));
	}
	
	/*
	@Test
	public void testHandleGetDocEntityRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleGetDocCategoryIdRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleGetDataEntityRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleGetDataCategoryIdRequest() {
		fail("Not yet implemented");
	}*/

}
