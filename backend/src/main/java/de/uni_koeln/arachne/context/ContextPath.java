package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

public class ContextPath {
	/**
	 * The simple types all things on the way to the target must have 
	 */
	protected List<String> typeStepRestrictions=null;
	
	/**
	 * The restriction of the connection types the path must have. An empty string means that there are no restrictions
	 */
	protected List<String> semanticConnectionRestrictions= null;
	
	/**
	 * TODO Implement an mechanism that can restict values of fields in the additional JSON fields
	 */
	protected List<String> fieldValueRestrictions = null;
	
	
	/**
	 * 
	 * @return Get List of Strings that contain the Step restriction
	 */
	public List<String> getTypeStepRestrictions() {
		return typeStepRestrictions;
	}
	
	public List<String> getSemanticConnectionRestrictions() {
		return semanticConnectionRestrictions;
	}
	
	public String getTargetType() {
		return typeStepRestrictions.get(typeStepRestrictions.size()-1);
	}
	
	/**
	 * 
	 * @param typeStepRestrictions Add a List of Types that restrict the Path Where ALL is means that a Step has no restriction
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
