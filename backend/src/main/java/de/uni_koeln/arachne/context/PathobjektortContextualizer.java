package de.uni_koeln.arachne.context;

/**
 * Adds the 'ort' context of the 'objekt' context to the dataset as nativ context, so that the 
 * standard 'ort_inc.xml' can be used for the dataset.
 */
public class PathobjektortContextualizer extends
		AbstractSemanticConnectionPathContextualizer {

	public PathobjektortContextualizer() {
		super();
		contextualizerName = "ort";
	}
	
	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("objekt");
		this.contextPath.addTypeStepRestriction("ort");
	}
}
