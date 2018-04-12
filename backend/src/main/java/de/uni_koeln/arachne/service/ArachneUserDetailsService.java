package de.uni_koeln.arachne.service;

import static de.uni_koeln.arachne.util.security.SecurityUtils.*;

import java.util.HashSet;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.hibernate.UserDao;
import de.uni_koeln.arachne.mapping.hibernate.User;

/**
 * Spring UserDetailsService implementation to retrieve user information from
 * the DB. </br>
 * The users dataset groups are converted to roles for easier/unified permission
 * checks.
 * 
 * @author Reimar Grabowski
 */
@Service
public class ArachneUserDetailsService implements UserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArachneUserDetailsService.class);

	/**
	 * Minimum administrator group id.
	 */
	private static final int MIN_ADMIN_ID = 800;
	/**
	 * Minimum editor group id.
	 */
	private static final int MIN_EDITOR_ID = 600;

	@Autowired
	private transient UserDao userDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		LOGGER.debug("Username: " + username);
		final User user = userDao.findByName(username);
		if (user == null) {
			throw new UsernameNotFoundException("Username not found.");
		}

		final HashSet<GrantedAuthority> grantedAuthorities = new HashSet<>();

		// roles based on groupID
		grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		if (user.getGroupID() >= MIN_ADMIN_ID) {
			grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		if (user.getGroupID() >= MIN_EDITOR_ID) {
			grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_EDITOR"));
		}

		// roles based on dataset groups
		if (user.isAll_groups()) {
			grantedAuthorities.add(new SimpleGrantedAuthority(ALL_GROUPS));
		}

		user.getDatasetGroups().stream().filter(Objects::nonNull).forEach(g -> grantedAuthorities
				.add(new SimpleGrantedAuthority(GROUP_PREFIX + g.getName())));

		user.setAuthorities(grantedAuthorities);
		
		return user;
	}

}
