package de.uni_koeln.arachne.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import de.uni_koeln.arachne.util.validation.Password;

/**
 * Helper class for registerform validation, also beeing
 * user for db-serialization
 * 
 * @author Sven Ole Clemens
 *
 */
@Entity
@Table(name = "verwaltung_benutzer")
public class RegisterFormValidationUtil {
	
	@Id
	@GeneratedValue
	@Column(name = "uid")
	private int userId;
	
	@Column(name = "dgid_alt")
	private String dgidAlt = "NULL";
	
	@Column(name = "institution")
	@Size(max=50, min=5)
	private String institution;
	
	@Column(name = "firstname")
	@NotEmpty @Size(max=50, min=4)
	private String firstName;
	
	@Column(name = "lastname")
	@NotEmpty @Size(max=50, min=4)
	private String surname;
	
	@Column(name = "email")
	@NotEmpty @Email
	private String email;
	
	@Column(name = "homepage")
	@Size(min=10) @URL
	private String homepage;
	
	@Column(name = "strasse")
	@NotEmpty @Size(max=50, min=5)
	private String street;
	
	@Column(name = "plz")
	@NotEmpty @Size(max=15, min=3)
	private String postalCode;
	
	@Column(name = "ort")
	@NotEmpty @Size(max=50, min=3)
	private String town;
	
	@Column(name = "land")
	@NotEmpty @Size(max=50, min=3)
	private String country;
	
	@Column(name = "telefon")
	@Size(max=30, min=5)
	private String phoneNumber;
	
	@Column(name = "username")
	@NotEmpty @Size(max=20, min=3)
	private String userName;
	
	@NotEmpty @Password
	@Column(insertable=false, updatable=false)
	private String password;
	
	@Column(name = "password")
	private String passwordMD;
	
	@NotEmpty @Password
	@Column(insertable=false, updatable=false)
	private String confirmPassword;
	
	@Column(name = "password_confirm")
	private String confirmPasswordMD;
	
	
	public int getUserId() {
		return userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public String getDgidAlt() {
		return dgidAlt;
	}

	public void setDgidAlt(String dgidAlt) {
		this.dgidAlt = dgidAlt;
	}

	public String getInstitution() {
		return institution;
	}
	public void setInstitution(final String institution) {
		this.institution = institution;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(final String surname) {
		this.surname = surname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(final String email) {
		this.email = email;
	}
	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(final String homepage) {
		this.homepage = homepage;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(final String street) {
		this.street = street;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}
	public String getTown() {
		return town;
	}
	public void setTown(final String town) {
		this.town = town;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(final String country) {
		this.country = country;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(final String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(final String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(final String password) throws NoSuchAlgorithmException {
		this.password = password;
		setPasswordMD(password);
	}
	
	public String getPasswordMD() {
		return passwordMD;
	}
	
	/**
	 * Method to set the MD5 password hash
	 * @param password
	 * @throws NoSuchAlgorithmException
	 */
	public void setPasswordMD(final String password) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.update(password.getBytes(), 0, password.length());
		String passmd = new BigInteger(1, messageDigest.digest()).toString(16);
		this.passwordMD = passmd;
	}
	
	public String getConfirmPassword() {
		return confirmPassword;
	}
	
	public void setConfirmPassword(final String confirmPassword) throws NoSuchAlgorithmException {
		this.confirmPassword = confirmPassword;
		setConfirmPasswordMD(confirmPassword);
	}
	
	public String getConfirmPasswordMD() {
		return confirmPasswordMD;
	}
	
	/**
	 * Method to set the MD5 password hash
	 * @param confirmPassword
	 * @throws NoSuchAlgorithmException
	 */
	public void setConfirmPasswordMD(final String confirmPassword) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.update(confirmPassword.getBytes(), 0, confirmPassword.length());
		String passmd = new BigInteger(1, messageDigest.digest()).toString(16);
		this.confirmPasswordMD = passmd;
	}

	/**
	 * Method to clear the password fields
	 */
	public void clearPasswords() {
		password = "";
		passwordMD = "";
		confirmPassword = "";
		confirmPasswordMD = "";
	}
}
