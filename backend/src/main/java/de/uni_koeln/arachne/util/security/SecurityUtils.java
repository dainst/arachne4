package de.uni_koeln.arachne.util.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import de.uni_koeln.arachne.mapping.hibernate.User;

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
	
	/**
	 * Returns the 'dataimport user' {@link Authentication}.</br>
	 * The granted roles are: ADMIN, EDITOR, USER and ALL_GROUPS
	 * 
	 * @return the authentication instance
	 */
	public static Authentication getDataimportAuthentication() {
		final ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		grantedAuthorities.add(new SimpleGrantedAuthority(ADMIN));
		grantedAuthorities.add(new SimpleGrantedAuthority(EDITOR));
		grantedAuthorities.add(new SimpleGrantedAuthority(USER));
		grantedAuthorities.add(new SimpleGrantedAuthority(ALL_GROUPS));

		User user = new User();
		user.setUsername(INDEXING);
		user.setLogin_permission(true);
		user.setAll_groups(true);
		user.setAuthorities(grantedAuthorities);

		return new UsernamePasswordAuthenticationToken(user, user.getPassword(), grantedAuthorities);
	}
}
