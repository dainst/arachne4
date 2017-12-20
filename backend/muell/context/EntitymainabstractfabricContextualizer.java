package de.uni_koeln.arachne.context;

/**
 * Connects any Entity to it's Fabric via it's Mainabstract.
 */
public class EntitymainabstractfabricContextualizer extends AbstractSemanticConnectionPathContextualizer {

	public EntitymainabstractfabricContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("fabric");
	}
}
