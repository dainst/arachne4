package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all contexts which are indirectly connected with the current fabric over its connected mainabstract-records.
 * @author Patrick Gunia
 */

public class FabricmainabstractbefundContextualizer extends AbstractSemanticConnectionPathContextualizer{

	public FabricmainabstractbefundContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("befund");
	}
	
}
