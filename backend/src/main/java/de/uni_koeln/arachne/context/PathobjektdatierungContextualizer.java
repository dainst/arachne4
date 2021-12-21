package de.uni_koeln.arachne.context;

/**
 * Adds the 'datierung' context of the 'objekt' context to the dataset as nativ context, so that the 
 * standard 'ort_inc.xml' can be used for the dataset.
 */
public class PathobjektdatierungContextualizer extends
		AbstractSemanticConnectionPathContextualizer {

	public PathobjektdatierungContextualizer() {
		super();
		contextualizerName = "datierung";
	}

	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("objekt");
		this.contextPath.addTypeStepRestriction("datierung");
	}

}