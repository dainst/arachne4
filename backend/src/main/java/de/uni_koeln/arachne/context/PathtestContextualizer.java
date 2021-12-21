package de.uni_koeln.arachne.context;

public class PathtestContextualizer extends
		AbstractSemanticConnectionPathContextualizer {



	@Override
	protected void setupContextPath() {
		

		contextPath.addTypeStepRestriction("buchseite");

		contextPath.addTypeStepRestriction("objekt");

	}

}
