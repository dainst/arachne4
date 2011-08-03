package de.uni_koeln.arachne.mapping;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;

@Entity
@Table(name="verwaltung_benutzer")
public class UserVerwaltung {

		/**
		 * This is the mapping of the possible connection between tables. 
		 */
		
		/**
		 * This is the Primary key 
		 */
		@Id
		@Column(name="uid")
		long id;
		/**
		 * GroupID
		 */
		@Column(name="gid")
		int groupID;
		/**
		 * The Groups of dataset possesion the User has the Right to view. comma seperated
		 */

		@Column(name="dgid")
		String rightGroups;
		
		/**
		* In Which Table the Connection is Stored
		*/
		@Column(name="username")
		String username;

	   /**
	    * All user Infos
	    */
		@Column(name="institution")
	   	String institution;
	   	@Column(name="firstname")
	 	String firstname; 
	   	@Column(name="lastname")
		String lastname;
	   	@Column(name="email")
		String email;
	   	@Column(name="strasse")
		String street;
	   	@Column(name="plz")
		String zip;
	   	@Column(name="ort")
		String place;
	   	@Column(name="homepage")
		String homepage;
	   	@Column(name="land")
		String country;
	   	@Column(name="telefon")
		String telephone;
		/**
		 * Is the User allowed to see all groups
		 */
	   	@Column(name="all_groups")
		boolean all_groups;
		/**
		 * Is the user allowed to Login
		 */
	   	@Column(name="login_permission")
		boolean login_permission;
		/**
		 * Time of the last Login
		 */
	   	@Column(name="LastLogin")
		Date lastLogin;
		
	   	public String getCountry() {
			return country;
		}
	   	public String getEmail() {
			return email;
		}
	   	public String getFirstname() {
			return firstname;
		}
	   	public int getGroupID() {
			return groupID;
		}
	   	public String getHomepage() {
			return homepage;
		}
	   	public long getId() {
			return id;
		}
	   	public String getInstitution() {
			return institution;
		}
	   	public Date getLastLogin() {
			return lastLogin;
		}
	   	
	   	public String getLastname() {
			return lastname;
		}
		
	   	public String getPlace() {
			return place;
		}
	   	public String getRightGroups() {
			return rightGroups;
		}
	   	public String getStreet() {
			return street;
		}
	   	public String getTelephone() {
			return telephone;
		}
	   	public String getZip() {
			return zip;
		}
	   	public boolean isAll_groups() {
			return all_groups;
		}
	   	public String getUsername() {
			return username;
		}
	   	public boolean isLogin_permission() {
			return login_permission;
		}


}
