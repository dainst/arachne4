package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all types which are indirectly connected with the current isolated sherd over its connected mainabstract-records.
 * @author Patrick Gunia
 */


public class IsolatedsherdmainabstractmorphologyContextualizer extends AbstractSemanticConnectionPathContextualizer{

	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("morphology");
	}

}
