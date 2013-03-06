package de.uni_koeln.arachne.context;


/**
 * Contextualizer retrieves all morphologys which are indirectly connected with the current fabric over its connected mainabstract-records.
 * @author Patrick Gunia
 *
 */
public class FabricmainabstractmorphologyContextualizer extends AbstractSemanticConnectionPathContextualizer{

	public FabricmainabstractmorphologyContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("morphology");
	}

}
