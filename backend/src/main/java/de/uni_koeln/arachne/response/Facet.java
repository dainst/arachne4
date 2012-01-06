package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;

public class Facet {
	private String name;
	
	private String labelKey;
	
	private List<String> values = new ArrayList<String>();
	
	public Facet() {
	}
	
	public Facet(String aName, String alabelKey) {
		this.name = aName;
		this.labelKey = alabelKey;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabelKey() {
		return labelKey;
	}

	public void setLabelKey(String labelKey) {
		this.labelKey = labelKey;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public void addValues(List<String> values) {
		this.values.addAll(values);		
	}
}
