package de.uni_koeln.arachne.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import static de.uni_koeln.arachne.util.security.SecurityUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
		User expectedUser = TestUserData.getAdmin();
		HashSet<GrantedAuthority> authorities = new HashSet<>(
				Arrays.asList(new SimpleGrantedAuthority(USER), new SimpleGrantedAuthority(EDITOR),
						new SimpleGrantedAuthority(ADMIN), new SimpleGrantedAuthority(ALL_GROUPS)));
		expectedUser.setAuthorities(authorities);

		testUser(expectedUser);
	}

	@Test
	public void testLoadUserByUsername_Editor() {
		User expectedUser = TestUserData.getEditor();
		HashSet<GrantedAuthority> authorities = new HashSet<>(Arrays.asList(new SimpleGrantedAuthority(USER),
				new SimpleGrantedAuthority(EDITOR), new SimpleGrantedAuthority(
						GROUP_PREFIX + expectedUser.getDatasetGroups().iterator().next().getName())));
		expectedUser.setAuthorities(authorities);

		testUser(expectedUser);
	}

	@Test
	public void testLoadUserByUsername_User() {
		User expectedUser = TestUserData.getUser();
		ArrayList<String> datasetGroups = new ArrayList<>(2);
		expectedUser.getDatasetGroups().forEach(g -> datasetGroups.add(GROUP_PREFIX + g.getName()));
		
		HashSet<GrantedAuthority> authorities = new HashSet<>(Arrays.asList(new SimpleGrantedAuthority(USER),
				new SimpleGrantedAuthority(datasetGroups.get(0)),
				new SimpleGrantedAuthority(datasetGroups.get(1))));
		expectedUser.setAuthorities(authorities);

		testUser(expectedUser);
	}

	@Test(expected = UsernameNotFoundException.class)
	public void testLoadUserByUsername_NoUser() {
		userDetailsService.loadUserByUsername("NoUser");
	}

	private void testUser(User expectedUser) {
		User user = new User();
		BeanUtils.copyProperties(expectedUser, user);
		
		when(userDao.findByName(expectedUser.getUsername())).thenReturn(user);

		User actualUser = (User) userDetailsService.loadUserByUsername(user.getUsername());

		assertNotNull(actualUser);
		assertEquals(expectedUser, actualUser);
		assertTrue(SecurityUtils.authoritiesHaveRole(actualUser.getAuthorities(),
				"ROLE_" + actualUser.getLastname().toUpperCase(Locale.ROOT)));
	}

}
