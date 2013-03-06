package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all contexts which are indirectly connected with the current morphology over its connected mainabstract-records.
 * @author Patrick Gunia
 *
 */
public class MorphologymainabstractbefundContextualizer extends AbstractSemanticConnectionPathContextualizer {

	public MorphologymainabstractbefundContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("befund");		
	}
}
