package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all surfacetreatmentactions which are indirectly connected with the current IsolatedSherd over its connected surfacetreatment-records.
 * @author Patrick Gunia
 */

public class IsolatedsherdsurfacetreatmentsurfacetreatmentactionContextualizer extends AbstractSemanticConnectionPathContextualizer  {

	public IsolatedsherdsurfacetreatmentsurfacetreatmentactionContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("surfacetreatment");
		this.contextPath.addTypeStepRestriction("surfacetreatmentaction");		
	}
}
