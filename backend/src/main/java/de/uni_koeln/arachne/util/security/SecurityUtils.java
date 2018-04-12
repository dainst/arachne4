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
	 * Administrator role.
	 */
	public static final String ADMIN = "ROLE_ADMIN";
	/**
	 * Editor role.
	 */
	public static final String EDITOR = "ROLE_EDITOR";
	/**
	 * User role.
	 */
	public static final String USER = "ROLE_USER";
	/**
	 * Indexing operation (meaning data import).
	 */
	public static final String INDEXING = "Indexing";
	/**
	 * Username used for anonymous users.
	 */
	public static final String ANONYMOUS_USER_NAME = "Anonymous";
	/**
	 * Dataset group role prefix.
	 */
	public static final String GROUP_PREFIX = "ARACHNE_";
	/**
	 * All dataset groups authority.
	 */
	public static final String ALL_GROUPS = GROUP_PREFIX + "ALL_GROUPS";
	
	
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
