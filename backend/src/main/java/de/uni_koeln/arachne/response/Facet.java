package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;

public class Facet {
	private String name;
	
	private String labelKey;
	
	private List<String> values = new ArrayList<String>();
	
	public Facet(String aName, String alabelKey) {
		name = aName;
		labelKey = alabelKey;
	}

	public String getName() {
		return name;
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
}
