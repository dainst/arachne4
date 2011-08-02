package de.uni_koeln.arachne.service;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.UserVerwaltungDao;
import de.uni_koeln.arachne.mapping.UserVerwaltung;

@Service("UserRightsService")
public class UserRightsService {
	
	/**
	 * User management DAO instance.
	 */
	@Autowired
	private UserVerwaltungDao userVerwaltungDao;

	/**
	 * Flag that indicates if the User Data is loaded.
	 */
	private boolean isSet = false;

	/**
	 * The Arachne user data set.
	 */
	private UserVerwaltung arachneUser = null;

	/**
	 * List of the groups the user has permissions to.
	 */
	//private List<String> userGroups;
	private String userGroups;

	/**
	 * Map holding informations about the user.
	 */
	private Map<String,String> userInfo;

	/**
	 * Private function to retrive the Session.
	 * @return Session
	 */
	/*private HttpSession getSession() {
			return RequestContextHolder.currentRequestAttributes().getSession();
	}*/

	/**
	 * Method initializing access to the user data.
	 * If the user data is not fetched yet, it fetches the user name from the CAS-Ticket, 
	 * gets the database row with the user data and formats it.
	 * Else it does nothing.
	 */
	private void initializeUserData() {
		if (!isSet) {
			//get username of validated CAS ticket
			//String username = getSession().getAt(CASFilter.CAS_FILTER_USER);

			//String username = "rKrempel";
			//get User Information from the Database

			arachneUser =   userVerwaltungDao.findById( new Long( 695));

			//Splitting up all user permissions from the User dataset
			userGroups = arachneUser.getRightGroups();
			//Setting the user person information 
			//TODO maybe better as object
			userInfo = null;
			/*= [
					 "username":arachneUser.getUsername() ,
					 "institution":arachneUser.getInstitution() ,
					 "firstname":arachneUser.getFirstname(),
					 "lastname":arachneUser.getLastname(),
					 "email":arachneUser.getEmail(),
					 "zip":arachneUser.getZip(),
					 "place":arachneUser.getPlace(),
					 "homepage":arachneUser.getHomepage(),
					 "country":arachneUser.getCountry(),
					 "telephone":arachneUser.getTelephone(),
					 	  ]
			 */
			isSet = true;
		}
	}		

	/**
	 * Fetches the user name.
	 * @return the user name as string. 
	 */
	public String getUsername(){
		initializeUserData();
		return arachneUser.getUsername();
	}
	
	/**
	 * Looks up all group rights of the user.
	 * @return returns a list of strings with the groups the user is in.
	 */
	public String getDataGroups(){
		initializeUserData();
		return userGroups;
	}

	/**
	 * Looks up if the user is allowed to see all groups.
	 * @return true or false
	 */
	public boolean isAuthorizedForAllGroups(){
		initializeUserData();
		return arachneUser.isAll_groups();
	}
	
	/**
	 * Returns if the user is confirmed for login.
	 * @return true or false
	 */
	public boolean isConfirmed(){
		initializeUserData();
		return arachneUser.isLogin_permission();
	}
	
	/**
	 * Initializes user data if the user fulfills a specific clearance. 
	 * @param permissionNeeded the minimum permission level needed
	 * @return true or false
	 */
	public boolean isPermissionLevelValid(int permissionNeeded) {
		initializeUserData();
		if (permissionNeeded <= arachneUser.getGroupID())
			return true;
		else
			return false;
	}
	
	/**
	 * This function collects all user data.
	 * @return key/value-map with all the attributes of the user info
	 */
	public Map getUserData() {
		initializeUserData();
		return userInfo;
	}
	
	/**
	 * Returns the time of the last login.
	 * @return timestamp as string
	 */
	public Date getLastLogin() {
		initializeUserData();
		return arachneUser.getLastLogin();
	}
}