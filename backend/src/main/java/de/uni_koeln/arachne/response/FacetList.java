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
	private transient final List<Facet> data = new ArrayList<Facet>();
	
	/**
	 * Adds a facet to the list. If a facet of the same name already exists it adds the values to this facet.
	 * @param facet
	 */
	public void add(final Facet facet) {
		if (getFacetByName(facet.getName()) > -1) {
			data.get(getFacetByName(facet.getName())).addValues(facet.getValues());
		} else {
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
	 * @param name String The name of the facet to find.
	 * @return int The index of the found <code>Facet</code> or <code>-1</code> if no facet was found.
	 */
	private int getFacetByName(final String name) {
		if (data.isEmpty()) {
			return -1;
		}
		
		int result = -1;
		int index = 0;
		while (index < data.size()) {
			if (data.get(index).getName().equals(name)) {
				result = index;
				break;
			}
			index++;
		}
				
		return result;
	}
}