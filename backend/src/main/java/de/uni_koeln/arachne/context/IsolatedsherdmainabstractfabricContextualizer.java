package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all fabrics which are indirectly connected with the current IsolatedSherd over its connected mainabstract-records.
 * @author Patrick Gunia
 */

public class IsolatedsherdmainabstractfabricContextualizer extends AbstractSemanticConnectionPathContextualizer {
	
	public IsolatedsherdmainabstractfabricContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("fabric");		
	}

	
}
