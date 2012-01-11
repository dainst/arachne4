package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.uni_koeln.arachne.dao.UserVerwaltungDao;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.util.UserRightsSingleton;

/**
 * This class allows to query the current users rights. 
 * It extracts the user name from the CAS ticket and looks up his rights in the database via hibernate.
 * @author Rasmus Krempel
 */
@Service("userRightsService")
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
	private UserAdministration arachneUser = null;

	/**
	 * List of the groups the user has permissions to.
	 */
	//private List<String> userGroups;
	private List<String> userGroups;

	/**
	 * Map holding informations about the user.
	 */
	private Map<String,String> userInfo;

	/**
	 * Private function to retrive the Session.
	 * @return Session
	 */

	/**
	 * Method initializing access to the user data. 
	 * If the user data is not fetched yet, it fetches the user name from the CAS-Ticket,  gets the database row with the user data and formats it. It also constructs the <code>ArachneUserRightsSingleton</code> which is Used by the sqlutils for example. 
	 */
	public void initializeUserData() {
		if (!isSet) {
			
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
	                .getRequestAttributes()).getRequest();

			arachneUser = userVerwaltungDao.findByName(request.getRemoteUser());

			//Splitting up all user permissions from the User dataset
			userGroups= new ArrayList<String>();
			
			if (arachneUser != null) {
			String[] temp = (arachneUser.getRightGroups().split(","));
				for (int i =0; i<temp.length; i++) {
					userGroups.add(temp[i]);
				}
			}
			
			//Setting the user person information 
			/* TODO implement following as map
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
			*/
			 
			userInfo = null;
			int userGroupID = -1;
			if (arachneUser != null) {
				userGroupID = arachneUser.getGroupID();
			}
			isSet = true;
			UserRightsSingleton.init(this.getUsername(), this.isAuthorizedForAllGroups(), this.isConfirmed(), userGroupID, this.getUserGroups());
			
		}
	}		

	/**
	 * Fetches the user name.
	 * @return the user name as string. 
	 */
	public String getUsername(){
		initializeUserData();
		if (arachneUser != null) {
			return arachneUser.getUsername();
		} else {
			return "anonymous";
		}
	}
	
	/**
	 * Looks up all group rights of the user.
	 * @return returns a list of strings with the groups the user is in.
	 */
	public List<String> getUserGroups(){
		initializeUserData();
		return userGroups;
	}

	/**
	 * Looks up if the user is allowed to see all groups.
	 * @return true or false
	 */
	public boolean isAuthorizedForAllGroups(){
		initializeUserData();
		if (arachneUser != null) {
			return arachneUser.isAll_groups();
		} else {
			return false;
		}
	}
	
	/**
	 * Returns if the user is confirmed for login.
	 * @return true or false
	 */
	public boolean isConfirmed(){
		initializeUserData();
		if (arachneUser != null) {
			return arachneUser.isLogin_permission();
		} else {
			return false;
		}
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
	public Map<String,String> getUserData() {
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