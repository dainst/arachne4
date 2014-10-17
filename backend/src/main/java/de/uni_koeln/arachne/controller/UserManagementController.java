/**
 * 
 */
package de.uni_koeln.arachne.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.dao.UserVerwaltungDao;
import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.mapping.User;

/**
 * @author scuy
 */
@Controller
public class UserManagementController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementController.class);
	
	@Autowired
	private transient UserVerwaltungDao userVerwaltungDao;
	
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
				
		User existingUser = userVerwaltungDao.findByName(user.getUsername());
		if (existingUser != null) {
			throw new RegistrationException("ui.register.usernameTaken");
		}

		HashSet<DatasetGroup> datasetGroups = new HashSet<DatasetGroup>();
		for (String dgName : defaultDatasetGroups) {
			DatasetGroup datasetGroup = userVerwaltungDao.findDatasetGroupByName(dgName);
			if (datasetGroup == null) continue;
			datasetGroups.add(datasetGroup);
		}
		user.setDatasetGroups(datasetGroups);
		
		userVerwaltungDao.createUser(user);
		
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
	
	/*
	@RequestMapping(value = "/user/register_old", method = RequestMethod.POST)
	public @ResponseBody Object register_old(
			@RequestParam(value="recaptcha_challenge_field", required = true) final String challenge,
			@RequestParam(value="recaptcha_response_field", required = true) final String userResponse,
			@Valid final RegisterForm registerForm,
			final BindingResult bResult,
			final HttpServletRequest request,
			final HttpServletResponse response) throws NoSuchAlgorithmException {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if(bResult.hasErrors()) {
			resultMap.put("status", bResult.getAllErrors());
		} else {
			final String remoteAddr = request.getRemoteAddr();
	        final ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
	        reCaptcha.setPrivateKey("6LfANeoSAAAAAAtryQ12lfb55jkQ6qDIVIdvURbH");
	        
	        final ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, userResponse);
	        
	        if(reCaptchaResponse.isValid()) {
	        	registerForm.setEmailAuth(RandomStringUtils.randomAlphanumeric(24));
	        	
	        	if(userVerwaltungDao.newUser(registerForm)) {
	        		response.setStatus(200);
	        		resultMap.put("status", "OK");
	        		// TODO use MailService
	        		final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	        		mailSender.setHost("smtp.uni-koeln.de");
		        	
		        	//TODO in eigenen Service auslagern
		        	StringBuffer sb = new StringBuffer();
		        	sb.append("http://");
		        	sb.append(request.getServerName());
		        	if(request.getServerPort() != 80) {
		        		sb.append(":");
		        		sb.append(request.getServerPort());
		        	}
		        	sb.append("/");
		        	sb.append("arachnedataservice");
		        	sb.append("/user/confirm/");
		        	sb.append(registerForm.getEmailAuth());
		        	
		        	final SimpleMailMessage mailMessage = new SimpleMailMessage();
		        	mailMessage.setFrom("arachne@uni-koeln.de");
		        	mailMessage.setTo(registerForm.getEmail());
		        	mailMessage.setSubject("Ihre Registrierung bei Arachne");
		        	mailMessage.setText("Vielen Dank für Ihre Registrierung bei Arachne. Um den Prozess abzuschließen, klicken sie bitte folgenden Link: "+sb.toString());
		        	
		        	try {
		        		mailSender.send(mailMessage);
		        	} catch(MailException e) {
		        		LOGGER.error("Sending email after registration failed: ", e);
		        	}
	        	} else {
	        		resultMap.put("status", "serialization error");
	        		response.setStatus(400);
	        	}
	        } else {
	        	resultMap.put("status", reCaptchaResponse.getErrorMessage());
	        	response.setStatus(403);
	        }
		}
		
		registerForm.clearPasswords();
		resultMap.put("registerForm", registerForm);
		return resultMap;
	}*/
	
}
