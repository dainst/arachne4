package de.uni_koeln.arachne.context;

/**
 * Connects any entity to the fabricdescriptions of it's mainabstract's fabric.
 */
public class EntitymainabstractfabricfabricdescriptionContextualizer extends AbstractSemanticConnectionPathContextualizer {
	
	public EntitymainabstractfabricfabricdescriptionContextualizer() {
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("fabric");	
		this.contextPath.addTypeStepRestriction("fabricdescription");	
	}
}
