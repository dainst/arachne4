package de.uni_koeln.arachne.context;

/**
 * Adds the 'literatur' context of the 'objekt' context to the 'realien' dataset as nativ context, so that the 
 * standard 'literatur_inc.xml' can be used for 'realien' datasets.
 */
public class PathrealienliteraturContextualizer extends
		AbstractSemanticConnectionPathContextualizer {

	public PathrealienliteraturContextualizer() {
		super();
		contextualizerName = "literatur";
	}

	@Override
	protected void setupContextPath() {
		this.contextPath.addTypeStepRestriction("objekt");
		this.contextPath.addTypeStepRestriction("literatur");
	}

}
