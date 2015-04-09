package de.uni_koeln.arachne.mapping.hibernate;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement
@Entity
@Table(name="verwaltung_benutzer")
@SuppressWarnings("PMD")
public class User {
	
		public enum BOOLEAN {
			TRUE, FALSE
		}

		/**
		 * This is the mapping of the possible connection between tables. 
		 */
		
		/**
		 * This is the Primary key 
		 */
		@Id
		@GeneratedValue(strategy=GenerationType.IDENTITY)
		@Column(name="uid")
		private long uid; 
		/**
		 * GroupID
		 */
		
		@Column(name="gid")
		private String groupID;
		
		/**
		 * The Groups of dataset possesion the User has the Right to view
		 */
		@ManyToMany(fetch=FetchType.EAGER)
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
	   	@Column(name="emailAuth")
	   	private String emailAuth;
	   	
		/**
		 * Is the User allowed to see all groups
		 */
	   	@Column(name="all_groups")
	   	@Enumerated(EnumType.STRING)
		BOOLEAN all_groups;  
	   	
		/**
		 * Is the user allowed to Login
		 */
	   	@Column(name="login_permission")
	   	@Enumerated(EnumType.STRING)
		BOOLEAN login_permission;  
	   	
		/**
		 * Time of the last Login
		 */
	   	@Column(name="LastLogin")
		Date lastLogin;
		
	   	/**
	   	 * List of catalogs owned by the user
	   	 */
		@ManyToMany(fetch=FetchType.EAGER)
		@JoinTable(name="catalog_benutzer",
			joinColumns={@JoinColumn(name="uid")},
			inverseJoinColumns={@JoinColumn(name="catalog_id")})
		private Set<Catalog> catalogs;

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
			if (groupID != null) {
				return Integer.valueOf(groupID);
			} else {
				return 0;
			}
		}

		/**
		 * @param groupID the groupID to set
		 */
		public void setGroupID(final int groupID) {
			this.groupID = String.valueOf(groupID);
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
			//return all_groups;
			//if all_groups == BOOLEAN.TRUE ? return true : return false;
			return (all_groups == BOOLEAN.TRUE) ? true : false;
		}

		/**
		 * @param all_groups the all_groups to set
		 */
		public void setAll_groups(final boolean all_groups) { 
			//this.all_groups = all_groups;
			this.all_groups = (all_groups) ? BOOLEAN.TRUE : BOOLEAN.FALSE;
		}
		
		/**
		 * @return the login_permission
		 */
		public boolean isLogin_permission() { 
			//return login_permission;
			return (login_permission == BOOLEAN.TRUE) ? true : false;
		}

		/**
		 * @param login_permission the login_permission to set
		 */
		public void setLogin_permission(final boolean login_permission) { 
			//this.login_permission = login_permission;
			this.login_permission = (login_permission) ? BOOLEAN.TRUE : BOOLEAN.FALSE;
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
			
			if(all_groups == BOOLEAN.TRUE) {
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
		 * @return the emailAuth
		 */
		public String getEmailAuth() {
			return emailAuth;
		}

		/**
		 * @param emailAuth the emailAuth to set
		 */
		public void setEmailAuth(String emailAuth) {
			this.emailAuth = emailAuth;
		}

		/**
		 * @return the catalogs
		 */
		public Set<Catalog> getCatalogs() {
			return catalogs;
		}

		/**
		 * @param catalogs the catalogs to set
		 */
		public void setCatalogs(Set<Catalog> catalogs) {
			this.catalogs = catalogs;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((all_groups == null) ? 0 : all_groups.hashCode());
			result = prime * result
					+ ((catalogs == null) ? 0 : catalogs.hashCode());
			result = prime * result
					+ ((country == null) ? 0 : country.hashCode());
			result = prime * result
					+ ((datasetGroups == null) ? 0 : datasetGroups.hashCode());
			result = prime * result + ((email == null) ? 0 : email.hashCode());
			result = prime * result
					+ ((emailAuth == null) ? 0 : emailAuth.hashCode());
			result = prime * result
					+ ((firstname == null) ? 0 : firstname.hashCode());
			result = prime * result
					+ ((groupID == null) ? 0 : groupID.hashCode());
			result = prime * result
					+ ((homepage == null) ? 0 : homepage.hashCode());
			result = prime * result
					+ ((institution == null) ? 0 : institution.hashCode());
			result = prime * result
					+ ((lastLogin == null) ? 0 : lastLogin.hashCode());
			result = prime * result
					+ ((lastname == null) ? 0 : lastname.hashCode());
			result = prime
					* result
					+ ((login_permission == null) ? 0 : login_permission
							.hashCode());
			result = prime * result
					+ ((password == null) ? 0 : password.hashCode());
			result = prime * result + ((place == null) ? 0 : place.hashCode());
			result = prime * result
					+ ((street == null) ? 0 : street.hashCode());
			result = prime * result
					+ ((telephone == null) ? 0 : telephone.hashCode());
			result = prime * result + (int) (uid ^ (uid >>> 32));
			result = prime * result
					+ ((username == null) ? 0 : username.hashCode());
			result = prime * result + ((zip == null) ? 0 : zip.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof User)) {
				return false;
			}
			User other = (User) obj;
			if (all_groups != other.all_groups) {
				return false;
			}
			if (catalogs == null) {
				if (other.catalogs != null) {
					return false;
				}
			} else if (!catalogs.equals(other.catalogs)) {
				return false;
			}
			if (country == null) {
				if (other.country != null) {
					return false;
				}
			} else if (!country.equals(other.country)) {
				return false;
			}
			if (datasetGroups == null) {
				if (other.datasetGroups != null) {
					return false;
				}
			} else if (!datasetGroups.equals(other.datasetGroups)) {
				return false;
			}
			if (email == null) {
				if (other.email != null) {
					return false;
				}
			} else if (!email.equals(other.email)) {
				return false;
			}
			if (emailAuth == null) {
				if (other.emailAuth != null) {
					return false;
				}
			} else if (!emailAuth.equals(other.emailAuth)) {
				return false;
			}
			if (firstname == null) {
				if (other.firstname != null) {
					return false;
				}
			} else if (!firstname.equals(other.firstname)) {
				return false;
			}
			if (groupID == null) {
				if (other.groupID != null) {
					return false;
				}
			} else if (!groupID.equals(other.groupID)) {
				return false;
			}
			if (homepage == null) {
				if (other.homepage != null) {
					return false;
				}
			} else if (!homepage.equals(other.homepage)) {
				return false;
			}
			if (institution == null) {
				if (other.institution != null) {
					return false;
				}
			} else if (!institution.equals(other.institution)) {
				return false;
			}
			if (lastLogin == null) {
				if (other.lastLogin != null) {
					return false;
				}
			} else if (!lastLogin.equals(other.lastLogin)) {
				return false;
			}
			if (lastname == null) {
				if (other.lastname != null) {
					return false;
				}
			} else if (!lastname.equals(other.lastname)) {
				return false;
			}
			if (login_permission != other.login_permission) {
				return false;
			}
			if (password == null) {
				if (other.password != null) {
					return false;
				}
			} else if (!password.equals(other.password)) {
				return false;
			}
			if (place == null) {
				if (other.place != null) {
					return false;
				}
			} else if (!place.equals(other.place)) {
				return false;
			}
			if (street == null) {
				if (other.street != null) {
					return false;
				}
			} else if (!street.equals(other.street)) {
				return false;
			}
			if (telephone == null) {
				if (other.telephone != null) {
					return false;
				}
			} else if (!telephone.equals(other.telephone)) {
				return false;
			}
			if (uid != other.uid) {
				return false;
			}
			if (username == null) {
				if (other.username != null) {
					return false;
				}
			} else if (!username.equals(other.username)) {
				return false;
			}
			if (zip == null) {
				if (other.zip != null) {
					return false;
				}
			} else if (!zip.equals(other.zip)) {
				return false;
			}
			return true;
		}
}