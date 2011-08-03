package de.uni_koeln.arachne.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.uni_koeln.arachne.service.ArachneEntityIdentificationService;
import de.uni_koeln.arachne.service.BuildingService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.ArachneId;


/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private BuildingService buildingService;
	
	@Autowired
	private UserRightsService userRightsService;
	
	@Autowired
	private ArachneEntityIdentificationService arachneEntityIdentificationService;
	//private Session session = SessionUtil.getSessionFactory().getCurrentSession();
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value="/", method=RequestMethod.GET)
	public ModelAndView home() {
		logger.info("Welcome home!");
		//session.beginTransaction();
		//session.load(Building.class, 1);
		long id = 270001;
		//List<Building> bl = buildingService.listBuilding();
		String username = userRightsService.getUsername();
		ArachneId thing = arachneEntityIdentificationService.getByTablenameAndInternalKey("Bauwerk",id);
		
		logger.info("ID "+ thing.getArachneEntityID());
		
		logger.info(username);
		logger.info("Result get: " + (buildingService.findBuildingById(id).getId()));
		ModelMap modelMap = new ModelMap();
		//modelMap.addAttribute("buildingList", bl);
		modelMap.addAttribute("TODO","ARRRRG");
		modelMap.addAttribute("Shit",username);
		
		return new ModelAndView("home", modelMap);
	}
	
}

