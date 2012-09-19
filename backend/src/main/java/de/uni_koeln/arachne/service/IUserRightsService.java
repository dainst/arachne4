package de.uni_koeln.arachne.service;

import de.uni_koeln.arachne.mapping.UserAdministration;

public interface IUserRightsService {

	public static final String SOLR_INDEXING = "SolrIndexing";
	public static final int MIN_ADMIN_ID = 800;
	
	/**
	 * Get the current arachne user
	 * @return UserAdministration the user object or null if no user is logged in
	 */
	public abstract UserAdministration getCurrentUser();

	/**
	 * Set the Solr user.
	 */
	public abstract void setUserSolr();
	
	/**
	 * Is the current user the Solr user.
	 * @return <code>true</code>if the current user is Solr.
	 */
	public abstract boolean isUserSolr();
	
	/**
	 * Method to reset the current user (e.g. for logout)
	 */
	public abstract void reset();

}