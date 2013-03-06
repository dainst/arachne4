package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all contexts which are indirectly connected with the current fabric over its connected mainabstract-records.
 * @author Patrick Gunia
 */

public class FabricmainabstractcontextContextualizer extends AbstractSemanticConnectionPathContextualizer {

	public FabricmainabstractcontextContextualizer() {
		super();
	}
	 
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("befund");

	}

}
