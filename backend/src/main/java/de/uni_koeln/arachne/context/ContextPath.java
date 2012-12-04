package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

public class ContextPath {
	//The Simple Types All Things on the way to the target must have 
	protected List<String> typeStepRestrictions=null;
	//The Restriction of the Connection types the Path must have. an Empty String means that there are no Restrictions
	protected List<String> semanticConnectionRestrictions= null;
	//TODO Implement an Mechanism that can Restict Values of Fields i the Additional JSON Fields
	protected List<String> fieldValueRestrictions = null;
	
	
	/**
	 * 
	 * @return Get List of Strings that contain the Step restiction
	 */
	public List<String> getTypeStepRestrictions() {
		return typeStepRestrictions;
	}
	
	public List<String> getSemanticConnectionRestrictions() {
		return semanticConnectionRestrictions;
	}
	public String getTargetType() {
		return semanticConnectionRestrictions.get(semanticConnectionRestrictions.size()-1);
	}
	
	/**
	 * 
	 * @param typeStepRestrictions Add a List of Types that restrict the Path Where ALL is means that a Seap has no restriction
	 */
	public void setTypeStepRestrictions(final List<String> typeStepRestrictions) {
		this.typeStepRestrictions = typeStepRestrictions;
	}
	
	/**
	 * 
	 * @param typeStepRestriction add Element to the Steprestriction Path
	 */
	public void addTypeStepRestriction(final String typeStepRestriction) {
		if (this.typeStepRestrictions == null) {
			this.typeStepRestrictions = new ArrayList<String>(3);
		}
		this.typeStepRestrictions.add(typeStepRestriction);
	}
	/**
	 * 
	 * @param semanticConnectionRestrictions List of Conection Type Restictions if a Field is Empty it wont be restricted 
	 */
	public void setSemanticConnectionRestrictions(final List<String> semanticConnectionRestrictions) {
		this.semanticConnectionRestrictions = semanticConnectionRestrictions;
	}
	
	
}
