package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all fabricdescriptions which are indirectly connected with the current IsolatedSherd over its connected mainabstract- and fabric-records.
 * @author Patrick Gunia
 */

public class IsolatedsherdmainabstractfabricfabricdescriptionContextualizer  extends AbstractSemanticConnectionPathContextualizer {

	public IsolatedsherdmainabstractfabricfabricdescriptionContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("fabric");		
		this.contextPath.addTypeStepRestriction("fabricdescription");
	}
	
}
