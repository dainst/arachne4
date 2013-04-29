package de.uni_koeln.arachne.context;

/**
 * Contextualizer retrieves all isolated sherds which are indirectly connected with the current type over its connected mainabstract-records.
 * @author Patrick Gunia
 */

public class MorphologymainabstractisolatedsherdContextualizer extends AbstractSemanticConnectionPathContextualizer{

	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("isolatedsherd");
	}

}
