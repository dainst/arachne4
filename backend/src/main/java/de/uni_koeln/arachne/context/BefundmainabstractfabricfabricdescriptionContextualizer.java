package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all fabricdescriptions which are indirectly connected with the current befund over its connected mainabstract- and fabric-records.
 * @author Patrick Gunia
 *
 */

public class BefundmainabstractfabricfabricdescriptionContextualizer extends AbstractSemanticConnectionPathContextualizer {

	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("fabric");	
		this.contextPath.addTypeStepRestriction("fabricdescription");	
	}
}
