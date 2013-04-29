package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all isolated sherds which are indirectly connected with the current surfacetreatmentaction over its connected surfacetreatment-records.
 * @author Patrick Gunia
 */

public class SurfacetreatmentactionsurfacetreatmentisolatedsherdContextualizer extends AbstractSemanticConnectionPathContextualizer{

	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("surfacetreatment");
		this.contextPath.addTypeStepRestriction("isolatedsherd");
	}
}
