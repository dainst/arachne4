package de.uni_koeln.arachne.service;

import de.uni_koeln.arachne.mapping.UserAdministration;

public interface UserRightsService {

	/**
	 * Get the current arachne user
	 * @return UserAdministration the user object or null if no user is logged in
	 */
	public abstract UserAdministration getCurrentUser();

	/**
	 * Method to reset the current user (e.g. for logout)
	 */
	public abstract void reset();

}