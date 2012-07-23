package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.arachne.mapping.DatasetGroup;
import de.uni_koeln.arachne.service.UserRightsService;


/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private transient UserRightsService userRightsService; 
	
	//private Session session = SessionUtil.getSessionFactory().getCurrentSession();
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value="/", method=RequestMethod.GET)
	public ModelAndView home(final HttpServletRequest request) {
		
		LOGGER.debug("Welcome to Arachne4 alpha!");
		
		final String sessionId = request.getSession().getId();
		LOGGER.debug("Session-ID: " + sessionId);
		
		final String username = userRightsService.getCurrentUser().getUsername();		
		LOGGER.debug("User: " + username);
		
		final Set<DatasetGroup> groups = userRightsService.getCurrentUser().getDatasetGroups();
		final List<String> logGroups = new ArrayList<String>();
		for (DatasetGroup group : groups) {
			logGroups.add(group.getName());
		}
		LOGGER.debug("Groups: " + logGroups.toString());
		
		final ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("Username", username);
		
		return new ModelAndView("home", modelMap);
	}
	
}

