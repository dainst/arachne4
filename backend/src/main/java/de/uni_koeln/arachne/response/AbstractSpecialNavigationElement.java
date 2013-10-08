package de.uni_koeln.arachne.response;

/**
 * 
 * Interface contain all necessary information to construct special naviagtion controls 
 * (e.g. for project specific computations within the frontend). For every type of project specific
 * navigatation, a concrete subclass has to be implemented containing all necessary information.
 * @author Patrick Gunia
 *
 */

public abstract class AbstractSpecialNavigationElement {
	
	/** Link which gets dynamically computed based on provided parameters, all other values are identical over all instances */
	public String link;
	
	/** How is the special navigation integrated into the frontend? */
	public abstract SpecialNavigationElementTypeEnum getType();
	
	/** On what targets the special navigation? */
	public abstract SpecialNavigationElementTargetEnum getTarget();

	/** Label for button / link */
	public abstract String getTitle();

	/** Request-Mapping for backend-service responsible for project specific request processing */
	public abstract String getRequestMapping();
	
	/** Name of the element / project where the navigation comes from */
	public abstract String getName();

	/** Check wether or not the current special navigation class matches the provided parameters */
	public abstract boolean matches(final String searchParam, final String filterValues);
	
	/** Constructs a result element matching the concrete request params e.g. filterValues and searchParams */
	public abstract AbstractSpecialNavigationElement getResult(final String searchParam, final String filterValues);
	
	/**
	 * Empty default-constructor
	 */
	public AbstractSpecialNavigationElement() {
		super();
	}
	
	/**
	 * Constructs a result-element with the provided link
	 * @param link Link containing all necessary parameters for further processing
	 */
	protected AbstractSpecialNavigationElement(final String link) {
		super();
		this.link = link;
	}

	protected enum SpecialNavigationElementTypeEnum {
		BUTTON, LINK;
	}

	protected enum SpecialNavigationElementTargetEnum {
		INTERN, EXTERN;
	}
}
