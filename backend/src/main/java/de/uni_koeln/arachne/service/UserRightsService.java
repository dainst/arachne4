package de.uni_koeln.arachne.service;

import com.fasterxml.jackson.annotation.JsonView;
import de.uni_koeln.arachne.dao.hibernate.UserDao;
import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.util.security.JSONView;
import de.uni_koeln.arachne.util.security.ProtectedObject;
import de.uni_koeln.arachne.util.security.SecurityUtils;
import de.uni_koeln.arachne.util.security.UserAccess;
import de.uni_koeln.arachne.util.security.UserAccess.Restrictions;
import de.uni_koeln.arachne.util.sql.Condition;
import de.uni_koeln.arachne.util.sql.SQLToolbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static de.uni_koeln.arachne.util.security.SecurityUtils.*;

/**
 * This class allows to query the current users rights. It looks up the session,
 * the corresponding user and the groups in the database.
 * 
 * @author Rasmus Krempel
 * @author Sebastian Cuy
 * @author Reimar Grabowski
 */
@Service("userRightsService")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserRightsService {

	/**
	 * Protected object access exception class.
	 */
	@SuppressWarnings("serial")
	public static class ObjectAccessException extends RuntimeException {
		/**
		 * Constructor to create an exception with a message.
		 * 
		 * @param message
		 *            The message of the exception.
		 */
		public ObjectAccessException(String message) {
			super(message);
		}
	}

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

	private transient List<String> excludedTables = new ArrayList<String>();

	/**
	 * Sets the tables for which no user authentification will be performed. It
	 * takes the names of the table from the 'application.properties' file.
	 * 
	 * @param authFreeTables
	 *            A list of table names.
	 */
	@Autowired
	public void setExcludedTables(@Value("#{'${authFreeTables}'.split(',')}") final List<String> authFreeTables) {
		excludedTables.addAll(authFreeTables);
	}

	/**
	 * Method initializing access to the user data. If the user data is not
	 * fetched yet, it fetches the user name from the session, gets the database
	 * row with the user data and formats it
	 */
	private void initializeUserData() {
		if (!isSet) {
			final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.getPrincipal() instanceof User) {
				arachneUser = (User) authentication.getPrincipal();
			} else {
				arachneUser = userDao.findByName(ANONYMOUS_USER_NAME);
			}
			isSet = true;
		}
	}

	/**
	 * Set the 'dataimport user'.
	 * </br>
	 * The 'dataimport user' cannot be set if there is already an authenticated user.
	 * Since the dataimport is running in its own thread and each SecurityContext is bound 
	 * to exactly one thread, this method cannot be abused to elevate a users privileges. 
	 * 
	 * @return if the 'dataimport user' could be set
	 */
	public boolean setDataimporter() {
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication auth = context.getAuthentication();
		if (auth != null) {
			LOGGER.warn("Could not set user " + INDEXING + " as there is already a user set: " + auth.getName());
			return false;
		}

		context.setAuthentication(getDataimportAuthentication());

		this.isSet = true;
		return true;
	}


	public boolean setDataExporter(User exportingUser) {
		this.arachneUser = exportingUser;
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(getDataExportAuthentication(exportingUser));

		this.isSet = true;
		return true;
	}

	/**
	 * Is the current user the 'dataimport user'.
	 * 
	 * @return <code>true</code> if the current user is the data importer.
	 */
	public boolean isDataimporter() {
		boolean isDataimporter = false;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			isDataimporter = INDEXING.equals(auth.getName());
		}
		return isSet && isDataimporter;
	}

	/**
	 * Is the current user signed in and has login permission.
	 * 
	 * @return <code>true</code> if the current user is signed in.
	 */
	public boolean isSignedInUser() {
		initializeUserData();
		boolean result = isSet && !ANONYMOUS_USER_NAME.equals(arachneUser.getUsername())
				&& arachneUser.isLogin_permission();
		return result;
	}

	/**
	 * If the current user has the specified role.
	 * 
	 * @param role
	 *            The role to check.
	 * @return <code>true</code> if the given role is in the users granted
	 *         authorities collection
	 */
	public boolean userHasRole(final String role) {
		boolean hasRole = false;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			hasRole = SecurityUtils.authoritiesHaveRole(authentication.getAuthorities(), role);
		}
		return hasRole;
	}

	/**
	 * Get the current arachne user.</br>
	 * Should only be used if the actual user data is needed. For permissions
	 * better use {@link #userHasRole(String)}.
	 * 
	 * @return User the user object or the "anonymous" user if no user is logged
	 *         in or the user has no login permission.
	 */
	public User getCurrentUser() {
		initializeUserData();
		if (!arachneUser.isLogin_permission()) {
			arachneUser = userDao.findByName(ANONYMOUS_USER_NAME);
			isSet = true;
		}
		return arachneUser;
	}

	/**
	 * Method to reset the current user.
	 */
	public void reset() {
		arachneUser = null;
		isSet = false;
	}

	/**
	 * Is the given <code>Datasetgroup</code> in the users <code>Set</code> of
	 * <code>DatasetGroups</code>.
	 * 
	 * @param datasetGroup
	 *            A <code>DatasetGroup</code> to check against the user groups.
	 * @return <code>true</code> if the given <code>DatasetGroup</code> is in
	 *         the users <code>Set</code>.
	 */
	public boolean userHasDatasetGroup(final DatasetGroup datasetGroup) {
		initializeUserData();
		final String datasetGroupName = datasetGroup.getName();
		return isSet && arachneUser.hasGroup(datasetGroupName) && arachneUser.isLogin_permission();
	}

	/**
	 * Gets the users permissions and converts them to a SQL statement ready to
	 * be appended to a SQL <code>WHERE</code> statement.
	 * 
	 * @param tableName
	 *            The name of the table that shall be accessed.
	 * @return A <code>String</code> that represents the user permission as SQL
	 *         statement or an empty <code>String</code> if the user is allowed
	 *         to see everything.
	 */
	public String getSQL(final String tableName) {
		initializeUserData();
		final Authentication sca = SecurityContextHolder.getContext().getAuthentication();
		if ((sca != null) && INDEXING.equals(sca.getName())) {
			return "";
		} else {
			// in This case The User is Authorized to see Everything
			if (arachneUser.isAll_groups() || excludedTables.contains(tableName)) {
				return "";
			} else {
				return buildSQL(tableName);
			}
		}
	}

	/**
	 * Method that builds the SQL statement returned by the getSQL() method.
	 * 
	 * @param tableName
	 *            The name of the table that shall be accessed.
	 * @return The SQL statement as <code>String</code>.
	 */
	private String buildSQL(final String tableName) {
		final StringBuilder sqlBuilder = new StringBuilder(16);
		// Get the Permission Groups
		Set<DatasetGroup> permissionGroups = arachneUser.getDatasetGroups();
		if (!permissionGroups.isEmpty()) {
			// Convert the Permission Groups to real conditions
			final List<Condition> conditions = new ArrayList<Condition>();

			for (final DatasetGroup group : permissionGroups) {
				final Condition condition = new Condition();

				condition.setPart1(
						SQLToolbox.getQualifiedFieldname(tableName, "DatensatzGruppe" + SQLToolbox.ucFirst(tableName)));
				condition.setPart2("\"" + group.getName() + "\"");
				condition.setOperator("=");
				conditions.add(condition);
			}

			// Sum up and Build the String
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

	/**
	 * Sets a property on the object associated with this accessor. Properties
	 * are inspected for annotations (namely @JsonView and @UserAccess) and are
	 * only set if the current user has the right to set them. If not an
	 * ObjectAccesException is thrown.
	 * 
	 * @param fieldName
	 *            The name of the property to set.
	 * @param value
	 *            The value to set.
	 * @param object
	 *            The <code>ProtectedObject</code> to set the property on.
	 * @param role
	 *            The 'minimum' role you need to edit a value;
	 * @throws ObjectAccessException
	 *             if the current user is not allowed to access the property.
	 */
	public void setPropertyOnProtectedObject(final String fieldName, final Object value, final ProtectedObject object,
			final String role) throws ObjectAccessException {
		if (isSignedInUser()) {
			try {
				Field field = object.getClass().getDeclaredField(fieldName);
				if (field.isAnnotationPresent(UserAccess.class)) {
					UserAccess userAccess = field.getAnnotation(UserAccess.class);
					Restrictions restrictions = userAccess.value();
					if (restrictions.equals(Restrictions.writeprotected)) {
						throw new ObjectAccessException("Field " + fieldName + " is write-protected.");
					}
				}
				if (field.isAnnotationPresent(JsonView.class)) {
					JsonView jsonView = field.getAnnotation(JsonView.class);
					Class<?> viewClass = jsonView.value()[0];
					boolean acccessGranted = false;
					if (userHasRole(ADMIN)) {
						acccessGranted = viewClass.equals(JSONView.User.class) || 
										 viewClass.equals(JSONView.Admin.class) ||
										 viewClass.equals(JSONView.UnprivilegedUser.class);
					} else {
						if (userHasRole(role)) {
							acccessGranted = viewClass.equals(JSONView.UnprivilegedUser.class) ||
											 viewClass.equals(JSONView.User.class);
						}
					}
					if (acccessGranted) {
						PropertyAccessor userAccessor = PropertyAccessorFactory.forBeanPropertyAccess(object);
						userAccessor.setPropertyValue(fieldName, value);
					} else {
						throw new ObjectAccessException("Access to " + fieldName + " is forbidden.");
					}
				}
			} catch (NoSuchFieldException | SecurityException e) {
				throw new ObjectAccessException("Field " + fieldName + " does not exist.");
			}
		} else {
			throw new ObjectAccessException("Access to " + fieldName + " is forbidden.");
		}
	}

	/**
	 * Sets a property on the object associated with this accessor. Properties
	 * are inspected for annotations (namely @JsonView and @UserAccess) and are
	 * only set if the current user has the right to set them. If not an
	 * ObjectAccesException is thrown.
	 * 
	 * @param fieldName
	 *            The name of the property to set.
	 * @param value
	 *            The value to set.
	 * @param object
	 *            The <code>ProtectedObject</code> to set the property on.
	 * @throws ObjectAccessException
	 *             if the current user is not allowed to access the property.
	 */
	public void setPropertyOnProtectedObject(final String fieldName, final Object value, final ProtectedObject object)
			throws ObjectAccessException {
		setPropertyOnProtectedObject(fieldName, value, object, EDITOR);
	}
}
