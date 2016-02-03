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
import de.uni_koeln.arachne.testconfig.TestUserData;

@RunWith(MockitoJUnitRunner.class)
public class TestUserRightsService {

	@Mock
	private UserDao userDao;
	
	@InjectMocks
	private UserRightsService userRightsService = new UserRightsService();
	
	@Before
	public void setUp() throws Exception {
		userRightsService.reset();
		when(userDao.findByName(UserRightsService.ANONYMOUS_USER_NAME)).thenReturn(TestUserData.getAnonymous());
		when(userDao.findByName(TestUserData.getUser().getUsername())).thenReturn(TestUserData.getUser());
		when(userDao.findByName(TestUserData.getEditor().getUsername())).thenReturn(TestUserData.getEditor());
		when(userDao.findByName(TestUserData.getAdmin().getUsername())).thenReturn(TestUserData.getAdmin());
		when(userDao.findByName(TestUserData.getUserNoLogin().getUsername())).thenReturn(TestUserData.getUserNoLogin());
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
		final User user = TestUserData.getUser();
		final Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertTrue(userRightsService.isSignedInUser());
	}
	
	@Test
	public void testIsSignedInUserUserNoLogin() {
		final User user = TestUserData.getUserNoLogin();
		final Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertFalse(userRightsService.isSignedInUser());
	}
	
	@Test
	public void testUserHasAtLeastGroupIDAnonymous() {
		SecurityContextHolder.getContext().setAuthentication(null);
		assertFalse(userRightsService.userHasAtLeastGroupID(UserRightsService.MIN_ADMIN_ID));
	}
	
	@Test
	public void testUserHasAtLeastGroupIDUser() {
		final User user = TestUserData.getUser();
		final Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertFalse(userRightsService.userHasAtLeastGroupID(UserRightsService.MIN_ADMIN_ID));
	}
	
	@Test
	public void testUserHasAtLeastGroupIDAdmin() {
		final User user = TestUserData.getAdmin();
		final Authentication authToken = TestUserData.getAuthentication(user);
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
	public void testGetCurrentUserUserNoLogin() {
		User user = TestUserData.getUserNoLogin();
		final Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		user = userRightsService.getCurrentUser();
		assertEquals(null, user.getFirstname());
		assertEquals(null, user.getLastname());
		assertEquals(0, user.getGroupID());
	}
	
	@Test
	public void testGetCurrentUserAdmin() {
		User user = TestUserData.getAdmin();
		final Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		user = userRightsService.getCurrentUser();
		assertEquals("testadmin", user.getUsername());
		assertEquals("test", user.getFirstname());
		assertEquals("admin", user.getLastname());
		assertEquals(UserRightsService.MIN_ADMIN_ID, user.getGroupID());
	}
	
	@Test
	public void testReset() {
		final User user = TestUserData.getUser();
		final Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertTrue(userRightsService.isSignedInUser());
		SecurityContextHolder.getContext().setAuthentication(null);
		userRightsService.reset();
		assertFalse(userRightsService.isSignedInUser());
	}
	
	@Test
	public void testUserHasDatasetGroupAllGroups() {
		final User user = TestUserData.getAdmin();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertTrue(userRightsService.userHasDatasetGroup(new DatasetGroup("editorTestGroup")));
	}
	
	@Test
	public void testUserHasDatasetGroupTrue() {
		final User user = TestUserData.getEditor();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertTrue(userRightsService.userHasDatasetGroup(new DatasetGroup("editorTestGroup")));
	}
	
	@Test
	public void testUserHasDatasetGroupFalse() {
		final User user = TestUserData.getUser();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertFalse(userRightsService.userHasDatasetGroup(new DatasetGroup("editorTestGroup")));
	}
	
	@Test
	public void testUserHasDatasetGroupFalseNoLogin() {
		final User user = TestUserData.getUserNoLogin();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertFalse(userRightsService.userHasDatasetGroup(new DatasetGroup("userTestGroup")));
	}
	
	@Test
	public void testGetSQLAllGroups() {
		final User user = TestUserData.getAdmin();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		assertEquals("", userRightsService.getSQL("testTable"));
	}
	
	@Test
	public void testGetSQLUser() {
		final User user = TestUserData.getUser();
		Authentication authToken = TestUserData.getAuthentication(user);
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
		final User user = TestUserData.getUser();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "user changed it", testObject);
	}
	
	@Test
	public void testSetPropertyOnProtectedObjectEditor() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUserData.getEditor();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "editor changed it", testObject);
		assertEquals("editor changed it", testObject.getUserStringValue());
	}
	
	@Test
	public void testSetPropertyOnProtectedObjectAdmin() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUserData.getAdmin();
		Authentication authToken = TestUserData.getAuthentication(user);
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
		final User user = TestUserData.getUser();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "user changed it", testObject, UserRightsService.MIN_USER_ID);
		assertEquals("user changed it", testObject.getUserStringValue());
	}
	
	@Test
	public void testSetPropertyOnProtectedObjectCustomMinGidEditor() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUserData.getEditor();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "editor changed it", testObject, UserRightsService.MIN_USER_ID);
		assertEquals("editor changed it", testObject.getUserStringValue());
	}
	
	@Test
	public void testSetPropertyOnProtectedObjectCustomMinGidAdmin() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUserData.getAdmin();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("userStringValue", "admin changed it", testObject, UserRightsService.MIN_USER_ID);
		assertEquals("admin changed it", testObject.getUserStringValue());
	}
	
	@Test(expected=ObjectAccessException.class)
	public void testSetPropertyOnProtectedObjectAdminFieldUser() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUserData.getUser();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("adminStringValue", "user changed it", testObject);
	}
	
	@Test
	public void testSetPropertyOnProtectedObjectAdminFieldAdmin() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUserData.getAdmin();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("adminStringValue", "admin changed it", testObject);
		assertEquals("admin changed it", testObject.getAdminStringValue());
	}
	
	@Test(expected=ObjectAccessException.class)
	public void testSetPropertyOnProtectedObjectWriteProtectedAdmin() {
		ProtectedTestObject testObject = new ProtectedTestObject();
		final User user = TestUserData.getAdmin();
		Authentication authToken = TestUserData.getAuthentication(user);
		SecurityContextHolder.getContext().setAuthentication(authToken);
		userRightsService.setPropertyOnProtectedObject("writeProtectedStringValue", "admin changed it", testObject);
	}
}
