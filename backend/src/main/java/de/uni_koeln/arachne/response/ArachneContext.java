package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;

public class ArachneContext {
	
	/*
	 * A Specified Context Type like Ort, Literatur, Literaturzitat.
	 */
	protected String contextName;
	
	/*
	 * ArachneDataset which is parent to this context-object
	 */
	protected ArachneDataset parent;
	
	/*
	 * List of links of the context
	 */
	protected List<Link> contextEntities;
	
	/*
	 * An Enumeration which Represents the State of the Context
	 */
	/*
	protected Enum<> completion; 
	{
		FULL, 		// Context is Completely existing with the Sufficent User Rights
		LIMITED, 	// A Limited Number of Contexts ist Fetched minimum 1
		FIRST, 		// The First Entity of this Context exists
		EMPTY		// The Context is Empty		
	}*/
	
	
	/**
	 * Constructor
	 */
	public ArachneContext(String type) {
		contextEntities = new ArrayList<Link>();
	}

	
	
	
	/*
	 * Getters and Setters
	 */

	
}
