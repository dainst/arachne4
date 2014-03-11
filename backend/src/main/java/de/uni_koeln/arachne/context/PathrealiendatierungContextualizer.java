package de.uni_koeln.arachne.context;

/**
 * Adds the 'datierung' context of the 'objekt' context to the 'realien' dataset as nativ context, so that the 
 * standard 'ort_inc.xml' can be used for 'realien' datasets.
 */
public class PathrealiendatierungContextualizer extends
		AbstractSemanticConnectionPathContextualizer {

	public PathrealiendatierungContextualizer() {
		super();
		contextualizerName = "datierung";
	}

	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("objekt");
		this.contextPath.addTypeStepRestriction("datierung");
	}

}