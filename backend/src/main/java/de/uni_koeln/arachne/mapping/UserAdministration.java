package de.uni_koeln.arachne.mapping;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement
@Entity
@Table(name="verwaltung_benutzer")
@SuppressWarnings("PMD")
public class UserAdministration {

		/**
		 * This is the mapping of the possible connection between tables. 
		 */
		
		/**
		 * This is the Primary key 
		 */
		@Id
		@Column(name="uid")
		private long uid; 
		/**
		 * GroupID
		 */
		
		@Column(name="gid")
		private int groupID;
		
		/**
		 * The Groups of dataset possesion the User has the Right to view
		 */
		@OneToMany(fetch=FetchType.EAGER)
		@JoinTable(name="verwaltung_benutzer_datensatzgruppen",
			joinColumns={@JoinColumn(name="uid")},
			inverseJoinColumns={@JoinColumn(name="dgid")})
		private Set<DatasetGroup> datasetGroups;
		
		/**
		* In Which Table the Connection is Stored
		*/
		@Column(name="username")
		private String username;
		
		@Column
		private String password;

	   /**
	    * All user Infos
	    */
		@Column(name="institution")
		private String institution;
	   	@Column(name="firstname")
	   	private String firstname; 
	   	@Column(name="lastname")
	   	private String lastname;
	   	@Column(name="email")
	   	private String email;
	   	@Column(name="strasse")
	   	private String street;
	   	@Column(name="plz")
	   	private String zip;
	   	@Column(name="ort")
	   	private String place;
	   	@Column(name="homepage")
	   	private String homepage;
	   	@Column(name="land")
	   	private String country;
	   	@Column(name="telefon")
	   	private String telephone;
	   	
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
	   	
	   	/**
	   	 * The lists of saved bookmarks owned by the user
	   	 */
		@OneToMany(mappedBy="user", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
		private Set<BookmarkList> bookmarkLists;

		/**
		 * @return the id
		 */
		public long getId() {
			return uid;
		}

		/**
		 * @param uid the id to set
		 */
		public void setId(final long uid) {
			this.uid = uid;
		}

		/**
		 * @return the groupID
		 */
		public int getGroupID() {
			return groupID;
		}

		/**
		 * @param groupID the groupID to set
		 */
		public void setGroupID(final int groupID) {
			this.groupID = groupID;
		}

		/**
		 * @return the username
		 */
		public String getUsername() {
			return username;
		}

		/**
		 * @param username the username to set
		 */
		public void setUsername(final String username) {
			this.username = username;
		}

		/**
		 * @return the password
		 */
		@JsonIgnore
		@XmlTransient
		public String getPassword() {
			return password;
		}

		/**
		 * @param password the password to set
		 */
		public void setPassword(final String password) {
			this.password = password;
		}

		/**
		 * @return the institution
		 */
		public String getInstitution() {
			return institution;
		}

		/**
		 * @param institution the institution to set
		 */
		public void setInstitution(final String institution) {
			this.institution = institution;
		}

		/**
		 * @return the firstname
		 */
		public String getFirstname() {
			return firstname;
		}

		/**
		 * @param firstname the firstname to set
		 */
		public void setFirstname(final String firstname) {
			this.firstname = firstname;
		}

		/**
		 * @return the lastname
		 */
		public String getLastname() {
			return lastname;
		}

		/**
		 * @param lastname the lastname to set
		 */
		public void setLastname(final String lastname) {
			this.lastname = lastname;
		}

		/**
		 * @return the email
		 */
		public String getEmail() {
			return email;
		}

		/**
		 * @param email the email to set
		 */
		public void setEmail(final String email) {
			this.email = email;
		}

		/**
		 * @return the street
		 */
		public String getStreet() {
			return street;
		}

		/**
		 * @param street the street to set
		 */
		public void setStreet(final String street) {
			this.street = street;
		}

		/**
		 * @return the zip
		 */
		public String getZip() {
			return zip;
		}

		/**
		 * @param zip the zip to set
		 */
		public void setZip(final String zip) {
			this.zip = zip;
		}

		/**
		 * @return the place
		 */
		public String getPlace() {
			return place;
		}

		/**
		 * @param place the place to set
		 */
		public void setPlace(final String place) {
			this.place = place;
		}

		/**
		 * @return the homepage
		 */
		public String getHomepage() {
			return homepage;
		}

		/**
		 * @param homepage the homepage to set
		 */
		public void setHomepage(final String homepage) {
			this.homepage = homepage;
		}

		/**
		 * @return the country
		 */
		public String getCountry() {
			return country;
		}

		/**
		 * @param country the country to set
		 */
		public void setCountry(final String country) {
			this.country = country;
		}

		/**
		 * @return the telephone
		 */
		public String getTelephone() {
			return telephone;
		}

		/**
		 * @param telephone the telephone to set
		 */
		public void setTelephone(final String telephone) {
			this.telephone = telephone;
		}

		/**
		 * @return the all_groups
		 */
		public boolean isAll_groups() { 
			return all_groups;
		}

		/**
		 * @param all_groups the all_groups to set
		 */
		public void setAll_groups(final boolean all_groups) { 
			this.all_groups = all_groups;
		}

		/**
		 * @return the login_permission
		 */
		public boolean isLogin_permission() { 
			return login_permission;
		}

		/**
		 * @param login_permission the login_permission to set
		 */
		public void setLogin_permission(final boolean login_permission) { 
			this.login_permission = login_permission;
		}

		/**
		 * @return the lastLogin
		 */
		public Date getLastLogin() {
			return lastLogin;
		}

		/**
		 * @param lastLogin the lastLogin to set
		 */
		public void setLastLogin(final Date lastLogin) {
			this.lastLogin = lastLogin;
		}

		/**
		 * @return the datasetGroups
		 */
		public Set<DatasetGroup> getDatasetGroups() {
			return datasetGroups;
		}

		/**
		 * @param datasetGroups the datasetGroups to set
		 */
		public void setDatasetGroups(final Set<DatasetGroup> datasetGroups) {
			this.datasetGroups = datasetGroups;
		}

		public boolean hasGroup(final String group) {
			
			if(all_groups) {
				return true;
			}
			
			for(DatasetGroup datasetGroup : datasetGroups) {
				if(datasetGroup.getName().equals(group)) {
					return true;
				}
			}
			
			return false;
		}

		/**
		 * @return the bookmarkLists
		 */
		public Set<BookmarkList> getBookmarkLists() {
			return bookmarkLists;
		}

		/**
		 * @param bookmarkLists the bookmarkLists to set
		 */
		public void setBookmarkLists(final Set<BookmarkList> bookmarkLists) {
			this.bookmarkLists = bookmarkLists;
		}
}
