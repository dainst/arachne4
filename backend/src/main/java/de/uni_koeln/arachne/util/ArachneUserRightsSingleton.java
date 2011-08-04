package de.uni_koeln.arachne.util;

import java.util.List;

/**
 * This class is A sigleton that keeps track of the Userrights.
 * It has to be Constructed by the UserRights Service!
 * @author Rasmus Krempel
 *
 */
public class ArachneUserRightsSingleton {

	
	private static ArachneUserRightsSingleton instance;
	
	/**
	 * List of the groups the user has permissions to.
	 */
	//private List<String> userGroups;
	private List<String> userGroups;
	/**
	 * Username
	 */
	
	private String username;

	private boolean authorizedForAllGroups;
	
	private boolean confirmed;
	
	private int permissionLevel;
	
	
	private ArachneUserRightsSingleton(String un, boolean authall, boolean conf,int permlvl, List<String> grps) {
		userGroups = grps;
		username = un;
		authorizedForAllGroups = authall;
		confirmed = conf;
		permissionLevel = permlvl;
	}
	/**
	 * Standard Singleton get Instance
	 * @return Returns the Instance of this Singleton
	 */
	public static ArachneUserRightsSingleton getInstance() {

		return instance;
	}
	/**
	 * Init Function that Shouls Only be used by the UserRightsService
	 * @param un
	 * @param authall
	 * @param conf
	 * @param permlvl
	 * @param grps
	 */
	public static void init(String un, boolean authall, boolean conf,int permlvl, List<String> grps){
		instance = new ArachneUserRightsSingleton(un, authall,conf,permlvl,  grps);
	}
	/**
	 * Initializes user data if the user fulfills a specific clearance. 
	 * @param permissionNeeded the minimum permission level needed
	 * @return true or false
	 */
	public boolean isPermissionLevelValid(int permissionNeeded) {
		if (permissionNeeded <= permissionLevel)
			return true;
		else
			return false;
	}
	/**
	 * Looks up all group rights of the user.
	 * @return returns a list of strings with the groups the user is in.
	 */
	public List<String> getUserGroups() {
		return userGroups;
	}
	/**
	 * Fetches the user name.
	 * @return the user name as string. 
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * Looks up if the user is allowed to see all groups.
	 * @return true or false
	 */
	public boolean isAuthorizedForAllGroups() {
		return authorizedForAllGroups;
	}
	/**
	 * Returns if the user is confirmed for login.
	 * @return true or false
	 */
	public boolean isConfirmed() {
		return confirmed;
	}
	
	
	

}
