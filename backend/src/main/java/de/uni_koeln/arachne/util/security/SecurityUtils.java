package de.uni_koeln.arachne.util.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;

/**
 * Security related utility functions.
 * 
 * @author Reimar Grabowski
 *
 */
public class SecurityUtils {

	/**
	 * Returns if the given role is included in the authorities collection.
	 * 
	 * @param authorities the authorities to inspect
	 * @param role the role to find
	 * @return {@code true} if the given role is included in the authorities
	 */
	public static boolean authoritiesHaveRole(Collection<? extends GrantedAuthority> authorities, String role) {
		boolean hasRole = false;
		if (StringUtils.hasText(role)) {
			hasRole = authorities.stream().anyMatch(a -> role.equals(a.getAuthority()));
		}
		return hasRole;
	}
}
