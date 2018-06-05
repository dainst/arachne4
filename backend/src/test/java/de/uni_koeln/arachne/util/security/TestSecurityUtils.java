package de.uni_koeln.arachne.util.security;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class TestSecurityUtils {

	private static final String TEST_ROLE_1 = "ROLE_TESTROLE_1";
			
	private static final String TEST_ROLE_2 = "ROLE_TESTROLE_2";
	
	private static final String TEST_ROLE_3 = "ROLE_TESTROLE_3";
	
	private static final Collection<GrantedAuthority> authorities = new HashSet<>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		authorities.add(new SimpleGrantedAuthority(TEST_ROLE_1));
		authorities.add(new SimpleGrantedAuthority(TEST_ROLE_2));
	}

	@Test
	public void testAuthoritiesHaveRole() {
		assertTrue(SecurityUtils.authoritiesHaveRole(authorities, TEST_ROLE_1));
		assertTrue(SecurityUtils.authoritiesHaveRole(authorities, TEST_ROLE_2));
		
		assertFalse(SecurityUtils.authoritiesHaveRole(authorities, null));
		assertFalse(SecurityUtils.authoritiesHaveRole(authorities, TEST_ROLE_3));
	}

}
