

class ArachneUsers {

	/**
	 * This is the mapping of the possible connection between tables. 
	 */
	
	/**
	 * This is the Primary key 
	 */
	int id
	/**
	 * GroupID
	 */
	int groupID
	/**
	 * The Groups of dataset possesion the User has the Right to view. comma seperated
	 */

	String rightGroups
	
	/**
	* In Which Table the Connection is Stored
	*/

   String username

   /**
    * All user Infos
    */
   	String institution
 	String firstname	 
	String lastname
	String email
	String street
	String zip
	String place
	String homepage
	String country
	String telephone
	/**
	 * Is the User allowed to see all groups
	 */
	boolean all_groups
	/**
	 * Is the user allowed to Login
	 */
	boolean login_permission
	/**
	 * Time of the last Login
	 */
	Date lastLogin
	/**
	* The actual object relational mapping.
	*/
   static mapping = {
	   table 'verwaltung_benutzer'
	   id column: 'uid'
	   groupID column: 'gid'
	   rightGroups column:'dgid'
	   username column: 'username'
	   institution column: 'institution'
	   firstname column: 'firstname'
	   lastname column: 'lastname'
	   email column: 'email'
	   street column: 'strasse'
	   zip column: 'plz'
	   place column: 'ort'
	   homepage column: 'homepage'
	   country column: 'land'
	   telephone column: 'telefon'
	   all_groups column: 'all_groups'
	   login_permission column: 'login_permission'
	   lastLogin column: 'LastLogin'
	   version false
   }
   /**
   * Not used.
   */
    static constraints = {
    }
}
