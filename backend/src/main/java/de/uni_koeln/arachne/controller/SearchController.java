package de.uni_koeln.arachne.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.SearchResult;

/**
 * Handles http requests (currently only get) for <code>/search<code>.
 */
@Controller
public class SearchController {
	/**
	 * Handles the http request.
	 * @param searchParam The value of the search parameter.
     * @return A response object containing the data (this is serialized to XML or JSON depending on content negotiation).
     */
	@RequestMapping(value="/search", method=RequestMethod.GET)
	public @ResponseBody SearchResult handleSearchRequest(@RequestParam("q") String searchParam) {
		SearchResult result = new SearchResult();
		result.result = searchParam;
		return result;
	}
}
