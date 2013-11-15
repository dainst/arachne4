package de.uni_koeln.arachne.util;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import de.uni_koeln.arachne.util.validation.Password;

public class RegisterFormValidationUtil {
	
	@Size(max=50, min=5)
	private String institution;
	@NotEmpty @Size(max=50, min=4)
	private String firstName;
	@NotEmpty @Size(max=50, min=4)
	private String surname;
	@NotEmpty @Email
	private String email;
	@Size(min=10) @URL
	private String homepage;
	@NotEmpty @Size(max=50, min=5)
	private String street;
	@NotEmpty @Size(max=15, min=3)
	private String postalCode;
	@NotEmpty @Size(max=50, min=3)
	private String town;
	@NotEmpty @Size(max=50, min=3)
	private String country;
	@Size(max=30, min=5)
	private String phoneNumber;
	@NotEmpty @Size(max=20, min=3)
	private String userName;
	@NotEmpty @Password
	private String password;
	@NotEmpty
	private String conformPassword;
	
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
	public void setPassword(final String password) {
		this.password = password;
	}
	public String getConformPassword() {
		return conformPassword;
	}
	public void setConformPassword(final String conformPassword) {
		this.conformPassword = conformPassword;
	}
}
