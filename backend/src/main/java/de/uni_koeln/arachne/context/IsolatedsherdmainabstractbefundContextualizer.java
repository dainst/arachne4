package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all contexts which are indirectly connected with the current IsolatedSherd over its connected mainabstract-records.
 * @author Patrick Gunia
 */

public class IsolatedsherdmainabstractbefundContextualizer extends AbstractSemanticConnectionPathContextualizer {

	public IsolatedsherdmainabstractbefundContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("befund");		
	}
}
