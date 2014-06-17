package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.uni_koeln.arachne.dao.SessionDao;
import de.uni_koeln.arachne.dao.UserVerwaltungDao;
import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.mapping.Session;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.sqlutil.Condition;
import de.uni_koeln.arachne.sqlutil.SQLToolbox;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * This class allows to query the current users rights. 
 * It looks up the session, the corresponding user and the groups in the database
 * @author Rasmus Krempel
 * @author Sebastian Cuy
 */
@Service("userRightsService")
@Scope(value="request",proxyMode=ScopedProxyMode.INTERFACES)
public class UserRightsService implements IUserRightsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserRightsService.class);
	
	/**
	 * User management DAO instance.
	 */
	@Autowired
	private transient UserVerwaltungDao userVerwaltungDao; 
	
	/**
	 * Session management DAO instance.
	 */
	@Autowired
	private transient SessionDao sessionDao; 
	
	/**
	 * Flag that indicates if the User Data is loaded.
	 */
	private transient boolean isSet = false;

	/**
	 * The Arachne user data set.
	 */
	private transient UserAdministration arachneUser = null;
	
	private transient List<String> exludedTables;
	
	@Autowired
	@Value("#{config.authFreeTables}")
	public void setExcludeTables(final String authFreeTablesCSS) {
		final List<String> authFreeTables = StrUtils.getCommaSeperatedStringAsList(authFreeTablesCSS);
		exludedTables = new ArrayList<String>();
		if (authFreeTables != null) {
			exludedTables.addAll(authFreeTables);
		} else {
			LOGGER.error("Problem configuring authentication free tables. List is: " + authFreeTables);
		}
	}
	
	/**
	 * Method initializing access to the user data. 
	 * If the user data is not fetched yet, it fetches the user name from the session, gets the database row with the user data and formats it 
	 */
	private void initializeUserData() {
		if (!isSet) {
			
			Session session = null;
			HttpServletRequest request = null;
						
			if (RequestContextHolder.getRequestAttributes() != null) {
				request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			}
						
			if (request != null) {
				LOGGER.debug("Session-ID: " + request.getSession().getId());
				session = sessionDao.findById(request.getSession().getId());
			}
			
			if (session == null) {
				arachneUser = userVerwaltungDao.findByName(ANONYMOUS_USER_NAME);
			} else {
				arachneUser = session.getUserAdministration();
			}
			
			isSet = true;		
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.UserRightsService#setUserSolr()
	 */
	@Override
	public void setDataimporter() {
		arachneUser = new UserAdministration();
		arachneUser.setUsername(INDEXING);
		arachneUser.setAll_groups(true);
		this.isSet = true;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.UserRightsService#isUserSolr()
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
	 * @see de.uni_koeln.arachne.service.UserRightsService#getCurrentUser()
	 */
	@Override
	public UserAdministration getCurrentUser() {
		initializeUserData();
		return arachneUser;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.UserRightsService#reset()
	 */
	@Override
	public void reset() {
		arachneUser = null;
		isSet = false;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.arachne.service.UserRightsService#userHasDatasetGroup()
	 */
	@Override
	public boolean userHasDatasetGroup(final DatasetGroup datasetGroup) {
		final Set<DatasetGroup> datasetGroups = this.getCurrentUser().getDatasetGroups();
		final String datasetGroupName = datasetGroup.getName();
		for (final DatasetGroup currentDatasetGroup: datasetGroups) {
			if (currentDatasetGroup.getName().equals(datasetGroupName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the users permissions and converts them in an SQL-Snipplet 
	 * ready to append it to the SQL <code>WHERE</code> statement.
	 * @return A String that represents the user permission SQl statements its empty if the User is allowed to see everything
	 */
	@Override
	public String getSQL(final String tableName) {
		initializeUserData();
		if (INDEXING.equals(arachneUser.getUsername())) {
			return "";
		} else {
			//in This case The User is Authorized to see Everything
			if (arachneUser.isAll_groups() || exludedTables.contains(tableName)) {
				return "";
			} else {
				return buildSQL(tableName);
			}
		}
	}


	private String buildSQL(final String tableName) {
		final StringBuilder sqlBuilder = new StringBuilder(16);
		//Get the Permission Groups
		Set<DatasetGroup> permissionGroups = arachneUser.getDatasetGroups();
		if (!permissionGroups.isEmpty()) {
			//Convert the Permission Groups to real conditions
			final List<Condition> conditions = new ArrayList<Condition>();

			for (final DatasetGroup group : permissionGroups) {
				final Condition condition = new Condition();

				condition.setPart1( SQLToolbox.getQualifiedFieldname(tableName, "DatensatzGruppe"+SQLToolbox.ucfirst(tableName)));
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
					sqlBuilder.append(" OR");
				}
				sqlBuilder.append(cnd.toString());
			}
			sqlBuilder.append(')');
		}
		return sqlBuilder.toString();
	}
}
