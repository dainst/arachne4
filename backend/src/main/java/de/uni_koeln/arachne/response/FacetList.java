package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class to easier create <code>List<Facet></code> objects.
 */
public class FacetList {
	/**
	 * Data of the class.
	 */
	private List<Facet> data = new ArrayList<Facet>();
	
	/**
	 * Adds a facet to the list. If a facet of the same name already exists it adds the values to this facet.
	 * @param facet
	 */
	public void add(Facet facet) {
		System.out.println("Add Facet: " + facet.getName());
		if (getFacetByName(facet.getName()) > -1) {
			// TODO only add values
			System.out.println("Facet found: " + getFacetByName(facet.getName()));
		} else {
			System.out.println("Not in List");
			data.add(facet);
		}
	}
	
	/**
	 * Convenience function.
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	public List<Facet> getList() {
		return data;
	}
	
	/**
	 * Returns the index of a facet by name.
	 * <br>
	 * It does a linear search as facet lists are small.
	 * @param aName String The name of the facet to find.
	 * @return int The index of the found <code>Facet</code> or <code>-1</code> if no facet was found.
	 */
	private int getFacetByName(String aName) {
		if (data.isEmpty()) {
			return -1;
		}
		
		int result = -1;
		int i = 0;
		while (i < data.size()) {
			if (data.get(i).getName() == aName) {
				result = i;
				break;
			}
			i++;
		}
				
		return result;
	}
}