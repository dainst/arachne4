package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.CeramalexQuantifySpecialNavigationElement;
import de.uni_koeln.arachne.response.SpecialNavigationElement;
import de.uni_koeln.arachne.response.SpecialNavigationElementList;

/**
 * 
 * Controller handles project-specific navigation requests and returns a
 * 
 * @author Patrick Gunia
 * 
 */
@Controller
public class SpecialNavigationsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpecialNavigationsController.class);
	
	/**
	 * List of all currently avaiable special navigation classes, needs to be
	 * extended, if additional special navigations have to be provided
	 */
	private List<SpecialNavigationElement> specialNavigationsClasses = new ArrayList<SpecialNavigationElement>();

	public SpecialNavigationsController() {
		specialNavigationsClasses
				.add(new CeramalexQuantifySpecialNavigationElement());
	}

	/**
	 * Method handles a Ceramalex-quantify-request. It uses the regular
	 * elasticsearch query- and facet-parameters to first receive a list of
	 * mainabstract-records and afterwards retrieves a list of all avaiable
	 * quantities-records connected with them. These are summed and passed back
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
			@RequestParam("q") final String searchParam,
			@RequestParam(value = "fq", required = false) final String filterValues,
			@RequestParam(value = "offset", required = false) final Integer offset,
			@RequestParam(value = "limit", required = false) final Integer limit,
			final HttpServletResponse response) {

		final SpecialNavigationElementList result = new SpecialNavigationElementList();
		
		for (SpecialNavigationElement cur : specialNavigationsClasses) {
			if (cur.matches(searchParam, filterValues)) {
				result.addElement(cur.getResult(searchParam, filterValues));
			}
		}
		LOGGER.debug("#Results: " + result.size());
		return result;
	}
}
