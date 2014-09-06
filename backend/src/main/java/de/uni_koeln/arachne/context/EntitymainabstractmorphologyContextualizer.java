package de.uni_koeln.arachne.context;

/**
 * Connects any Entity to it's Morphology via it's Mainabstract.
 */
public class EntitymainabstractmorphologyContextualizer extends AbstractSemanticConnectionPathContextualizer {

	public EntitymainabstractmorphologyContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("morphology");
	}
}
