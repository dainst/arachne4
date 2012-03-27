package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FieldList extends AbstractContent {
	public FieldList() {
		value = new ArrayList<String>();
	}
	
	private List<String> value = null;
	
	@XmlElementWrapper
	public List<String> getValue() {
		return this.value;
	}
	
	public void add(String value) {
		this.value.add(value);
	}
	
	public String get(int index) {
		return this.value.get(index);
	}
	
	public void modify(int index, String value) {
		this.value.set(index, value);
	}
	
	public int size() {
		return this.value.size();
	}
}
