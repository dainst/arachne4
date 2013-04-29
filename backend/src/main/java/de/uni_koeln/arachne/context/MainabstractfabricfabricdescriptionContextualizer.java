package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all fabricdescriptions which are indirectly connected with the current mainabstract over its connected fabric-records.
 * @author Patrick Gunia
 *
 */

public class MainabstractfabricfabricdescriptionContextualizer extends AbstractSemanticConnectionPathContextualizer {

	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("fabric");
		this.contextPath.addTypeStepRestriction("fabricdescription");	
		
	}

}
