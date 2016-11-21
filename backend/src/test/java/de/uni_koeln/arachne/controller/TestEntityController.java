package de.uni_koeln.arachne.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

//needed to use .andDo(print()) for debugging
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.mockito.Mockito.*;
import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.response.ImageListResponse;
import de.uni_koeln.arachne.service.ESService;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.EntityService;
import de.uni_koeln.arachne.service.ImageService;
import de.uni_koeln.arachne.service.Transl8Service;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.TypeWithHTTPStatus;

@RunWith(MockitoJUnitRunner.class)
public class TestEntityController {

	@Mock
	private ESService esService;
	
	@Mock
	private EntityService entityService;
	
	@Mock
	private EntityIdentificationService entityIdentificationService;
	
	@Mock
	private ImageService imageService;
	
	@InjectMocks
	private EntityController controller;
	
	private MockMvc mockMvc;
	
	private ObjectMapper jsonMapper = new ObjectMapper();
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
				
		when(esService.getCount()).thenReturn(666l, -1l);
		
		when(entityService.getEntityFromIndex(1l, null)).thenReturn(new TypeWithHTTPStatus<String>("{test:value}"));
		when(entityService.getEntityFromIndex(1l, "test")).thenReturn(new TypeWithHTTPStatus<String>("{test:value}"));
		when(entityService.getEntityFromDB(1l, null)).thenReturn(new TypeWithHTTPStatus<String>("{test:value}"));
		when(entityService.getEntityFromDB(1l, "test")).thenReturn(new TypeWithHTTPStatus<String>("{test:value}"));
		// instance of Transl8Serive to be able to throw its exception 
		Transl8Service dummyTransl8Service = new Transl8Service("test.server.com");
		when(entityService.getEntityFromIndex(2l, null)).thenThrow(dummyTransl8Service.new Transl8Exception("test Exception"));
		when(entityService.getEntityFromIndex(2l, "test")).thenThrow(dummyTransl8Service.new Transl8Exception("test Exception"));
		
		final Dataset testDataset = TestData.getTestDataset();
		
		when(entityIdentificationService.getId(1l)).thenReturn(testDataset.getArachneId());
		
		when(imageService.getImagesSubList(eq(testDataset.getArachneId()), anyInt(), anyInt()))
		.thenAnswer(new Answer<TypeWithHTTPStatus<List<Image>>>() {
			@Override
			public TypeWithHTTPStatus<List<Image>> answer(InvocationOnMock invocation) throws Throwable {
				final int offset = invocation.getArgumentAt(1, Integer.class);
				final int limit = invocation.getArgumentAt(2, Integer.class);
				List<Image> imageList = testDataset.getImages();
				int upperBound = limit + offset;
				upperBound = (upperBound > imageList.size() || limit == 0) ? imageList.size() : upperBound;
				try {
					return new TypeWithHTTPStatus<List<Image>>(imageList.subList(offset, upperBound));
				} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
					return new TypeWithHTTPStatus<>(HttpStatus.BAD_REQUEST);
				}
			}
		});
		when(imageService.getImagesSubList(TestData.getTestDataset().getArachneId(), 0, 0))
			.thenReturn(new TypeWithHTTPStatus<List<Image>>(TestData.getTestDataset().getImages()));
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
	public void testHandleGetEntityIdRequestTransl8Exception() throws Exception {
		mockMvc.perform(
				get("/entity/2")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isInternalServerError());
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
	
	@Test
	public void testHandleGetCategoryIdRequestTransl8Exception() throws Exception {
		mockMvc.perform(
				get("/entity/test/2")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isInternalServerError());
	}
	
	@Test
	public void testHandleImagesRequest() throws Exception {
		final String expectedJson = jsonMapper.writeValueAsString(new ImageListResponse(TestData.getTestDataset().getImages()));
		mockMvc.perform(
				get("/entity/1/images")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(expectedJson));
	}
	
	@Test
	public void testHandleImagesRequestInvalidEntityId() throws Exception {
		mockMvc.perform(
				get("/entity/2/images")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testHandleImagesRequestOffset() throws Exception {
		List<Image> imageList = TestData.getTestDataset().getImages();
		final String expectedJson = jsonMapper.writeValueAsString(new ImageListResponse(imageList.subList(2, 4)));
		mockMvc.perform(
				get("/entity/1/images")
					.param("offset","2")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(expectedJson));
	}
	
	@Test
	public void testHandleImagesRequestOffsetInvalidTooLarge() throws Exception {
		mockMvc.perform(
				get("/entity/1/images")
					.param("offset","5")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8));
	}
	
	@Test
	public void testHandleImagesRequestOffsetInvalidNegativ() throws Exception {
		mockMvc.perform(
				get("/entity/1/images")
					.param("offset","-1")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8));
	}
	
	@Test
	public void testHandleImagesRequestLimit() throws Exception {
		List<Image> imageList = TestData.getTestDataset().getImages();
		final String expectedJson = jsonMapper.writeValueAsString(new ImageListResponse(imageList.subList(0, 2)));
		mockMvc.perform(
				get("/entity/1/images")
					.param("limit","2")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(expectedJson));
	}
	
	@Test
	public void testHandleImagesRequestLimitTooLarge() throws Exception {
		List<Image> imageList = TestData.getTestDataset().getImages();
		final String expectedJson = jsonMapper.writeValueAsString(new ImageListResponse(imageList));
		mockMvc.perform(
				get("/entity/1/images")
					.param("limit","17")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(expectedJson));
	}
	
	@Test
	public void testHandleImagesRequestLimitInvalidNegativ() throws Exception {
		mockMvc.perform(
				get("/entity/1/images")
					.param("limit","-2")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testHandleImagesRequestLimitAndOffset() throws Exception {
		List<Image> imageList = TestData.getTestDataset().getImages();
		final String expectedJson = jsonMapper.writeValueAsString(new ImageListResponse(imageList.subList(1, 3)));
		mockMvc.perform(
				get("/entity/1/images")
					.param("limit","2")
					.param("offset", "1")
					.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json(expectedJson));
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
