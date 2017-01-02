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
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static de.uni_koeln.arachne.util.FormDataUtils.*;
import de.uni_koeln.arachne.dao.hibernate.ResetPasswordRequestDao;
import de.uni_koeln.arachne.dao.hibernate.UserDao;
import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.ResetPasswordRequest;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.service.MailService;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.network.CustomMediaType;
import de.uni_koeln.arachne.util.security.JSONView;
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

	@Value("${testUserName:null}") // should default to null value in when using production configuration
	private String testUserName;

	@Autowired
	private transient UserDao userDao;
	
	@Autowired
	private transient ResetPasswordRequestDao resetPasswordRequestDao;
	
	@Autowired
	private transient Random random;
	
	@Autowired
	private transient MailService mailService; 
	
	@Autowired
	private transient UserRightsService userRightsService;
	
	private transient final List<String> defaultDatasetGroups; 
	private transient final String adminEmail;
	private transient final String serverAddress;
	
	/**
	 * Constructor setting the default dataset groups, the admin eMail and the server address. 
	 * @param defaultDatasetGroups The list of default dataset groups.
	 * @param adminEmail The admin eMail address.
	 * @param serverAddress The server address.
	 */
	@Autowired
	public UserManagementController(
			final @Value("#{'${defaultDatasetGroups}'.split(',')}") List<String> defaultDatasetGroups,
			final @Value("${adminEmail}") String adminEmail,
			final @Value("${serverAddress}") String serverAddress) {
		this.defaultDatasetGroups = defaultDatasetGroups;
		this.adminEmail = adminEmail;
		this.serverAddress = serverAddress;
	}
	
	/**
	 * End point to retrieve user information. Admins get more information returned than a normal user.
	 * @param username The username of interest.
	 * @return A JSON serialization of the corresponding User object.
	 */
	// use regex addon to disable extension recognition and allow usernames that include dots
	@RequestMapping(value="/userinfo/{username:.+}",
			method=RequestMethod.GET, 
			produces={CustomMediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<MappingJacksonValue> getUserInfo(@PathVariable("username") String username) {
		if (userRightsService.isSignedInUser()) {
			if (userRightsService.userHasAtLeastGroupID(UserRightsService.MIN_ADMIN_ID)) {
				User user = userDao.findByName(username);
				MappingJacksonValue wrapper = new MappingJacksonValue(user);
				wrapper.setSerializationView(JSONView.Admin.class);
				return ResponseEntity.ok(wrapper);
			} else {
				User currentUser = userRightsService.getCurrentUser();
				if (currentUser.equals(userDao.findByName(username))) {
					MappingJacksonValue wrapper = new MappingJacksonValue(currentUser);
					wrapper.setSerializationView(JSONView.User.class);
					return ResponseEntity.ok(wrapper);
				}
			}
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	}
	
	/**
	 * End point to update (not create) user information. Admins can change more fields (all_groups, etc.) than a 
	 * normal user.
	 * @param username The username of interest.
	 * @param formData The form data as map.
	 * @return The JSON serialization of a success message.
	 * @throws FormDataException if the email address is invalid or already taken or the username is already taken.
	 */
	@RequestMapping(value="/userinfo/{username}", 
			method=RequestMethod.PUT, 
			produces={CustomMediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<Map<String,String>> updateUserInfo(@PathVariable("username") String username, 
			@RequestBody Map<String,String> formData) throws FormDataException {
		if (userRightsService.isSignedInUser()) {
			checkForBot(formData, "ui.update.");
			// remove bot data as we want to traverse the map later on
			formData.remove("iAmHuman");
			Map<String,String> result = new HashMap<String,String>();
			User user = userDao.findByName(username);
			
			// check if new email shall be set and if then check if it is unique
			final String eMail = getFormData(formData, "email", false, "ui.update.");
			if (!StrUtils.isEmptyOrNull(eMail)) {
				if (!mailService.isValidEmailAddress(eMail)) {
					throw new FormDataException("ui.update.emailInvalid");
				}
				final User existingUser = userDao.findByEMailAddress(eMail);
				if (existingUser != null && !existingUser.equals(user)) {
					throw new FormDataException("ui.update.emailTaken");
				}
			}
			
			// check if new username shall be set and if then check if it is unique
			final String usernameForm = getFormData(formData, "username", false, "ui.update.");
			if (!StrUtils.isEmptyOrNull(usernameForm)) {
				final User existingUser = userDao.findByName(usernameForm);
				if (existingUser != null && !existingUser.equals(user)) {
					throw new FormDataException("ui.update.usernameTaken");
				}
			}
			
			if (userRightsService.userHasAtLeastGroupID(UserRightsService.MIN_ADMIN_ID) ||
					userRightsService.getCurrentUser().equals(user)) {
				
				try {
					for (Map.Entry<String, String> entry : formData.entrySet()) {
						userRightsService.setPropertyOnProtectedObject(entry.getKey(), entry.getValue(), user
								, UserRightsService.MIN_USER_ID);
					}
					userDao.updateUser(user);
				} catch (de.uni_koeln.arachne.service.UserRightsService.ObjectAccessException e) {
					result.put("Exception", e.getMessage());
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
				} catch (DataAccessException e) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
				}
			}
			result.put("success", "true");
			return ResponseEntity.ok(result);
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	}
	
	/**
	 * HTTP endpoint for registering a new user.
	 * @param formData The registration from data.
	 * @param response The HTTP response.
	 * @return The JSON serialization of a success message.
	 * @throws FormDataException if the form data cannot be validated.
	 */
	@ResponseBody
	@RequestMapping(value="/user/register", 
			method=RequestMethod.POST, 
			produces={CustomMediaType.APPLICATION_JSON_UTF8_VALUE})
	public Map<String,String> register(@RequestBody Map<String,String> formData, HttpServletResponse response) 
			throws FormDataException {
		
		Map<String,String> result = new HashMap<String,String>();
		if (!userRightsService.isSignedInUser()) {
			checkForBot(formData, "ui.register.");

			User user = new User();
			
			user.setUsername(getFormData(formData, "username", true, "ui.register."));
			User existingUser = userDao.findByName(user.getUsername());
			if (existingUser != null) {
				throw new FormDataException("ui.register.usernameTaken");
			}
			
			final String eMail = getFormData(formData, "email", true, "ui.register.");
			if (!mailService.isValidEmailAddress(eMail)) {
				throw new FormDataException("ui.register.emailInvalid");
			}
			
			if (userDao.findByEMailAddress(eMail) != null) {
				throw new FormDataException("ui.register.emailTaken");
			}
			
			if (!eMail.equals(formData.get("emailValidation"))) {
				throw new FormDataException("ui.register.emailsDontMatch");
			}
			user.setEmail(eMail);
			
			final String password = getFormData(formData, "password", true, "ui.register.");
			if (!password.equals(formData.get("passwordValidation"))) {
				throw new FormDataException("ui.register.passwordsDontMatch");
			}
			if (password.length() < 10) {
				throw new FormDataException("ui.register.passwordTooShort");
			}
			user.setPassword(password);
			
			user.setFirstname(getFormData(formData, "firstname", true, "ui.register."));
			user.setLastname(getFormData(formData, "lastname", true, "ui.register."));
			user.setStreet(getFormData(formData, "street", true, "ui.register."));
			user.setZip(getFormData(formData, "zip", true, "ui.register."));
			user.setPlace(getFormData(formData, "place", true, "ui.register."));
			user.setCountry(getFormData(formData, "country", true, "ui.register."));
			user.setInstitution(getFormData(formData, "institution", false, "ui.register."));
			user.setHomepage(getFormData(formData, "homepage", false, "ui.register."));
			user.setTelephone(getFormData(formData, "telephone", false, "ui.register."));
			user.setAll_groups(false);
			user.setGroupID(500);
			if(isTestUser(user)){
				user.setLogin_permission(true);
			}
			else {
				user.setLogin_permission(false);
			}

			HashSet<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
			for (String dgName : defaultDatasetGroups) {
				DatasetGroup datasetGroup = userDao.findDatasetGroupByName(dgName);
				if (datasetGroup == null) continue;
				datasetGroups.add(datasetGroup);
			}
			user.setDatasetGroups(datasetGroups);

			userDao.createUser(user);

			if(!isTestUser(user)){

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
			}

			result.put("success", "true");
			response.setStatus(201);

		} else {
			result.put("success", "false");
			response.setStatus(400);
		}
		return result;

	}

	/**
	 * HTTP endpoint for deleting a user. Users can delete themselves or be deleted by admins.
	 * @param username The username of interest.
	 * @param response The HTTP response.
	 * @return The JSON serialization of a success message.
	 */

	@ResponseBody
	@RequestMapping(value="/userinfo/{username}",
			method=RequestMethod.DELETE,
			produces= {CustomMediaType.APPLICATION_JSON_UTF8_VALUE})
	public Map<String,String> delete(@PathVariable("username") String username, HttpServletResponse response) {
		Map<String,String> result = new HashMap<String,String>();

		if(!userRightsService.isSignedInUser()) {
			result.put("success", "false");
			response.setStatus(401);
			return result;
		}

		if(username.compareTo(userRightsService.getCurrentUser().getUsername()) != 0
				&& !userRightsService.userHasAtLeastGroupID(UserRightsService.MIN_ADMIN_ID)) {
			result.put("success", "false");
			response.setStatus(403);
			return result;
		}

		userDao.deleteUser(userDao.findByName(username));

		result.put("success", "true");
		response.setStatus(200);
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
	@RequestMapping(value="/user/reset", 
	method=RequestMethod.POST,
	produces= {CustomMediaType.APPLICATION_JSON_UTF8_VALUE})
	public Map<String,String> reset(@RequestBody Map<String,String> userCredentials, HttpServletResponse response) {
		Map<String,String> result = new HashMap<String,String>();

		checkForBot(userCredentials, "ui.passwordreset.");		
		if (!userRightsService.isSignedInUser()) {
			final String userName = getFormData(userCredentials, "username", true, "ui.passwordreset.");
			final String eMailAddress = getFormData(userCredentials, "email", true, "ui.passwordreset.");
			final String firstName = getFormData(userCredentials, "firstname", true, "ui.passwordreset.");
			final String zipCode = getFormData(userCredentials, "zip", true, "ui.passwordreset.");

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

						if (!isTestUser(userByName) && !mailService.sendMail(userByName.getEmail(), "Passwort zurückgesetzt bei Arachne", messageBody)) {
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
	 */
	@ResponseBody
	@RequestMapping(value="/user/activation/{token}",
			method=RequestMethod.POST,
			produces={CustomMediaType.APPLICATION_JSON_UTF8_VALUE})
	public void changePasswordAfterResetRequest(@PathVariable("token") final String token,
			@RequestBody Map<String,String> password, HttpServletResponse response) {
		
		checkForBot(password, "ui.passwordactivation.");
		
		response.setStatus(404);
		if (!userRightsService.isSignedInUser()) {
			final ResetPasswordRequest resetPasswordRequest = resetPasswordRequestDao.getByToken(token);
			if (resetPasswordRequest != null) {
				final String newPassword = getFormData(password, "password", true, "ui.passwordactivation.");
				if (newPassword.equals(getFormData(password, "passwordConfirm", true, "ui.passwordactivation."))) {
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
		}
	}
	
	/**
	 * Exception handler for {@link FormDataException}s.
	 * @param e The exception.
	 * @param response The HTTP response.
	 * @return A message indicating failure including the original exceptions message.
	 */
	@ResponseBody
	@ExceptionHandler(FormDataException.class)
	public Map<String,String> handleRequiredFieldException(FormDataException e, HttpServletResponse response) {
		Map<String,String> result = new HashMap<String,String>();
		result.put("success", "false");
		result.put("message", e.getMessage());
		response.setStatus(400);
		return result;
	}

	private boolean isTestUser(User user) {
		if(testUserName == null)
			return false;
		else
			return user.getUsername().compareTo(testUserName) == 0;
	}
}
