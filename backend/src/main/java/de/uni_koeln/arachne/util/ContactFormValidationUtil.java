package de.uni_koeln.arachne.util;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Helperclass for contactformdata validation
 * 
 * @author Sven Ole Clemens
 *
 */
public class ContactFormValidationUtil {
	
	@NotEmpty @Size(min = 3, max = 30)
	private String userName;
	
	@NotEmpty @Email
	private String userEmail;
	
	@NotEmpty
	private String category;
	
	@NotEmpty @Size(min = 10, max = 250)
	private String message;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(final String userName) {
		this.userName = userName;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(final String userEmail) {
		this.userEmail = userEmail;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(final String category) {
		this.category = category;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(final String message) {
		this.message = message;
	}
	
}
