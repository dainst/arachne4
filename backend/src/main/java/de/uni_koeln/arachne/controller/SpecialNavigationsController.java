package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.AbstractSpecialNavigationElement;
import de.uni_koeln.arachne.response.CeramalexQuantifySpecialNavigationElement;
import de.uni_koeln.arachne.response.SpecialNavigationElementList;
import de.uni_koeln.arachne.response.TeiViewerSpecialNavigationElement;

/**
 * 
 * Controller handles project-specific navigation requests and returns a
 * 
 * @author Patrick Gunia
 * @author Sven Ole Clemens
 * 
 */
@Controller
public class SpecialNavigationsController {

	//private static final Logger LOGGER = LoggerFactory.getLogger(SpecialNavigationsController.class);
	
	@Autowired
	@Qualifier("teiViewerSpecialNavigationElement")
	private transient TeiViewerSpecialNavigationElement teiViewerSE;
	/**
	 * List of all currently avaiable special navigation classes, needs to be
	 * extended, if additional special navigations have to be provided.
	 * To fill the list, look at the init-method. You can create new objects in
	 * that method or use autowired ones.
	 */
	private transient final List<AbstractSpecialNavigationElement> specNavClasses = new ArrayList<AbstractSpecialNavigationElement>();

	/**
	 * Method that fills the specNavClasses List in runtime.
	 * This is needed, becouse the autowirded objects have to be
	 * loaded first.
	 */
	private void init() {
		specNavClasses.clear();
		specNavClasses.add(new CeramalexQuantifySpecialNavigationElement());
		specNavClasses.add(teiViewerSE);
	}

	/**
	 * Method handles different specialNavigation-request. It uses the regular
	 * elasticsearch query- and facet-parameters or entityID to first receive a list of
	 * dedicated-records and afterwards retrieves a list of all avaiable
	 * navigationelements connected with them. These are summed and passed back
	 * as JSP which can then be rendered by the frontend.
	 * 
	 * @param searchParam
	 *            The value of the search parameter. (mandatory)
	 * @param filterValues
	 *            The values of the elasticsearch filter query. (optional)
	 * @param facetLimit
	 *            The maximum number of facets. (optional)
	 * @return A response object containing the data or a status response (this
	 *         is serialized to XML or JSON depending on content negotiation).
	 */
	@RequestMapping(value = "/specialNavigationsService", method = RequestMethod.GET)
	public @ResponseBody
	SpecialNavigationElementList handleSearchRequest(
			@RequestParam(value = "q", required = false, defaultValue = "") final String searchParam,
			@RequestParam(value = "fq", required = false, defaultValue = "") final String filterValues,
			@RequestParam(value = "offset", required = false) final Integer offset,
			@RequestParam(value = "limit", required = false) final Integer limit,
			final HttpServletResponse response) {

		// create the list of classes for the matching process
		init();
		
		final SpecialNavigationElementList result = new SpecialNavigationElementList();
		
		for (final AbstractSpecialNavigationElement currentNavigationElement : specNavClasses) {
			if (currentNavigationElement.matches(searchParam, filterValues)) {
				result.addElement(currentNavigationElement.getResult(searchParam, filterValues));
			}
		}
		
		return result;
	}
}
