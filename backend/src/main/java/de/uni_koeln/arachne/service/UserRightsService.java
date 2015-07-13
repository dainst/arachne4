package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.hibernate.UserDao;
import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.util.sql.Condition;
import de.uni_koeln.arachne.util.sql.SQLToolbox;

/**
 * This class allows to query the current users rights. 
 * It looks up the session, the corresponding user and the groups in the database.
 * 
 * @author Rasmus Krempel
 * @author Sebastian Cuy
 * @author Reimar Grabowski
 */
@Service("userRightsService")
@Scope(value="request",proxyMode=ScopedProxyMode.INTERFACES)
public class UserRightsService implements IUserRightsService {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(UserRightsService.class);
	
	/**
	 * User management DAO instance.
	 */
	@Autowired
	private transient UserDao userDao; 
	
	/**
	 * Flag that indicates if the User Data is loaded.
	 */
	private transient boolean isSet = false;

	/**
	 * The Arachne user data set.
	 */
	private transient User arachneUser = null;
	
	private transient List<String> excludedTables;
	
	@Autowired
	public void setExcludedTables(@Value("#{'${authFreeTables}'.split(',')}") final List<String> authFreeTables) {
		excludedTables = authFreeTables;
	}
	
	/**
	 * Method initializing access to the user data. 
	 * If the user data is not fetched yet, it fetches the user name from the session, gets the database row with the user data and formats it 
	 */
	private void initializeUserData() {
		if (!isSet) {
			final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null) {
				final String username = authentication.getName();
				
				// TODO - change anonymous username in db to make this 'if free'
				if ("anonymousUser".equals(username)) {
					arachneUser = userDao.findByName(ANONYMOUS_USER_NAME);
				} else {
					arachneUser = userDao.findByName(username);
				}
			} else {
				arachneUser = userDao.findByName(ANONYMOUS_USER_NAME);
			}
			isSet = true;		
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.IUserRightsService#setDataimporter()
	 */
	@Override
	public void setDataimporter() {
		arachneUser = new User();
		arachneUser.setUsername(INDEXING);
		arachneUser.setAll_groups(true);
		this.isSet = true;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.IUserRightsService#isDataimporter()
	 */
	@Override
	public boolean isDataimporter() {
		return isSet && INDEXING.equals(arachneUser.getUsername());
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.UserRightsService#isSignedInUser()
	 */
	@Override
	public boolean isSignedInUser() {
		return isSet && !(ANONYMOUS_USER_NAME.equals(arachneUser.getUsername()));
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.UserRightsService#isSignedInUser()
	 */
	@Override
	public boolean userHasAtLeastGroupID(int groupId) {
		initializeUserData();
		return groupId <= arachneUser.getGroupID();
	};
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.IUserRightsService#getCurrentUser()
	 */
	@Override
	public User getCurrentUser() {
		initializeUserData();
		return arachneUser;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.IUserRightsService#reset()
	 */
	@Override
	public void reset() {
		arachneUser = null;
		isSet = false;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.IUserRightsService#userHasDatasetGroup()
	 */
	@Override
	public boolean userHasDatasetGroup(final DatasetGroup datasetGroup) {
		final User user = getCurrentUser();
		final String datasetGroupName = datasetGroup.getName();
		return user.hasGroup(datasetGroupName);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.IUserRightsService#getSQL()
	 */
	@Override
	public String getSQL(final String tableName) {
		initializeUserData();
		if (INDEXING.equals(arachneUser.getUsername())) {
			return "";
		} else {
			//in This case The User is Authorized to see Everything
			if (arachneUser.isAll_groups() || excludedTables.contains(tableName)) {
				return "";
			} else {
				return buildSQL(tableName);
			}
		}
	}

	/**
	 * Method that builds the SQL statement returned by the getSQL() method.
	 * @param tableName The name of the table that shall be accessed.
	 * @return The SQL statement as <code>String</code>.
	 */
	private String buildSQL(final String tableName) {
		final StringBuilder sqlBuilder = new StringBuilder(16);
		//Get the Permission Groups
		Set<DatasetGroup> permissionGroups = arachneUser.getDatasetGroups();
		if (!permissionGroups.isEmpty()) {
			//Convert the Permission Groups to real conditions
			final List<Condition> conditions = new ArrayList<Condition>();

			for (final DatasetGroup group : permissionGroups) {
				final Condition condition = new Condition();

				condition.setPart1( SQLToolbox.getQualifiedFieldname(tableName, "DatensatzGruppe"+SQLToolbox.ucFirst(tableName)));
				condition.setPart2("\""+ group.getName() +"\"");
				condition.setOperator("=");
				conditions.add( condition);
			}

			//Sum up and Build the String
			sqlBuilder.append(" AND (");
			boolean first = true;
			for (final Condition cnd : conditions) {
				if (first) {
					first = false;
				} else {
					sqlBuilder.append(" OR ");
				}
				sqlBuilder.append(cnd.toString());
			}
			sqlBuilder.append(')');
		}
		return sqlBuilder.toString();
	}
}
