package de.uni_koeln.arachne.service;

import de.uni_koeln.arachne.mapping.UserAdministration;

public interface UserRightsService {

	public static final String SOLR_INDEXING = "SolrIndexing";
	
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
	 * Method to reset the current user (e.g. for logout)
	 */
	public abstract void reset();

}