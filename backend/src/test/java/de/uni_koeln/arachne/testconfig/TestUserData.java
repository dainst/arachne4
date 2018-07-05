package de.uni_koeln.arachne.testconfig;

import static de.uni_koeln.arachne.util.security.SecurityUtils.*;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TestUserData {

	/**
	 * Minimum administrator group id.
	 */
	private static final int MIN_ADMIN_ID = 800;
	/**
	 * Minimum editor group id.
	 */
	private static final int MIN_EDITOR_ID = 600;
	/**
	 * Minimum user group id.
	 */
	private static final int MIN_USER_ID = 400;
	
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
		user.setPassword("testpass");
		user.setLogin_permission(true);
		user.setAll_groups(true);
		user.setGroupID(MIN_ADMIN_ID);
		return user;
	}
	
	public static User getEditor() {
		final User user = new User();
		user.setId(2);
		user.setUsername("testeditor");
		user.setFirstname("test");
		user.setLastname("editor");
		user.setPassword("testpass");
		user.setLogin_permission(true);
		user.setAll_groups(false);
		Set<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		datasetGroups.add(new DatasetGroup("editorTestGroup"));
		user.setDatasetGroups(datasetGroups);
		user.setGroupID(MIN_EDITOR_ID);
		return user;
	}
	
	public static User getUser() {
		final User user = new User();
		user.setId(3);
		user.setUsername("testuser");
		user.setFirstname("test");
		user.setLastname("user");
		user.setPassword("testpass");
		user.setLogin_permission(true);
		user.setAll_groups(false);
		Set<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		datasetGroups.add(new DatasetGroup("userTestGroup"));
		datasetGroups.add(new DatasetGroup("anotherTestGroup"));
		user.setDatasetGroups(datasetGroups);
		user.setGroupID(MIN_USER_ID);
		return user;
	}
	
	public static User getUserDB() {
		final User user = new User();
		user.setId(3);
		user.setUsername("testuser");
		user.setFirstname("test");
		user.setLastname("user");
		user.setPassword("testpass");
		user.setEmail("someaddress@somedomain.com");
		user.setLogin_permission(true);
		user.setAll_groups(false);
		Set<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		user.setDatasetGroups(datasetGroups);
		user.setGroupID(MIN_USER_ID);
		return user;
	}
	
	public static User getUserNoLogin() {
		final User user = new User();
		user.setId(4);
		user.setUsername("testuser_noLogin");
		user.setFirstname("test");
		user.setLastname("user");
		user.setLogin_permission(false);
		user.setAll_groups(false);
		Set<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		datasetGroups.add(new DatasetGroup("userTestGroup"));
		user.setDatasetGroups(datasetGroups);
		user.setGroupID(MIN_USER_ID);
		return user;
	}
	
	public static User getAnonymous() {
		final User user = new User();
		user.setUsername(ANONYMOUS_USER_NAME);
		user.setLogin_permission(true);
		user.setAll_groups(false);
		user.setGroupID(0);
		return user;
	}
	
	public static Authentication getAuthentication(final User user) {
		final ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		grantedAuthorities.add(new SimpleGrantedAuthority(USER));
		if (user.getGroupID() >= MIN_EDITOR_ID) {
			grantedAuthorities.add(new SimpleGrantedAuthority(EDITOR));
		}
		if (user.getGroupID() >= MIN_ADMIN_ID) {
			grantedAuthorities.add(new SimpleGrantedAuthority(ADMIN));
		}
		return new TestingAuthenticationToken(user, user.getPassword(), grantedAuthorities);
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
