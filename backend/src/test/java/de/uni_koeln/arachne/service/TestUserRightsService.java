package de.uni_koeln.arachne.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import de.uni_koeln.arachne.dao.hibernate.UserDao;
import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.UserRightsService.ObjectAccessException;
import de.uni_koeln.arachne.testconfig.ProtectedTestObject;
import de.uni_koeln.arachne.testconfig.TestUsers;

@RunWith(MockitoJUnitRunner.class)
public class TestUserRightsService {

	@Mock
	private UserDao userDao;
	
	@InjectMocks
	private UserRightsService userRightsService = new UserRightsService();
	
	@Before
	public void setUp() throws Exception {
		userRightsService.reset();
		when(userDao.findByName(UserRightsService.ANONYMOUS_USER_NAME)).thenReturn(TestUsers.getAnonymous());
		when(userDao.findByName(TestUsers.getUser().getUsername())).thenReturn(TestUsers.getUser());
		when(userDao.findByName(TestUsers.getEditor().getUsername())).thenReturn(TestUsers.getEditor());
		when(userDao.findByName(TestUsers.getAdmin().getUsername())).thenReturn(TestUsers.getAdmin());
	}
	
	
	@Test
	public void testSet_Is_Dataimporter() {
		userRightsService.setDataimporter();
		assertTrue(userRightsService.isDataimporter());
	}

	@Test
	public void testIsSignedInUserAnonymous() {
		SecurityContextHolder.getContext().setAuthentication(null);
		assertFalse(userRightsService.isSignedInUser());
	}

	@Test
	public void testIsSignedInUserUser() {
		final User user = TestUsers.getUser();
		final Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertTrue(userRightsService.isSignedInUser());
	}
	
	@Test
	public void testUserHasAtLeastGroupIDAnonymous() {
		SecurityContextHolder.getContext().setAuthentication(null);
		assertFalse(userRightsService.userHasAtLeastGroupID(UserRightsService.MIN_ADMIN_ID));
	}
	
	@Test
	public void testUserHasAtLeastGroupIDAdmin() {
		final User user = TestUsers.getAdmin();
		final Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertTrue(userRightsService.userHasAtLeastGroupID(UserRightsService.MIN_ADMIN_ID));
	}

	@Test
	public void testGetCurrentUserAnonymous() {
		final User user =  userRightsService.getCurrentUser();
		SecurityContextHolder.getContext().setAuthentication(null);
		assertEquals(UserRightsService.ANONYMOUS_USER_NAME, user.getUsername());
		assertEquals(null, user.getFirstname());
		assertEquals(null, user.getLastname());
		assertEquals(0, user.getGroupID());
	}

	@Test
	public void testGetCurrentUserAdmin() {
		final User user = TestUsers.getAdmin();
		final Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertEquals("testadmin", user.getUsername());
		assertEquals("test", user.getFirstname());
		assertEquals("admin", user.getLastname());
		assertEquals(UserRightsService.MIN_ADMIN_ID, user.getGroupID());
	}
	
	@Test
	public void testReset() {
		final User user = TestUsers.getUser();
		final Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertTrue(userRightsService.isSignedInUser());
		SecurityContextHolder.getContext().setAuthentication(null);
		userRightsService.reset();
		assertFalse(userRightsService.isSignedInUser());
	}
	
	@Test
	public void testUserHasDatasetGroupAllGroups() {
		final User user = TestUsers.getAdmin();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertTrue(userRightsService.userHasDatasetGroup(new DatasetGroup("editorTestGroup")));
	}
	
	@Test
	public void testUserHasDatasetGroupTrue() {
		final User user = TestUsers.getEditor();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertTrue(userRightsService.userHasDatasetGroup(new DatasetGroup("editorTestGroup")));
	}
	
	@Test
	public void testUserHasDatasetGroupFalse() {
		final User user = TestUsers.getUser();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertFalse(userRightsService.userHasDatasetGroup(new DatasetGroup("editorTestGroup")));
	}
	
	@Test
	public void testGetSQLAllGroups() {
		final User user = TestUsers.getAdmin();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertEquals("", userRightsService.getSQL("testTable"));
	}
	
	@Test
	public void testGetSQLUser() {
		final User user = TestUsers.getUser();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertEquals(" AND (`testTable`.`DatensatzGruppeTestTable` = \"userTestGroup\")"
				, userRightsService.getSQL("testTable"));
	}
	
	@Test(expected=ObjectAccessException.class)
	public void testSetPropertyOnProtectedObjectAnonymous() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		SecurityContextHolder.getContext().setAuthentication(null);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "anonymous changed it", testObject);
	}
	
	@Test(expected=ObjectAccessException.class)
	public void testSetPropertyOnProtectedObjectUser() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUsers.getUser();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "user changed it", testObject);
	}
	
	@Test
	public void testSetPropertyOnProtectedObjectEditor() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUsers.getEditor();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "editor changed it", testObject);
		assertEquals("editor changed it", testObject.getUserStringValue());
	}
	
	@Test
	public void testSetPropertyOnProtectedObjectAdmin() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUsers.getAdmin();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "admin changed it", testObject);
		assertEquals("admin changed it", testObject.getUserStringValue());
	}
	
	@Test(expected=ObjectAccessException.class)
	public void testSetPropertyOnProtectedObjectCustomMinGidAnonymous() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		SecurityContextHolder.getContext().setAuthentication(null);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "anonymous changed it", testObject, UserRightsService.MIN_USER_ID);
	}
	
	@Test
	public void testSetPropertyOnProtectedObjectCustomMinGidUser() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUsers.getUser();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "user changed it", testObject, UserRightsService.MIN_USER_ID);
		assertEquals("user changed it", testObject.getUserStringValue());
	}
	
	@Test
	public void testSetPropertyOnProtectedObjectCustomMinGidEditor() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUsers.getEditor();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "editor changed it", testObject, UserRightsService.MIN_USER_ID);
		assertEquals("editor changed it", testObject.getUserStringValue());
	}
	
	@Test
	public void testSetPropertyOnProtectedObjectCustomMinGidAdmin() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUsers.getAdmin();
		Authentication authToken = TestUsers.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "admin changed it", testObject, UserRightsService.MIN_USER_ID);
		assertEquals("admin changed it", testObject.getUserStringValue());
	}
}
