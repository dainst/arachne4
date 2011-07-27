
import edu.yale.its.tp.cas.client.filter.CASFilter

import javax.servlet.http.HttpSession

import org.springframework.web.context.request.RequestContextHolder


class UserRightsService {
	static scope = "request";
    static transactional = true;

	//Flag that initializeUserDatas if the User Data is loaded
	private boolean isset = false;
	// the Arachne user Dataset
	private def ArachneUsers arachneUser = null;
	//List of the userGroups the user has permissions on
	private def userGroups = [];
	//The Informations about the user
	private def userInfo = [:];
	/**
	 * Private function to retrive the Session.
	 * @return Session
	 */
	private HttpSession getSession() {
		return RequestContextHolder.currentRequestAttributes().getSession();
	}
	
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
			String username = getSession().getAt(CASFilter.CAS_FILTER_USER);
			
			//get User Information from the Database
			 arachneUser =  ArachneUsers.findByUsername( username);
			 
			 //Splitting up all user permissions from the User dataset
			 userGroups = arachneUser.getRightGroups().split(",")
			 //Setting the user person information 
			 //TODO maybe better as object
			 userInfo = [
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
	public List getDataGroups(){
		initializeUserData()
		
		return userGroups
		
	}
	
	/**
	 * Looks up if the User is allowed to see all Groups
	 * @return true or false
	 */
	
	public boolean isAuthorizedForAllGroups(){
		initializeUserData()
		
		return arachneUser.getAll_groups()
		
	}
	/**
	 * is The User confirmed for login
	 * @return true or false
	 */
	public boolean isConfirmed(){
		initializeUserData()
		
		return arachneUser.getLogin_permission()
		
	}
	/**
	 * This Function initializeUserDatas if the User fullfils a specific clearence 
	 * @param permissionNeeded The level of the permission minimal needed
	 * @return boolean true or false
	 */
	public boolean isPermissionLevelValid(int permissionNeeded){
		initializeUserData()
		
		if(permissionNeeded <= arachneUser.getGroupID() )
			return true
		else
			return false
		
	}
	/**
	 * This function Collect all Data about the user
	 * @return Key Value map with all the attributes of the User Info
	 */
	public Map getUserData(){
		initializeUserData()
		return userInfo
		
	}
	/**
	 * Returns a Timestamp as String
	 * @return Timestamp of last login
	 */
	public Date getLastLogin(){
		initializeUserData()
		return arachneUser.getLastLogin()
	}
}
