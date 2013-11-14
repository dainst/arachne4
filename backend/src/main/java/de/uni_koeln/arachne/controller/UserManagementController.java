/**
 * 
 */
package de.uni_koeln.arachne.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.arachne.util.ContactFormValidationUtil;

/**
 * @author Sven Ole Clemens
 *
 */
@Controller
public class UserManagementController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementController.class);
	
	
	/**
	 * Method to show the register form
	 * @return
	 */
	@RequestMapping(value = "/user/register", method = RequestMethod.GET)
	public ModelAndView registerFrom() {
		
		return new ModelAndView("registerForm");
	}
	
	/**
	 * Method to create the new a new user
	 * @return
	 */
	@RequestMapping(value = "/user/register", method = RequestMethod.POST)
	public @ResponseBody Object register(
			@RequestParam(value="recaptcha_challenge_field", required = true) final String challenge,
			@RequestParam(value="recaptcha_response_field", required = true) final String userResponse,
			/**@RequestParam(value = "userName", required = true) final String userName,
			@RequestParam(value = "userEmail", required = true) final String userEmail,
			@RequestParam(value = "category", required = true) @Validated() final String category,
			@RequestParam(value = "message", required = true) final String message,*/
			@Valid final ContactFormValidationUtil contactForm,
			final BindingResult bResult,
			final HttpServletRequest request) {
		
		if(bResult.hasErrors()) {
			LOGGER.info(bResult.getAllErrors().toString());
			return "not valid form-data: " + bResult.getFieldError().getField() + " => " + bResult.getFieldError().getDefaultMessage();
		} else {
			final String remoteAddr = request.getRemoteAddr();
	        final ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
	        reCaptcha.setPrivateKey("6LfANeoSAAAAAAtryQ12lfb55jkQ6qDIVIdvURbH");
	        
	        final ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, userResponse);
	        
	        if(reCaptchaResponse.isValid()) {
	        	final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	        	mailSender.setHost("smtp.uni-koeln.de");
	        	
	        	final SimpleMailMessage mailMessage = new SimpleMailMessage();
	        	mailMessage.setFrom("arachne@uni-koeln.de");
	        	mailMessage.setTo(contactForm.getUserEmail());
	        	mailMessage.setSubject("Ihre Anfrage an Arachne");
	        	mailMessage.setText("Vielen Dank für Ihre Email an das Arachne-Team! Wir werden diese so schnell wie möglich beantworten.");
	        	
	        	try {
	        		mailSender.send(mailMessage);
	        	} catch(MailException e) {
	        		LOGGER.error(e.getMessage());
	        	}
	        	
	        	return "voll valid der Kram!";
	        } else {
	        	return "nee, nix da... kacke.. setzen 6, noch mal neu!";
	        }
		}
	}
	
}
