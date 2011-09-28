package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;


public class ArachneContext {
	
	/**
	 * Draft!
	 * Type of context as string
	 */
	protected String contextType;
	
	/**
	 * Draft!
	 * List of links of the context
	 */
	protected List<Link> links;
	
	
	
	
	/*
	 * Constructor
	 */
	public ArachneContext(String type) {
		contextType = type;	
		links = new ArrayList<Link>();
	}

	
	
	
	/**
	 * Getters and Setters
	 */
	
	
	public String getContextType() {
		return contextType;
	}

	public void setContextType(String contextType) {
		this.contextType = contextType;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}
	
}
