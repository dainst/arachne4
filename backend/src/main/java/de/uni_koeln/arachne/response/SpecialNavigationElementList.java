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

	private transient final List<AbstractSpecialNavigationElement> specialNavigationElements = new ArrayList<AbstractSpecialNavigationElement>();
		
	public List<AbstractSpecialNavigationElement> getSpecialNavigationElements() {
		return specialNavigationElements;
	}

	public void addElement(final AbstractSpecialNavigationElement element) {
		if(!specialNavigationElements.contains(element)) {
			specialNavigationElements.add(element);
		}
	}
	
	public int size() {
		return specialNavigationElements.size();
	}
}
