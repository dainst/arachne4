package de.uni_koeln.arachne.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.arachne.mapping.Building;
import de.uni_koeln.arachne.service.BuildingService;


/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private BuildingService buildingService;
	
	//private Session session = SessionUtil.getSessionFactory().getCurrentSession();
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value="/", method=RequestMethod.GET)
	public ModelAndView home() {
		logger.info("Welcome home!");
		//session.beginTransaction();
		//session.load(Building.class, 1);
		long id = 270000;
		List<Building> bl = buildingService.listBuilding();
		
		logger.info("Result get: " + (buildingService.findBuildingById(id).getId()));
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("buildingList", bl);
		return new ModelAndView("home", modelMap);
	}
	
}

