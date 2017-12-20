package de.uni_koeln.arachne.context;

/**
 * Connects any Entity to Isolatedsherds connected to the entity's Mainabstract.
 */
public class EntitymainabstractisolatedsherdContextualizer extends AbstractSemanticConnectionPathContextualizer {

	public EntitymainabstractisolatedsherdContextualizer(){
		super();
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("mainabstract");
		this.contextPath.addTypeStepRestriction("isolatedsherd");		
	}
}

