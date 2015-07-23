package de.uni_koeln.arachne.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.arachne.service.UserRightsService;


/**
 * Handles requests for the application home page.
 * @author Reimar Grabowski
 */
@Controller
public class HomeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private transient UserRightsService userRightsService; 
	
	/**
	 * Retrieves the username and sends it to the view to render.
	 */
	@RequestMapping(value="/", method=RequestMethod.GET)
	public ModelAndView home(final HttpServletRequest request) {
		
		LOGGER.debug("Welcome to Arachne4 beta!");
		
		final String username = userRightsService.getCurrentUser().getUsername();		
		LOGGER.debug("User: " + username);
		
		final ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("Username", username);
		
		return new ModelAndView("home", modelMap);
	}
	
}

