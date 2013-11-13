/**
 * 
 */
package de.uni_koeln.arachne.controller;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
	public ModelAndView register(
			@RequestParam(value="recaptcha_challenge_field") final String challenge,
			@RequestParam(value="recaptcha_response_field") final String userResponse,
			final HttpServletRequest request) {
		
		final String remoteAddr = request.getRemoteAddr();
        final ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        reCaptcha.setPrivateKey("6LfANeoSAAAAAAtryQ12lfb55jkQ6qDIVIdvURbH");
        
        final ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, userResponse);
        
        if(reCaptchaResponse.isValid()) {
        	LOGGER.info("voll valid der Kram!");
        } else {
        	LOGGER.info("nee, nix da... kacke.. setzen 6, noch mal neu!");
        }
		
		return null;
	}
	
}
