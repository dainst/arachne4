package de.uni_koeln.arachne.controller;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import de.uni_koeln.arachne.dao.hibernate.ResetPasswordRequestDao;
import de.uni_koeln.arachne.dao.hibernate.UserDao;
import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.ResetPasswordRequest;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.MailService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.security.ProtectedObject;
import de.uni_koeln.arachne.util.security.Random;

@RunWith(MockitoJUnitRunner.class)
public class TestUserManagementController {

	private final static String TOKEN = "verysecuretoken";
	
	@Mock
	private UserRightsService userRightsService;
	
	@Mock
	private UserDao userDao;
	
	@Mock
	private MailService mailService;
	
	@Mock
	private ResetPasswordRequestDao resetPasswordRequestDao;
	
	@Mock
	private Random random;
	
	@InjectMocks
	private UserManagementController controller;
	
	private MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		
		// use reflection to set defaultDatasetGroups
		final List<String> defaultDatasetGroupList = new ArrayList<String>();
		defaultDatasetGroupList.add("testGroup1");
		defaultDatasetGroupList.add("testGroup2");
		Field defaultDatasetGroups = UserManagementController.class.getDeclaredField("defaultDatasetGroups");
		defaultDatasetGroups.setAccessible(true);
		defaultDatasetGroups.set(controller, defaultDatasetGroupList);
		
		when(userRightsService.isSignedInUser()).thenReturn(true, true, false);
				
		final User testUser = new User();
		testUser.setUsername("testuser");
		testUser.setGroupID(UserRightsService.MIN_ADMIN_ID);
		testUser.setFirstname("test");
		testUser.setLastname("user");
		testUser.setZip("12345");
		testUser.setAll_groups(true);
		
		// access pattern admin, user and last anonymous
		when(userRightsService.isSignedInUser()).thenReturn(true, true, false);
		when(userRightsService.getCurrentUser()).thenReturn(testUser, testUser, null);
		when(userRightsService.userHasAtLeastGroupID(UserRightsService.MIN_ADMIN_ID)).thenReturn(true, false);
		
		// simulate write-protected field
		doThrow(new UserRightsService.ObjectAccessException("Field id is write-protected.")).when(userRightsService)
				.setPropertyOnProtectedObject(eq("id"), any(), any(ProtectedObject.class), anyInt());
		
		when(userDao.findByName("testuser")).thenReturn(testUser, testUser, null);
		when(userDao.findByEMailAddress("someaddress")).thenReturn(testUser);
		when(userDao.findById(0)).thenReturn(testUser);
		when(userDao.findDatasetGroupByName("testGroup1")).thenReturn(new DatasetGroup("testGroup1"));
		when(userDao.findDatasetGroupByName("testGroup2")).thenReturn(new DatasetGroup("testGroup2"));
		
		when(mailService.sendMail(anyString(), anyString(), anyString())).thenReturn(true);
		
		when(random.getNewToken()).thenReturn(TOKEN);
		
		final Calendar calender = Calendar.getInstance();
		final long now = calender.getTime().getTime();
		calender.setTimeInMillis(now);
		calender.add(Calendar.HOUR_OF_DAY, 1);
		final Timestamp expirationDate = new Timestamp(calender.getTime().getTime());
		
		final ResetPasswordRequest request = new ResetPasswordRequest();
		request.setToken("verysecuretoken");
		request.setUserId(0L);
		request.setExpirationDate(expirationDate);
		when(resetPasswordRequestDao.getByUserId(anyLong())).thenReturn(null, request);
		when(resetPasswordRequestDao.getByToken(TOKEN)).thenReturn(request);
	}
	
	@Test
	public void testGetUserInfo() throws Exception {
		// admin
		mockMvc.perform(
				get("/userinfo/testuser")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{username:\"testuser\","
						+ "firstname:\"test\","
						+ "lastname:\"user\","
						+ "all_groups:true,"
						+ "}"));
		
		// user
		MvcResult result = mockMvc.perform(
				get("/userinfo/testuser")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{username:\"testuser\","
						+ "firstname:\"test\","
						+ "lastname:\"user\","
						+ "}"))
				.andReturn();
		
		assertFalse(result.getResponse().getContentAsString().contains("all_groups"));
		
		// anonymous
		mockMvc.perform(
				get("/userinfo/testuser")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isForbidden());
	}
		
	@Test
	public void testUpdateUserInfoValid() throws Exception {
		final String json = "{\"firstname\":\"some name\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				put("/userinfo/testuser")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{success:\"true\"}"));
		
		// user
		mockMvc.perform(
				put("/userinfo/testuser")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{success:\"true\"}"));
		
		// anonymous
		mockMvc.perform(
				put("/userinfo/testuser")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void testUpdateUserInfoInvalidBot() throws Exception {
		final String json = "{\"firstname\":\"some name\"}";
		
		// admin
		mockMvc.perform(
				put("/userinfo/testuser")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{success:\"false\",message:\"ui.update.bot\"}"));
	}
	
	@Test
	public void testUpdateUserInfoInvalidWriteProtectedFieldId() throws Exception {
		final String json = "{\"firstname\":\"some name\","
				+ "\"id\":\"7\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		mockMvc.perform(
				put("/userinfo/testuser")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{Exception:\"Field id is write-protected.\"}"));
	}

	@Test
	public void testRegisterValid() throws Exception {
		final String json = "{\"username\":\"newTestuser\","
				+ "\"email\":\"someaddress\","
				+ "\"emailValidation\":\"someaddress\","
				+ "\"password\":\"somepass\","
				+ "\"passwordValidation\":\"somepass\","
				+ "\"firstname\":\"some name\","
				+ "\"lastname\":\"aome other name\","
				+ "\"street\":\"somestreet\","
				+ "\"zip\":\"12345\","
				+ "\"place\":\"some place\","
				+ "\"country\":\"some country\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				post("/user/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{\"success\":\"false\"}"));
		
		// user
		mockMvc.perform(
				post("/user/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{\"success\":\"false\"}"));
		
		// anonymous
		mockMvc.perform(
				post("/user/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"success\":\"true\"}"));
				
	}

	@Test
	public void testRegisterInvalidMissingField() throws Exception {
		final String json = "{\"username\":\"testuser\","
				+ "\"password\":\"somepass\","
				+ "\"lastname\":\"aome other name\","
				+ "\"street\":\"somestreet\","
				+ "\"zip\":\"12345\","
				+ "\"place\":\"some place\","
				+ "\"country\":\"some country\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin dummy request
		mockMvc.perform(
				post("/user/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest());
		
		// user dummy request
		mockMvc.perform(
				post("/user/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest());
		
		// anonymous
		mockMvc.perform(
				post("/user/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{message:\"ui.register.fieldMissing.email\",success:\"false\"}"));
	}
	
	@Test
	public void testRegisterInvalidBot() throws Exception {
		final String json = "{\"username\":\"newTestuser\","
				+ "\"email\":\"someaddress\","
				+ "\"emailValidation\":\"someaddress\","
				+ "\"password\":\"somepass\","
				+ "\"passwordValidation\":\"somepass\","
				+ "\"firstname\":\"some name\","
				+ "\"lastname\":\"aome other name\","
				+ "\"street\":\"somestreet\","
				+ "\"zip\":\"12345\","
				+ "\"place\":\"some place\","
				+ "\"country\":\"some country\"}";
		
		// admin
		mockMvc.perform(
				post("/user/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{\"success\":\"false\"}"));
		
		// user
		mockMvc.perform(
				post("/user/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{\"success\":\"false\"}"));
		
		// anonymous
		mockMvc.perform(
				post("/user/register")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"success\":\"false\",message:\"ui.register.bot\"}"));
				
	}
	
	@Test
	public void testResetValid() throws Exception {
		final String json = "{\"username\":\"testuser\","
				+ "\"email\":\"someaddress\","
				+ "\"firstname\":\"test\","
				+ "\"zip\":\"12345\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		
		// user
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		// anonymous
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isOk())
				.andExpect(content().json("{success:\"true\"}"));
	}

	@Test
	public void testResetInvalidBot() throws Exception {
		final String json = "{\"username\":\"testuser\","
				+ "\"email\":\"someaddress\","
				+ "\"firstname\":\"test\","
				+ "\"zip\":\"12345\"}";
		
		// admin
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		
		// user
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		// anonymous
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\",message:\"ui.passwordreset.bot\"}"));
	}
	
	@Test
	public void testResetInvalidUnknownUser() throws Exception {
		final String json = "{\"username\":\"unknownuser\","
				+ "\"email\":\"someaddress\","
				+ "\"firstname\":\"test\","
				+ "\"zip\":\"12345\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		
		// user
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		// anonymous
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
	}
	
	@Test
	public void testResetInvalidIncorrectEMail() throws Exception {
		final String json = "{\"username\":\"testuser\","
				+ "\"email\":\"someincorrectaddress\","
				+ "\"firstname\":\"test\","
				+ "\"zip\":\"12345\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		
		// user
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		// anonymous
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
	}
	
	@Test
	public void testResetInvalidIncorrectFirstname() throws Exception {
		final String json = "{\"username\":\"testuser\","
				+ "\"email\":\"someaddress\","
				+ "\"firstname\":\"incorrect\","
				+ "\"zip\":\"12345\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		
		// user
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		// anonymous
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
	}
	
	@Test
	public void testResetInvalidIncorrectZip() throws Exception {
		final String json = "{\"username\":\"testuser\","
				+ "\"email\":\"someaddress\","
				+ "\"firstname\":\"test\","
				+ "\"zip\":\"54321\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		
		// user
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		// anonymous
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
	}
	
	@Test
	public void testResetInvalidRequestPending() throws Exception {
		final String json = "{\"username\":\"testuser\","
				+ "\"email\":\"someaddress\","
				+ "\"firstname\":\"test\","
				+ "\"zip\":\"12345\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		
		// user
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
		
		// anonymous
		// no request pending
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isOk());
		// test
		mockMvc.perform(
				post("/user/reset")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{success:\"false\"}"));
	}
	
	
	@Test
	public void testChangePasswordAfterResetRequestValid() throws Exception {
		final String json = "{\"password\":\"newpass\","
				+ "\"passwordConfirm\":\"newpass\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				post("/user/activation/" + TOKEN)
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isNotFound());

		// user
		mockMvc.perform(
				post("/user/activation/" + TOKEN)
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isNotFound());

		// anonymous
		mockMvc.perform(
				post("/user/activation/" + TOKEN)
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isOk());
	}

	@Test
	public void testChangePasswordAfterResetRequestInvalidToken() throws Exception {
		final String json = "{\"password\":\"newpass\","
				+ "\"passwordConfirm\":\"newpass\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				post("/user/activation/invalid")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isNotFound());

		// user
		mockMvc.perform(
				post("/user/activation/invalid")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isNotFound());

		// anonymous
		mockMvc.perform(
				post("/user/activation//invalid")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void testChangePasswordAfterResetRequestInvalidPasswordsDontMatch() throws Exception {
		final String json = "{\"password\":\"newpass\","
				+ "\"passwordConfirm\":\"otherpass\","
				+ "\"iAmHuman\":\"humanIAm\"}";
		
		// admin
		mockMvc.perform(
				post("/user/activation/" + TOKEN)
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isNotFound());

		// user
		mockMvc.perform(
				post("/user/activation/" + TOKEN)
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isNotFound());

		// anonymous
		mockMvc.perform(
				post("/user/activation/" + TOKEN)
				.contentType(APPLICATION_JSON_UTF8)
				.content(json))
				.andExpect(status().isBadRequest());
	}
}
