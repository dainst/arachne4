package de.uni_koeln.arachne.service;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.mapping.UserAdministration;

public interface IUserRightsService {

	public static final String SOLR_INDEXING = "SolrIndexing";
	public static final String ANONYMOUS_USER_NAME = "Anonymous";
	public static final int MIN_ADMIN_ID = 800;
	
	/**
	 * Get the current arachne user
	 * @return UserAdministration the user object or the "anonymous" user if no user is logged in
	 */
	public abstract UserAdministration getCurrentUser();

	/**
	 * Set the Solr user.
	 */
	public abstract void setUserSolr();
	
	/**
	 * Is the current user the Solr user.
	 * @return <code>true</code> if the current user is Solr.
	 */
	public abstract boolean isUserSolr();
	
	/**
	 * Is the current user signed in.
	 * @return <code>true</code> if the current user is signed in.
	 */
	public abstract boolean isSignedInUser();
	
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

}