package de.uni_koeln.arachne.controller;

import java.util.Comparator;

import de.uni_koeln.arachne.response.search.SearchResultFacetValue;

public class FacetValueComparator implements Comparator<SearchResultFacetValue> {

	@Override
	public int compare(SearchResultFacetValue o1, SearchResultFacetValue o2) {
		return o1.getValue().compareTo(o2.getValue());
	}

}
