package de.uni_koeln.arachne.service;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.UserVerwaltungDao;
import de.uni_koeln.arachne.mapping.UserVerwaltung;

@Service("UserRightsService")
public class UserRightsService {


		@Autowired
		private UserVerwaltungDao userVerwaltungDao;


		
		//Flag that initializeUserDatas if the User Data is loaded
		private boolean isset = false;
		// the Arachne user Dataset
		private UserVerwaltung arachneUser = null;
		//List of the userGroups the user has permissions on
		//private List<String> userGroups;
		private String userGroups;
		
		//The Informations about the user
		private Map<String,String> userInfo ;

		/**
		 * Private function to retrive the Session.
		 * @return Session
		 */
		/*private HttpSession getSession() {
			return RequestContextHolder.currentRequestAttributes().getSession();
		}*/
		
		/**
		 * If the Userdata is not fetched yet,
		 * Fetches the Username from the CAS-Ticket, 
		 * gets the Database row with the User data and preformats it.
		 * Else it does nothing.
		 * @return VOID
		 */
	    private void initializeUserData() {
			
			if(! isset ){
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
				 isset = true;
				
			}

	    }		
		
		public String getUsername(){
			initializeUserData();
			return arachneUser.getUsername();
			}
		/**
		 * Looks up all Group Rights the User has
		 * @return returns a list of Strings with the User Groups the User is intothe user has
		 */
		public String getDataGroups(){
			initializeUserData();
			
			return userGroups;
			
		}
		
		/**
		 * Looks up if the User is allowed to see all Groups
		 * @return true or false
		 */
		
		public boolean isAuthorizedForAllGroups(){
			initializeUserData();
			
			return arachneUser.isAll_groups();
			
		}
		/**
		 * is The User confirmed for login
		 * @return true or false
		 */
		public boolean isConfirmed(){
			initializeUserData();
			
			return arachneUser.isLogin_permission();
			
		}
		/**
		 * This Function initializeUserDatas if the User fullfils a specific clearence 
		 * @param permissionNeeded The level of the permission minimal needed
		 * @return boolean true or false
		 */
		public boolean isPermissionLevelValid(int permissionNeeded){
			initializeUserData();
			
			if(permissionNeeded <= arachneUser.getGroupID() )
				return true;
			else
				return false;
			
		}
		/**
		 * This function Collect all Data about the user
		 * @return Key Value map with all the attributes of the User Info
		 */
		public Map getUserData(){
			initializeUserData();
			return userInfo;
			
		}
		/**
		 * Returns a Timestamp as String
		 * @return Timestamp of last login
		 */
		public Date getLastLogin(){
			initializeUserData();
			return arachneUser.getLastLogin();
		}

		

}
