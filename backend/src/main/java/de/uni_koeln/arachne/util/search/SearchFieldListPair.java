package de.uni_koeln.arachne.util.search;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Class holding two immutable string lists. Used as a function return value.
 * @author Reimar Grabowski
 *
 */
public class SearchFieldListPair {

	private final ImmutableList<String> list;
	
	private final ImmutableList<String> listWithBoostValues;
	
	public SearchFieldListPair(final List<String> list, final List<String> listWithBoostValues) {
		this.list = ImmutableList.copyOf(list);
		this.listWithBoostValues = ImmutableList.copyOf(listWithBoostValues);
	}

	public ImmutableList<String> getList() {
		return list;
	}
	
	public ImmutableList<String> getListWithBoosts() {
		return listWithBoostValues;
	}
}
