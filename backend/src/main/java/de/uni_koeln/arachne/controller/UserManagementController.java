/**
 * 
 */
package de.uni_koeln.arachne.controller;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.arachne.dao.UserVerwaltungDao;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.response.StatusResponse;
import de.uni_koeln.arachne.util.RegisterFormValidationUtil;

/**
 * @author Sven Ole Clemens
 *
 */
@Controller
public class UserManagementController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementController.class);
	
	@Autowired
	private transient UserVerwaltungDao userVerwaltungDao;
	
	/**
	 * Method to show the register form
	 * @return
	 */
	@RequestMapping(value = "/user/register", method = RequestMethod.GET)
	public ModelAndView registerFrom() {
		
		return new ModelAndView("registerForm");
	}
	
	@RequestMapping(value = "/user/confirm/{token}", method = RequestMethod.GET)
	public @ResponseBody Object finishRegistration(
			@PathVariable("token") final String token,
			final HttpServletRequest request,
			final HttpServletResponse response) {
		
		final UserAdministration user = userVerwaltungDao.findByAuthToken(token);
		if(user != null) {
			user.setLogin_permission(true);
			user.setEmailAuth(null);
			userVerwaltungDao.updateUser(user);
		}
		StatusResponse statusResponse = new StatusResponse();
		statusResponse.setMessage("MEGAGEIL!!! Du darfst jetzt die hammer Datenbank Arachne nicht nur nutzen, sondern dich dazu noch anmelden!!");
		return statusResponse;
	}
	
	/**
	 * Method request for user registration
	 * @return
	 * @throws NoSuchAlgorithmException 
	 */
	@RequestMapping(value = "/user/register", method = RequestMethod.POST)
	public @ResponseBody Object register(
			@RequestParam(value="recaptcha_challenge_field", required = true) final String challenge,
			@RequestParam(value="recaptcha_response_field", required = true) final String userResponse,
			@Valid final RegisterFormValidationUtil registerForm,
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
	}
	
}
