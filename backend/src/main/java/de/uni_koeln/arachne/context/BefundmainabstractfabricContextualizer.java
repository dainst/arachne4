package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all fabrics which are indirectly connected with the current befund over its connected mainabstract-records.
 * @author Patrick Gunia
 *
 */

public class BefundmainabstractfabricContextualizer extends AbstractSemanticConnectionPathContextualizer{

	public BefundmainabstractfabricContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("fabric");		
	}

}
