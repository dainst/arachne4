package de.uni_koeln.arachne.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;
import de.uni_koeln.arachne.service.MailService;

@RunWith(MockitoJUnitRunner.class)
public class TestContactController {

	@Mock
	private MailService mailService;
	
	@InjectMocks
	private ContactController controller;
	
	private MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		when(mailService.sendMail(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
	}
	
	@Test
	public void testValidData() throws Exception {
		final String json = "{\"name\":\"some name\","
				+ "\"email\":\"some mail address\","
				+ "\"subject\":\"some subject\","
				+ "\"message\":\"some message\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		mockMvc.perform(
				post("/contact")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isOk())
				.andExpect(content().string(""));
	}
	
	@Test
	public void testValidDataAsBot() throws Exception {
		final String json = "{\"name\":\"some name\","
				+ "\"email\":\"some mail address\","
				+ "\"subject\":\"some subject\","
				+ "\"message\":\"some message\"}";
		
		mockMvc.perform(
				post("/contact")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().is4xxClientError())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"success\":\"false\",\"message\":\"ui.contact.bot\"}"));
	}
	
	@Test
	public void testInvalidDataMissingName() throws Exception {
		mockMvc.perform(
				post("/contact")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"some\":\"random garbage\",\"iAmHuman\":\"humanIAm\"}"))
				.andExpect(status().is4xxClientError())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"success\":\"false\",\"message\":\"ui.contact.fieldMissing.name\"}"));
	}
	
	@Test
	public void testInvalidDataMissingEMail() throws Exception {
		final String json = "{\"name\":\"some name\","
				+ "\"some\":\"random garbage\","
				+ "\"subject\":\"some subject\","
				+ "\"message\":\"some message\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		mockMvc.perform(
				post("/contact")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().is4xxClientError())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"success\":\"false\",\"message\":\"ui.contact.fieldMissing.email\"}"));
	}
	
	@Test
	public void testInvalidDataMissingSubject() throws Exception {
		final String json = "{\"name\":\"some name\","
				+ "\"email\":\"some mail address\","
				+ "\"some\":\"random garbage\","
				+ "\"message\":\"some message\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		mockMvc.perform(
				post("/contact")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().is4xxClientError())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"success\":\"false\",\"message\":\"ui.contact.fieldMissing.subject\"}"));
	}
	
	@Test
	public void testInvalidDataMissingMessage() throws Exception {
		final String json = "{\"name\":\"some name\","
				+ "\"email\":\"some mail address\","
				+ "\"subject\":\"some subject\","
				+ "\"some\":\"random garbage\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		mockMvc.perform(
				post("/contact")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().is4xxClientError())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"success\":\"false\",\"message\":\"ui.contact.fieldMissing.message\"}"));
	}

}