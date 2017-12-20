package de.uni_koeln.arachne.context;

/**
 * Connects any Entity to it's Befund via it's Mainabstract.
 */
public class EntitymainabstractbefundContextualizer extends AbstractSemanticConnectionPathContextualizer {

	public EntitymainabstractbefundContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("befund");		
	}
}
