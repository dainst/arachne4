package de.uni_koeln.arachne.context;

import java.util.ArrayList;

public class PathtestContextualizer extends
		AbstractSemanticConnectionPathContextualizer {

	@Override
	public String getContextType() {
		// TODO Auto-generated method stub
		return typeStepRestrictions.get(typeStepRestrictions.size()-1);
	}

	@Override
	protected void setupContextPath() {
		
		this.typeStepRestrictions = new ArrayList<String>(2);
		
		typeStepRestrictions.add("buchseite");

		typeStepRestrictions.add("objekt");

	}

}
