package de.uni_koeln.arachne.testconfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.UserRightsService;

public class TestUsers {

	public static User getAdmin() {
		final User user = new User();
		user.setUsername("testadmin");
		user.setFirstname("test");
		user.setLastname("admin");
		user.setAll_groups(true);
		user.setGroupID(UserRightsService.MIN_ADMIN_ID);
		return user;
	}
	
	public static User getEditor() {
		final User user = new User();
		user.setUsername("testeditor");
		user.setFirstname("test");
		user.setLastname("editor");
		user.setAll_groups(false);
		Set<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		datasetGroups.add(new DatasetGroup("editorTestGroup"));
		user.setDatasetGroups(datasetGroups);
		user.setGroupID(UserRightsService.MIN_EDITOR_ID);
		return user;
	}
	
	public static User getUser() {
		final User user = new User();
		user.setUsername("testuser");
		user.setFirstname("test");
		user.setLastname("user");
		user.setAll_groups(false);
		Set<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		datasetGroups.add(new DatasetGroup("userTestGroup"));
		user.setDatasetGroups(datasetGroups);
		user.setGroupID(UserRightsService.MIN_USER_ID);
		return user;
	}
	
	public static User getAnonymous() {
		final User user = new User();
		user.setUsername(UserRightsService.ANONYMOUS_USER_NAME);
		user.setAll_groups(false);
		user.setGroupID(0);
		return user;
	}
	
	public static Authentication getAuthentication(final User user) {
		final ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		if (user.getGroupID() >= UserRightsService.MIN_ADMIN_ID) {
			grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), grantedAuthorities);
	}
}
