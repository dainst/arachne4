package de.uni_koeln.arachne.service;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.User;

public interface IUserRightsService {

	public static final String INDEXING = "Indexing";
	public static final String ANONYMOUS_USER_NAME = "Anonymous";
	public static final int MIN_ADMIN_ID = 800;
	public static final int MIN_EDITOR_ID = 600;
	
	/**
	 * Get the current arachne user
	 * @return User the user object or the "anonymous" user if no user is logged in
	 */
	public abstract User getCurrentUser();

	/**
	 * Set the 'dataimport user'.
	 */
	public abstract void setDataimporter();
	
	/**
	 * Is the current user the 'dataimport user'.
	 * @return <code>true</code> if the current user is Solr.
	 */
	public abstract boolean isDataimporter();
	
	/**
	 * Is the current user signed in.
	 * @return <code>true</code> if the current user is signed in.
	 */
	public abstract boolean isSignedInUser();
	
	/**
	 * If the current user has at least the given groupId.
	 * @param groupId A groupId to check against the users groupId.
	 * @return <code>true</code> if the given groupId is equal or less than the users groupId.
	 */
	public boolean userHasAtLeastGroupID(final int groupId);
	
	/**
	 * Is the given <code>Datasetgroup</code> in the users <code>Set</code> of <code>DatasetGroups</code>.
	 * @param datasetGroup A <code>DatasetGroup</code> to check against the user groups.
	 * @return <code>true</code> if the given <code>DatasetGroup</code> is in the users <code>Set</code>.
	 */
	public boolean userHasDatasetGroup(final DatasetGroup datasetGroup);
	
	/**
	 * Method to reset the current user (e.g. for logout)
	 */
	public abstract void reset();

	/**
	 * Gets the users permissions and converts them to a SQL statement ready to be appended to a SQL <code>WHERE</code> 
	 * statement.
	 * @param tableName The name of the table that shall be accessed.
	 * @return A <code>String</code> that represents the user permission as SQL statement or an empty 
	 * <code>String</code> if the user is allowed to see everything.
	 */
	public abstract String getSQL(final String tableName);
}