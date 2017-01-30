package de.uni_koeln.arachne.testconfig;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.UserRightsService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TestUserData {

	private JdbcTemplate jdbcTemplate;
	
	public TestUserData(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public static User getAdmin() {
		final User user = new User();
		user.setId(1);
		user.setUsername("testadmin");
		user.setFirstname("test");
		user.setLastname("admin");
		user.setLogin_permission(true);
		user.setAll_groups(true);
		user.setGroupID(UserRightsService.MIN_ADMIN_ID);
		return user;
	}
	
	public static User getEditor() {
		final User user = new User();
		user.setId(2);
		user.setUsername("testeditor");
		user.setFirstname("test");
		user.setLastname("editor");
		user.setLogin_permission(true);
		user.setAll_groups(false);
		Set<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		datasetGroups.add(new DatasetGroup("editorTestGroup"));
		user.setDatasetGroups(datasetGroups);
		user.setGroupID(UserRightsService.MIN_EDITOR_ID);
		return user;
	}
	
	public static User getUser() {
		final User user = new User();
		user.setId(3);
		user.setUsername("testuser");
		user.setFirstname("test");
		user.setLastname("user");
		user.setLogin_permission(true);
		user.setAll_groups(false);
		Set<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		datasetGroups.add(new DatasetGroup("userTestGroup"));
		datasetGroups.add(new DatasetGroup("anotherTestGroup"));
		user.setDatasetGroups(datasetGroups);
		user.setGroupID(UserRightsService.MIN_USER_ID);
		return user;
	}
	
	public static User getUserNoLogin() {
		final User user = new User();
		user.setId(3);
		user.setUsername("testuser_noLogin");
		user.setFirstname("test");
		user.setLastname("user");
		user.setLogin_permission(false);
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
		user.setLogin_permission(false);
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
	
	public TestUserData createUserTable() throws DataAccessException {
		jdbcTemplate.execute("CREATE TABLE verwaltung_benutzer("
				+ "uid int(10) PRIMARY KEY,"
				+ "gid int(10)," //set('0','500','550','600','700','800','900')
				+ "dgid_alt varchar(255),"
				+ "dgid varchar(255),"
				+ "username varchar(100),"
				+ "password varchar(32),"
				+ "password_confirm varchar(32),"
				+ "institution varchar(64),"
				+ "firstname varchar(64),"
				+ "lastname varchar(64),"
				+ "email varchar(128),"
				+ "emailAuth varchar(24),"
				+ "homepage varchar(128),"
				+ "strasse varchar(64),"
				+ "plz varchar(20),"
				+ "ort varchar(64),"
				+ "land varchar(64),"
				+ "telefon varchar(32),"
				+ "all_groups tinyint(1)," //enum('TRUE','FALSE')
				+ "login_permission tinyint(1)," //enum('TRUE','FALSE')
				+ "info_windows tinyint(1),"
				+ "LastLogin timestamp);");
		return this;
	}
	
	public TestUserData dropUserTable() throws DataAccessException {
		jdbcTemplate.execute("DROP TABLE IF EXISTS verwaltung_benutzer;");
		return this;
	}
	
	public TestUserData setUpUser(final User user) {
		jdbcTemplate.execute("INSERT INTO verwaltung_benutzer("
				+ "uid, gid, username, firstname, lastname, login_permission, all_groups)"
				+ "VALUES"
				+ "("
				+ user.getId() + ","
				+ user.getGroupID() + ","
				+ "'" + user.getUsername() + "',"
				+ "'" + user.getFirstname() + "',"
				+ "'" + user.getLastname() + "',"
				+ "1,0);");
		return this;
	}
}
