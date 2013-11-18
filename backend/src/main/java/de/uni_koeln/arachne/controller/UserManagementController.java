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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.arachne.dao.UserVerwaltungDao;
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
			LOGGER.info(bResult.getAllErrors().toString());
			//return "not valid form-data: " + bResult.getFieldError().getField() + " => " + bResult.getFieldError().getDefaultMessage();
			resultMap.put("status", bResult.getAllErrors());
		} else {
			final String remoteAddr = request.getRemoteAddr();
	        final ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
	        reCaptcha.setPrivateKey("6LfANeoSAAAAAAtryQ12lfb55jkQ6qDIVIdvURbH");
	        
	        final ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, userResponse);
	        
	        if(reCaptchaResponse.isValid()) {
	        	/*final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	        	mailSender.setHost("smtp.uni-koeln.de");
	        	
	        	final SimpleMailMessage mailMessage = new SimpleMailMessage();
	        	mailMessage.setFrom("arachne@uni-koeln.de");
	        	mailMessage.setTo(registerForm.getEmail());
	        	mailMessage.setSubject("Ihre Anfrage an Arachne");
	        	mailMessage.setText("Vielen Dank für Ihre Email an das Arachne-Team! Wir werden diese so schnell wie möglich beantworten.");
	        	
	        	try {
	        		mailSender.send(mailMessage);
	        	} catch(MailException e) {
	        		LOGGER.error(e.getMessage());
	        	}*/
	        	
	        	if(userVerwaltungDao.newUser(registerForm)) {
	        		response.setStatus(200);
	        		resultMap.put("status", "OK");
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
