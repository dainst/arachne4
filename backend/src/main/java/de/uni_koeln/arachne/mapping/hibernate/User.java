package de.uni_koeln.arachne.mapping.hibernate;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;

import de.uni_koeln.arachne.util.security.JSONView;
import de.uni_koeln.arachne.util.security.ProtectedObject;
import de.uni_koeln.arachne.util.security.UserAccess;

@XmlRootElement
@JsonInclude(Include.NON_EMPTY)
@Entity
@Table(name = "verwaltung_benutzer")
public class User extends ProtectedObject implements UserDetails {

	/**
	 * Default serial version.
	 */
	private static final long serialVersionUID = 1L;

	public enum BOOLEAN {
		TRUE, FALSE
	}

	/**
	 * This is the mapping of the possible connection between tables.
	 */

	/**
	 * This is the Primary key
	 */
	@JsonView(JSONView.UnprivilegedUser.class)
	@UserAccess(UserAccess.Restrictions.writeprotected)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "uid")
	private long id;

	/**
	 * GroupID
	 */
	@JsonView(JSONView.UnprivilegedUser.class)
	@Column(name = "gid")
	private String groupID;

	/**
	 * The Groups of dataset possesion the User has the Right to view
	 */
	@JsonView(JSONView.User.class)
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "verwaltung_benutzer_datensatzgruppen", joinColumns = {
			@JoinColumn(name = "uid") }, inverseJoinColumns = { @JoinColumn(name = "dgid") })
	private Set<DatasetGroup> datasetGroups = new HashSet<>();

	/**
	 * In Which Table the Connection is Stored
	 */
	@JsonView(JSONView.UnprivilegedUser.class)
	@Column(name = "username")
	private String username;

	@UserAccess(UserAccess.Restrictions.writeprotected)
	@Column
	private String password;

	/**
	 * All user Infos
	 */
	@JsonView(JSONView.User.class)
	@Column(name = "institution")
	private String institution;
	@JsonView(JSONView.User.class)
	@Column(name = "firstname")
	private String firstname;
	@JsonView(JSONView.User.class)
	@Column(name = "lastname")
	private String lastname;
	@JsonView(JSONView.User.class)
	@Column(name = "email")
	private String email;
	@JsonView(JSONView.User.class)
	@Column(name = "strasse")
	private String street;
	@JsonView(JSONView.User.class)
	@Column(name = "plz")
	private String zip;
	@JsonView(JSONView.User.class)
	@Column(name = "ort")
	private String place;
	@JsonView(JSONView.User.class)
	@Column(name = "homepage")
	private String homepage;
	@JsonView(JSONView.User.class)
	@Column(name = "land")
	private String country;
	@JsonView(JSONView.User.class)
	@Column(name = "telefon")
	private String telephone;
	@Column(name = "emailAuth")
	private String emailAuth;

	/**
	 * Is the User allowed to see all groups
	 */
	@JsonView(JSONView.Admin.class)
	@Column(name = "all_groups")
	@Enumerated(EnumType.STRING)
	BOOLEAN all_groups;

	/**
	 * Is the user allowed to Login
	 */
	@JsonView(JSONView.Admin.class)
	@Column(name = "login_permission")
	@Enumerated(EnumType.STRING)
	BOOLEAN login_permission;

	/**
	 * Time of the last Login
	 */
	@JsonView(JSONView.Admin.class)
	@Column(name = "LastLogin")
	Date lastLogin;

	@Transient
	private Collection<? extends GrantedAuthority> authorities = new HashSet<>();

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param uid
	 *            the id to set
	 * @return {@code this}
	 */
	public User setId(final long uid) {
		this.id = uid;
		return this;
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
	 * @param groupID
	 *            the groupID to set
	 * @return {@code this}
	 */
	public User setGroupID(final int groupID) {
		this.groupID = String.valueOf(groupID);
		return this;
	}

	/**
	 * @return the username
	 */
	@Override
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 * @return {@code this}
	 */
	public User setUsername(final String username) {
		this.username = username;
		return this;
	}

	/**
	 * @return the password
	 */
	@XmlTransient
	@Override
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 * @return {@code this}
	 */
	public User setPassword(final String password) {
		this.password = password;
		return this;
	}

	/**
	 * @return the institution
	 */
	public String getInstitution() {
		return institution;
	}

	/**
	 * @param institution
	 *            the institution to set
	 * @return {@code this}
	 */
	public User setInstitution(final String institution) {
		this.institution = institution;
		return this;
	}

	/**
	 * @return the firstname
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 * @param firstname
	 *            the firstname to set
	 * @return {@code this}
	 */
	public User setFirstname(final String firstname) {
		this.firstname = firstname;
		return this;
	}

	/**
	 * @return the lastname
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 * @param lastname
	 *            the lastname to set
	 * @return {@code this}
	 */
	public User setLastname(final String lastname) {
		this.lastname = lastname;
		return this;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 * @return {@code this}
	 */
	public User setEmail(final String email) {
		this.email = email;
		return this;
	}

	/**
	 * @return the street
	 */
	public String getStreet() {
		return street;
	}

	/**
	 * @param street
	 *            the street to set
	 * @return {@code this}
	 */
	public User setStreet(final String street) {
		this.street = street;
		return this;
	}

	/**
	 * @return the zip
	 */
	public String getZip() {
		return zip;
	}

	/**
	 * @param zip
	 *            the zip to set
	 * @return {@code this}
	 */
	public User setZip(final String zip) {
		this.zip = zip;
		return this;
	}

	/**
	 * @return the place
	 */
	public String getPlace() {
		return place;
	}

	/**
	 * @param place
	 *            the place to set
	 * @return {@code this}
	 */
	public User setPlace(final String place) {
		this.place = place;
		return this;
	}

	/**
	 * @return the homepage
	 */
	public String getHomepage() {
		return homepage;
	}

	/**
	 * @param homepage
	 *            the homepage to set
	 * @return {@code this}
	 */
	public User setHomepage(final String homepage) {
		this.homepage = homepage;
		return this;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country
	 *            the country to set
	 * @return {@code this}
	 */
	public User setCountry(final String country) {
		this.country = country;
		return this;
	}

	/**
	 * @return the telephone
	 */
	public String getTelephone() {
		return telephone;
	}

	/**
	 * @param telephone
	 *            the telephone to set
	 * @return {@code this}
	 */
	public User setTelephone(final String telephone) {
		this.telephone = telephone;
		return this;
	}

	/**
	 * @return the all_groups
	 */
	public boolean isAll_groups() {
		return enumToBoolean(all_groups);
	}

	/**
	 * @param all_groups
	 *            the all_groups to set
	 * @return {@code this}
	 */
	public User setAll_groups(final boolean all_groups) {
		this.all_groups = booleanToEnum(all_groups);
		return this;
	}

	/**
	 * @return the login_permission
	 */
	public boolean isLogin_permission() {
		return enumToBoolean(login_permission);
	}

	/**
	 * @param login_permission
	 *            the login_permission to set
	 * @return {@code this}
	 */
	public User setLogin_permission(final boolean login_permission) {
		this.login_permission = booleanToEnum(login_permission);
		return this;
	}

	/**
	 * @return the lastLogin
	 */
	public Date getLastLogin() {
		return lastLogin;
	}

	/**
	 * @param lastLogin
	 *            the lastLogin to set
	 * @return {@code this}
	 */
	public User setLastLogin(final Date lastLogin) {
		this.lastLogin = lastLogin;
		return this;
	}

	/**
	 * @return the datasetGroups
	 */
	public Set<DatasetGroup> getDatasetGroups() {
		return datasetGroups;
	}

	/**
	 * @param datasetGroups
	 *            the datasetGroups to set
	 * @return {@code this}
	 */
	public User setDatasetGroups(final Set<DatasetGroup> datasetGroups) {
		this.datasetGroups = datasetGroups;
		return this;
	}

	public boolean hasGroup(final String group) {

		if (all_groups == BOOLEAN.TRUE) {
			return true;
		}

		for (DatasetGroup datasetGroup : datasetGroups) {
			if (datasetGroup.getName().equals(group)) {
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
	 * @param emailAuth
	 *            the emailAuth to set
	 * @return {@code this}
	 */
	public User setEmailAuth(String emailAuth) {
		this.emailAuth = emailAuth;
		return this;
	}

	public User setAuthorities(final Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
		return this;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return enumToBoolean(login_permission);
	}

	@Override
	public boolean isAccountNonLocked() {
		return enumToBoolean(login_permission);
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return enumToBoolean(login_permission);
	}

	@Override
	public boolean isEnabled() {
		return enumToBoolean(login_permission);
	}

	private boolean enumToBoolean(final BOOLEAN value) {
		return value == BOOLEAN.TRUE ? true : false;
	}

	private BOOLEAN booleanToEnum(final boolean value) {
		return value == true ? BOOLEAN.TRUE : BOOLEAN.FALSE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "User [" + (username != null ? "Username: " + username + "; " : "")
				+ (password != null ? "Password: [PROTECTED]; " : "") + "Enabled: " + isEnabled() + "; "
				+ "AccountNonExpired: " + isAccountNonExpired() + "; " + "CredentialsNonExpired: "
				+ isCredentialsNonExpired() + "; " + "AccountNonLocked: " + isAccountNonLocked() + "; "
				+ (authorities != null ? "Granted Authorities: " + authorities + ", " : "")
				+ (all_groups != null ? "all_groups=" + all_groups + ", " : "")
				+ (login_permission != null ? "login_permission=" + login_permission + ", " : "")
				+ (datasetGroups != null ? "datasetGroups=" + datasetGroups + ", " : "")
				+ ("id=" + id + ", " + (groupID != null ? "groupID=" + groupID + ", " : "")
						+ (institution != null ? "institution=" + institution + ", " : "")
						+ (firstname != null ? "firstname=" + firstname + ", " : "")
						+ (lastname != null ? "lastname=" + lastname + ", " : "")
						+ (email != null ? "email=" + email + ", " : "")
						+ (street != null ? "street=" + street + ", " : "") + (zip != null ? "zip=" + zip + ", " : "")
						+ (place != null ? "place=" + place + ", " : "")
						+ (homepage != null ? "homepage=" + homepage + ", " : "")
						+ (country != null ? "country=" + country + ", " : "")
						+ (telephone != null ? "telephone=" + telephone + ", " : "")
						+ (emailAuth != null ? "emailAuth=" + emailAuth + ", " : "")
						+ (lastLogin != null ? "lastLogin=" + lastLogin : "") + "]");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((all_groups == null) ? 0 : all_groups.hashCode());
		result = prime * result + ((authorities == null) ? 0 : authorities.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((datasetGroups == null) ? 0 : datasetGroups.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((emailAuth == null) ? 0 : emailAuth.hashCode());
		result = prime * result + ((firstname == null) ? 0 : firstname.hashCode());
		result = prime * result + ((groupID == null) ? 0 : groupID.hashCode());
		result = prime * result + ((homepage == null) ? 0 : homepage.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((institution == null) ? 0 : institution.hashCode());
		result = prime * result + ((lastLogin == null) ? 0 : lastLogin.hashCode());
		result = prime * result + ((lastname == null) ? 0 : lastname.hashCode());
		result = prime * result + ((login_permission == null) ? 0 : login_permission.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((place == null) ? 0 : place.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((telephone == null) ? 0 : telephone.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		if (authorities == null) {
			if (other.authorities != null) {
				return false;
			}
		} else if (!authorities.equals(other.authorities)) {
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
		if (id != other.id) {
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