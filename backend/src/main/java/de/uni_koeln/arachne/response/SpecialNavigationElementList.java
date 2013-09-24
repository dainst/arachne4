package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of class contain an arbitrary number of SpecialNavigationElement-instances which describe special 
 * behaviour within the frontend
 * @author Patrick Gunia
 *
 */

public class SpecialNavigationElementList {

	private List<SpecialNavigationElement> specialNavigationElements = new ArrayList<SpecialNavigationElement>();
		
	public List<SpecialNavigationElement> getSpecialNavigationElements() {
		return specialNavigationElements;
	}

	public void addElement(final SpecialNavigationElement element) {
		if(!specialNavigationElements.contains(element)) {
			specialNavigationElements.add(element);
		}
	}
	
	public int size() {
		return specialNavigationElements.size();
	}
}
