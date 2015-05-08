/**
 * 
 */
package de.uni_koeln.arachne.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import de.uni_koeln.arachne.service.MailService;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.StrUtils.FormDataException;
import de.uni_koeln.arachne.util.security.Random;

/**
 * Controller that handles user registration and password reset/activation requests.
 * 
 * @author scuy
 * @author Reimar Grabowski
 */
@Controller
public class UserManagementController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementController.class);
	
	private static final String newLine = System.lineSeparator();
	
	@Autowired
	private transient UserDao userDao;
	
	@Autowired
	private transient ResetPasswordRequestDao resetPasswordRequestDao;
	
	@Autowired
	private transient Random random;
	
	@Autowired
	private transient MailService mailService; 
	
	private transient final List<String> defaultDatasetGroups; 
	private transient final String adminEmail;
	private transient final String serverAddress;
	
	@Autowired
	public UserManagementController(
			final @Value("#{config.defaultDatasetGroups.split(',')}") List<String> defaultDatasetGroups,
			final @Value("#{config.adminEmail}") String adminEmail,
			final @Value("#{config.serverAddress}") String serverAddress) {
		this.defaultDatasetGroups = defaultDatasetGroups;
		this.adminEmail = adminEmail;
		this.serverAddress = serverAddress;
	}
		
	@ResponseBody
	@RequestMapping(value="/user/register", method=RequestMethod.POST, produces="application/json;charset=UTF-8")
	public Map<String,String> register(@RequestBody Map<String,String> formData, HttpServletResponse response) 
			throws FormDataException {
		
		Map<String,String> result = new HashMap<String,String>();
		
		// simple attempt to keep bots from issuing register requests
		if (!(formData.containsKey("iAmHuman") && formData.get("iAmHuman").equals("humanIAm"))) {
			throw new FormDataException("ui.register.bot");
		}
		
		User user = new User();
		
		user.setUsername(StrUtils.getFormData(formData, "username", true, "ui.register."));
		user.setEmail(StrUtils.getFormData(formData, "email", true, "ui.register."));
		user.setPassword(StrUtils.getFormData(formData, "password", true, "ui.register."));
		user.setFirstname(StrUtils.getFormData(formData, "firstname", true, "ui.register."));
		user.setLastname(StrUtils.getFormData(formData, "lastname", true, "ui.register."));
		user.setStreet(StrUtils.getFormData(formData, "street", true, "ui.register."));
		user.setZip(StrUtils.getFormData(formData, "zip", true, "ui.register."));
		user.setPlace(StrUtils.getFormData(formData, "place", true, "ui.register."));
		user.setCountry(StrUtils.getFormData(formData, "country", true, "ui.register."));
		user.setInstitution(StrUtils.getFormData(formData, "institution", false, "ui.register."));
		user.setHomepage(StrUtils.getFormData(formData, "homepage", false, "ui.register."));
		user.setTelephone(StrUtils.getFormData(formData, "telephone", false, "ui.register."));
		user.setAll_groups(false);
		user.setGroupID(500);
		user.setLogin_permission(false);
		
		if (!formData.get("email").equals(formData.get("emailValidation"))) {
			throw new FormDataException("ui.register.emailsDontMatch");
		}
		
		if (!formData.get("password").equals(formData.get("passwordValidation"))) {
			throw new FormDataException("ui.register.passwordsDontMatch");
		}
				
		User existingUser = userDao.findByName(user.getUsername());
		if (existingUser != null) {
			throw new FormDataException("ui.register.usernameTaken");
		}

		HashSet<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		for (String dgName : defaultDatasetGroups) {
			DatasetGroup datasetGroup = userDao.findDatasetGroupByName(dgName);
			if (datasetGroup == null) continue;
			datasetGroups.add(datasetGroup);
		}
		user.setDatasetGroups(datasetGroups);
		
		userDao.createUser(user);
		
		// mail to user
		String messageBody = "Ihre Anmeldung bei Arachne ist eingegangen und wird in Kürze von uns bearbeitet "
				+ "werden." + newLine + newLine + "Mit freundlichen Grüßen" + newLine + "das Arachne-Team";		
    	
    	if (!mailService.sendMail(user.getEmail(), "Ihre Anmeldung bei Arachne", messageBody)) {
    		LOGGER.error("Unable to send registration eMail to user.");
    		throw new FormDataException("ui.registration.emailFailed");
    	}
    	
    	// mail to admin
		messageBody = "Ein Benutzer hat sich mit folgenden Daten bei Arachne registriert:" + newLine + newLine
				+ "Username: " + user.getUsername() + newLine
				+ "Name: " + user.getFirstname() + " " + user.getLastname() + newLine
				+ "E-Mail: " + user.getEmail() + newLine + newLine
				+ "Wenn Sie in Arachne eingeloggt sind, können Sie folgenden Link benutzen um den Benutzer "
    			+ "freizuschalten:" + newLine
    			+ "http://arachne.uni-koeln.de/activate_account/" + user.getId();
    			
    	if (!mailService.sendMail(adminEmail, "Anmeldung bei Arachne", messageBody)) {
    		LOGGER.error("Unable to send registration eMail to admin.");
    		throw new FormDataException("ui.registration.emailToAdminFailed");
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
		
		final String userName = StrUtils.getFormData(userCredentials, "username", true, "ui.passwordreset.");
		final String eMailAddress = StrUtils.getFormData(userCredentials, "email", true, "ui.passwordreset.");
		final String firstName = StrUtils.getFormData(userCredentials, "firstname", true, "ui.passwordreset.");
		final String zipCode = StrUtils.getFormData(userCredentials, "zip", true, "ui.passwordreset.");
		 
		User userByEMailAddress = userDao.findByEMailAddress(eMailAddress);
		User userByName = userDao.findByName(userName);
		if (userByName != null && userByName.equals(userByEMailAddress)) {
			if (userByName.getFirstname().equals(firstName) && userByName.getZip().equals(zipCode)) {
				// get rid of all expired requests
				resetPasswordRequestDao.deleteExpiredRequests();
				final ResetPasswordRequest resetPasswordRequest = resetPasswordRequestDao.getByUserId(userByName.getId());
				// if there is already a request pending do not allow to add a new one
				if (resetPasswordRequest == null) {
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
					resetPasswordRequestDao.save(request);

					// sent mail with activation link to user
					final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					final String nowString = dateFormat.format(now);
					final String expirationDateString = dateFormat.format(expirationDate);
					final String linkString = "http://" + serverAddress + "/user/activation/" + token;

					final String messageBody = "Sie haben ihr Passwort bei Arachne am " + nowString + " zurückgesetzt." 
							+ newLine + "Bitte folgen sie diesem Link um den Prozess abzuschließen: " + linkString 
							+ newLine + "Dieser Link ist bis zum " + expirationDateString + " gültig.";
					
					if (!mailService.sendMail(userByName.getEmail(), "Passwort zurückgesetzt bei Arachne", messageBody)) {
						LOGGER.error("Unable to send password activation eMail to user: " + userByName.getEmail());
						resetPasswordRequestDao.delete(request);
						result.put("success", "false");
						response.setStatus(400);
						return result;
					}
					result.put("success", "true");
					response.setStatus(200);
					return result;
				}
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
			final String newPassword = StrUtils.getFormData(password, "password", true, "ui.passwordactivation.");
			if (newPassword.equals(StrUtils.getFormData(password, "passwordConfirm", true, "ui.passwordactivation."))) {
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
	
	@ResponseBody
	@ExceptionHandler(StrUtils.FormDataException.class)
	public Map<String,String> handleRequiredFieldException(FormDataException e, HttpServletResponse response) {
		Map<String,String> result = new HashMap<String,String>();
		result.put("success", "false");
		result.put("message", e.getMessage());
		response.setStatus(400);
		return result;
	}
}
