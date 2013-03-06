package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all morphologys which are indirectly connected with the current befund over its connected mainabstract-records.
 * @author Patrick Gunia
 *
 */
public class BefundmainabstractmorphologyContextualizer extends AbstractSemanticConnectionPathContextualizer {

	public BefundmainabstractmorphologyContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("morphology");
	}

}
