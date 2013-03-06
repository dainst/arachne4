package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all Isolatedsherds which are indirectly connected with the current befund over its connected mainabstract-records.
 * @author Patrick Gunia
 *
 */

public class BefundmainabstractisolatedsherdContextualizer extends AbstractSemanticConnectionPathContextualizer {

	public BefundmainabstractisolatedsherdContextualizer(){
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("isolatedsherd");		
	}
}
