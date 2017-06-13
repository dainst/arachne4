package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;

public class Facet {
	private String name;
	
	private String labelKey;


	private String group;
	
	private List<String> values = new ArrayList<String>();
	
	public Facet(final String name, final String labelKey) {
		this.name = name;
		this.labelKey = labelKey;
	}

	public Facet(final String name, final String labelKey, final String group) {
		this.name = name;
		this.labelKey = labelKey;
		if (group != null) {
			this.group = group;
		}
	}
	
	public Facet() {
		// Emtpy no-args constructor to make JAXB happy
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getLabelKey() {
		return labelKey;
	}

	public void setLabelKey(final String labelKey) {
		this.labelKey = labelKey;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(final List<String> values) {
		this.values = values;
	}

	public void addValues(final List<String> values) {
		this.values.addAll(values);		
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
}
