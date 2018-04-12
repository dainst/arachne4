package de.uni_koeln.arachne.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import de.uni_koeln.arachne.dao.hibernate.UserDao;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.testconfig.TestUserData;
import de.uni_koeln.arachne.util.security.SecurityUtils;

@RunWith(MockitoJUnitRunner.class)
public class TestArachneUserDetailsService {

	@Mock
	UserDao userDao;

	@InjectMocks
	ArachneUserDetailsService userDetailsService = new ArachneUserDetailsService();

	@Test
	public void testLoadUserByUsername_Admin() {
		testUser(TestUserData.getAdmin());
	}

	@Test
	public void testLoadUserByUsername_Editor() {
		testUser(TestUserData.getEditor());
	}

	@Test
	public void testLoadUserByUsername_User() {
		testUser(TestUserData.getUser());
	}

	@Test(expected = UsernameNotFoundException.class)
	public void testLoadUserByUsername_NoUser() {
		userDetailsService.loadUserByUsername("NoUser");
	}

	private void testUser(User expectedUser) {
		expectedUser.setAuthorities(null);
		when(userDao.findByName(expectedUser.getUsername())).thenReturn(expectedUser);

		User actualUser = (User) userDetailsService.loadUserByUsername(expectedUser.getUsername());
		
		assertNotNull(actualUser);
		assertEquals(expectedUser, actualUser);
		assertTrue(SecurityUtils.authoritiesHaveRole(actualUser.getAuthorities(),
				"ROLE_" + actualUser.getLastname().toUpperCase(Locale.ROOT)));
	}

}
