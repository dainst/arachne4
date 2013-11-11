package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.action.get.GetAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.CeramalexQuantifySpecialNavigationElement;
import de.uni_koeln.arachne.response.AbstractSpecialNavigationElement;
import de.uni_koeln.arachne.response.SpecialNavigationElementList;
import de.uni_koeln.arachne.response.TeiViewerSpecialNavigationElement;

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
	@Autowired
	@Qualifier("teiViewerSpecialNavigationElement")
	private transient TeiViewerSpecialNavigationElement teiViewerSpecialNavigationElement;
	/**
	 * List of all currently avaiable special navigation classes, needs to be
	 * extended, if additional special navigations have to be provided
	 */
	private transient final List<AbstractSpecialNavigationElement> specialNavigationsClasses = new ArrayList<AbstractSpecialNavigationElement>();

	private void init() {
		specialNavigationsClasses.clear();
		specialNavigationsClasses.add(new CeramalexQuantifySpecialNavigationElement());
		specialNavigationsClasses.add(teiViewerSpecialNavigationElement);
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
			@RequestParam(value = "q", required = false, defaultValue = "") final String searchParam,
			@RequestParam(value = "fq", required = false, defaultValue = "") final String filterValues,
			@RequestParam(value = "offset", required = false) final Integer offset,
			@RequestParam(value = "limit", required = false) final Integer limit,
			final HttpServletResponse response) {

		init();
		
		final SpecialNavigationElementList result = new SpecialNavigationElementList();
		
		for (final AbstractSpecialNavigationElement currentNavigationElement : specialNavigationsClasses) {
			if (currentNavigationElement.matches(searchParam, filterValues)) {
				result.addElement(currentNavigationElement.getResult(searchParam, filterValues));
			}
		}
		LOGGER.debug("#Results: " + result.size());
		return result;
	}
}
