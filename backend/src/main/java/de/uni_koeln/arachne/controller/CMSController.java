package de.uni_koeln.arachne.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.response.MenuEntry;
import de.uni_koeln.arachne.response.Node;
import de.uni_koeln.arachne.service.CMSService;

@Controller
public class CMSController {
	
	@Autowired
	private transient CMSService cmsService;
	
	@RequestMapping(value="/node/{id}", method=RequestMethod.GET)
	public @ResponseBody Node page(@PathVariable("id") final Integer id) { // NOPMD		
		return cmsService.getNodeById(id);		
	}
	
	@RequestMapping(value="/menu/{language}", method=RequestMethod.GET)
	public @ResponseBody Map<String, MenuEntry> menu(@PathVariable("language") final String language) {	
		String name = "menu-menu-" + language;
		if ("de".equals(language)) {
			name = "primary-links";
		}
		return cmsService.getMenuByName(name);
	}
	
	@RequestMapping(value="/teasers/{language}", method=RequestMethod.GET)
	public @ResponseBody List<Node> teasers(@PathVariable("language") final String language) {	
		final List<Node> teasers = cmsService.getTeasers(language);
		for (Node node : teasers) {
			node.setBody(null);
		}
		return teasers;
	}

}
