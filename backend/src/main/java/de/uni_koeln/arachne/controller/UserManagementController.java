/**
 * 
 */
package de.uni_koeln.arachne.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.dao.hibernate.ResetPasswordRequestDao;
import de.uni_koeln.arachne.dao.hibernate.UserDao;
import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.ResetPasswordRequest;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.util.security.Random;

/**
 * @author scuy
 * @author Reimar Grabowski
 */
@Controller
public class UserManagementController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementController.class);
	
	@Autowired
	private transient UserDao userDao;
	
	@Autowired
	private transient ResetPasswordRequestDao resetPasswordRequestDao;
	
	@Autowired
	private transient Random random;
	
	private transient final List<String> defaultDatasetGroups; 
	private transient final String adminEmail;
	
	@Autowired
	public UserManagementController(
			final @Value("#{config.defaultDatasetGroups.split(',')}") List<String> defaultDatasetGroups,
			final @Value("#{config.adminEmail}") String adminEmail) {
		this.defaultDatasetGroups = defaultDatasetGroups;
		this.adminEmail = adminEmail;
	}
		
	@ResponseBody
	@RequestMapping(value="/user/register", method=RequestMethod.POST, produces="application/json;charset=UTF-8")
	public Map<String,String> register(@RequestBody Map<String,String> formData, HttpServletResponse response) {
		
		Map<String,String> result = new HashMap<String,String>();
		
		// simple attempt to keep bots from issuing register requests
		if (!(formData.containsKey("iAmHuman") && formData.get("iAmHuman").equals("humanIAm"))) {
			throw new RegistrationException("ui.register.bot");
		}
		
		User user = new User();
		
		user.setUsername(getFormData(formData, "username", true));
		user.setEmail(getFormData(formData, "email", true));
		user.setPassword(getFormData(formData, "password", true));
		user.setFirstname(getFormData(formData, "firstname", true));
		user.setLastname(getFormData(formData, "lastname", true));
		user.setStreet(getFormData(formData, "street", true));
		user.setZip(getFormData(formData, "zip", true));
		user.setPlace(getFormData(formData, "place", true));
		user.setCountry(getFormData(formData, "country", true));
		user.setInstitution(getFormData(formData, "institution", false));
		user.setHomepage(getFormData(formData, "homepage", false));
		user.setTelephone(getFormData(formData, "telephone", false));
		user.setAll_groups(false);
		user.setGroupID(500);
		user.setLogin_permission(false);
		
		if (!formData.get("email").equals(formData.get("emailValidation"))) {
			throw new RegistrationException("ui.register.emailsDontMatch");
		}
		
		if (!formData.get("password").equals(formData.get("passwordValidation"))) {
			throw new RegistrationException("ui.register.passwordsDontMatch");
		}
				
		User existingUser = userDao.findByName(user.getUsername());
		if (existingUser != null) {
			throw new RegistrationException("ui.register.usernameTaken");
		}

		HashSet<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		for (String dgName : defaultDatasetGroups) {
			DatasetGroup datasetGroup = userDao.findDatasetGroupByName(dgName);
			if (datasetGroup == null) continue;
			datasetGroups.add(datasetGroup);
		}
		user.setDatasetGroups(datasetGroups);
		
		userDao.createUser(user);
		
		final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.uni-koeln.de");
		
		// Mail to User
		final SimpleMailMessage mailMessage = new SimpleMailMessage();
    	mailMessage.setFrom("arachne@uni-koeln.de");
    	mailMessage.setTo(user.getEmail());
    	mailMessage.setSubject("Ihre Anmeldung bei Arachne");
    	mailMessage.setText("Ihre Anmeldung bei Arachne ist eingegangen und wird in Kürze von uns bearbeitet werden.\n\nMit freundlichen Grüßen\ndas Arachne-Team");		
    	try {
    		mailSender.send(mailMessage);
    	} catch(MailException e) {
    		LOGGER.error("Unable to send registration eMail to user.", e);
    		throw new RegistrationException("ui.registration.emailFailed");
    	}
    	
		final SimpleMailMessage mailMessage2 = new SimpleMailMessage();
    	mailMessage2.setFrom("arachne@uni-koeln.de");
    	mailMessage2.setTo(adminEmail);
    	mailMessage2.setSubject("Anmeldung bei Arachne");
    	String text = "Ein Benutzer hat sich mit folgenden Daten bei Arachne registriert:\n\n";
    	text += "Username: " + user.getUsername() + "\n";
    	text += "Name: " + user.getFirstname() + " " + user.getLastname() + "\n";
    	text += "E-Mail: " + user.getEmail() + "\n";
    	text += "\nWenn Sie in Arachne eingeloggt sind, können Sie folgenden Link benutzen um den Benutzer freizuschalten:\n";
		text += "http://arachne.uni-koeln.de/activate_account/" + user.getId();
    	mailMessage2.setText(text);		
    	try {
    		mailSender.send(mailMessage2);
    	} catch(MailException e) {
    		LOGGER.error("Unable to send registration eMail to admin.", e);
    	}
    	
		result.put("success", "true");
		response.setStatus(201);
		return result;
		
	}
	
	/**
	 * If enough information about the user account is provided (meaning user name, eMail address, first name and 
	 * zip code) then a request to change the password of the identified user account is created.
	 * An eMail containing a registration link that is valid for 12 hours is sent to the user.
	 * <br/>
	 * If the validation of the user fails no information is returned why it failed (this is on purpose to not disclose 
	 * information to a potential attacker).
	 * @param userCredentials Credentials to identify the User including the new password as JSON object.
	 * @param response The outgoing HTTP response.
	 * @return A message indicating success or failure.
	 */
	@ResponseBody
	@RequestMapping(value="/user/reset", method=RequestMethod.POST,
			produces="application/json;charset=UTF-8")
	public Map<String,String> reset(@RequestBody Map<String,String> userCredentials, HttpServletResponse response) {
		Map<String,String> result = new HashMap<String,String>();
		
		final String userName = getFormData(userCredentials, "username", true);
		final String eMailAddress = getFormData(userCredentials, "email", true);
		final String firstName = getFormData(userCredentials, "firstname", true);
		final String zipCode = getFormData(userCredentials, "zip", true);
		 
		User userByEMailAddress = userDao.findByEMailAddress(eMailAddress);
		User userByName = userDao.findByName(userName);
		if (userByName != null && userByName.equals(userByEMailAddress)) {
			if (userByName.getFirstname().equals(firstName) && userByName.getZip().equals(zipCode)) {
				final String token = random.getNewToken();
				final Calendar calender = Calendar.getInstance();
				final long now = calender.getTime().getTime();
				calender.setTimeInMillis(now);
				calender.add(Calendar.HOUR_OF_DAY, 12);
				final Timestamp expirationDate = new Timestamp(calender.getTime().getTime()); 
				
				ResetPasswordRequest request = new ResetPasswordRequest();
				request.setToken(token);
				request.setUserId(userByName.getId());
				request.setExpirationDate(expirationDate);
				resetPasswordRequestDao.saveOrUpdate(request);
								
				// sent mail with activation link to user
				final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
				mailSender.setHost("smtp.uni-koeln.de");
				
				final SimpleMailMessage userMail = new SimpleMailMessage();
		    	userMail.setFrom("arachne@uni-koeln.de");
		    	userMail.setTo(userByEMailAddress.getEmail());
		    	userMail.setSubject("Passwort zurückgesetzt bei Arachne");
		    	
		    	final String newLine = System.lineSeparator();
		    	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		    	final String nowString = dateFormat.format(now);
		    	final String expirationDateString = dateFormat.format(expirationDate);
		    	final String linkString = "http://lakota.archaeologie.uni-koeln.de/user/activation/" + token;
		    	
		    	String text = "Sie haben ihr Passwort bei Arachne am " + nowString + " zurückgesetzt." + newLine;
		    	text += "Bitte folgen sie diesem Link um den Prozess abzuschließen: " + linkString + newLine;
		    	text += "Dieser Link ist bis zum " + expirationDateString + " gültig.";
		    	userMail.setText(text);		
		    	try {
		    		mailSender.send(userMail);
		    	} catch(MailException e) {
		    		LOGGER.error("Unable to send registration eMail to admin.", e);
		    	}
				
		    	result.put("success", "true");
				response.setStatus(200);
				return result;
			}	
		}
		result.put("success", "false");
		response.setStatus(400);
		return result;
	}
	
	/**
	 * This method is the second and last step in the 'forgot password' process. It changes the password of a user to 
	 * the provided on.
	 * @param token The token representing the 'PasswordResetRequest'.
	 * @param password The new password to set
	 * @param response The HTTP servlet response.
	 * @return A message indicating success or failure.
	 */
	@ResponseBody
	@RequestMapping(value="/user/activation/{token}", method=RequestMethod.POST,
			produces="application/json;charset=UTF-8")
	public void changePasswordAfterResetRequest(@PathVariable("token") final String token,
			@RequestBody Map<String,String> password, HttpServletResponse response) {
		
		response.setStatus(404);
		final ResetPasswordRequest resetPasswordRequest = resetPasswordRequestDao.getByToken(token);
		if (resetPasswordRequest != null) {
			final String newPassword = getFormData(password, "password", true);
			if (newPassword.equals(getFormData(password, "passwordConfirm", true))) {
				final Calendar calender = Calendar.getInstance();
				final Timestamp now = new Timestamp(calender.getTime().getTime());
				if (now.before(resetPasswordRequest.getExpirationDate())) {
					final User user = userDao.findById(resetPasswordRequest.getUserId());
					user.setPassword(newPassword);
					userDao.updateUser(user);
					response.setStatus(200);
				}
				resetPasswordRequestDao.delete(resetPasswordRequest);
			} else {
				response.setStatus(400);
			}
		}
		return;
	}
	
	private String getFormData(Map<String, String> formData,
			String fieldName, boolean required) {
		if (required && (!formData.containsKey(fieldName) || formData.get(fieldName).isEmpty())) {
			throw new RegistrationException("ui.register.fieldMissing." + fieldName);
		} else {
			return formData.get(fieldName);
		}
	}
	
	@ResponseBody
	@ExceptionHandler(RegistrationException.class)
	public Map<String,String> handleRequiredFieldException(RegistrationException e, HttpServletResponse response) {
		Map<String,String> result = new HashMap<String,String>();
		result.put("success", "false");
		result.put("message", e.getMessage());
		response.setStatus(400);
		return result;
	}
	
	@SuppressWarnings("serial")
	public static class RegistrationException extends RuntimeException {
		public RegistrationException(String message) {
			super(message);
		}
	}
	
}
