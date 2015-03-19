package de.uni_koeln.arachne.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import de.uni_koeln.arachne.dao.hibernate.UserDao;
import de.uni_koeln.arachne.mapping.User;

/**
 * Sping UserDetailsService implementation to retrieve user information from the DB.
 * @author Reimar Grabowski
 */
public class ArachneUserDetailsService implements UserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArachneUserDetailsService.class);
	
	public static final int MIN_ADMIN_ID = 800;
	
	@Autowired
	private transient UserDao userDao;

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		
		LOGGER.debug("Username: " + username);
		final User user = userDao.findByName(username);
		if (user == null) {
			throw new UsernameNotFoundException("Username not found.");
		}
		
		final ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		if (user.getGroupID() >= MIN_ADMIN_ID) {
			grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		
		UserDetails userDetails = new org.springframework.security.core.userdetails.User(username, user.getPassword() 
				, grantedAuthorities);
		
		return userDetails;
	}

}
