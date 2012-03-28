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
	
	private transient List<String> value = null;
	
	@XmlElementWrapper
	public List<String> getValue() {
		return this.value;
	}
	
	public void add(final String value) {
		this.value.add(value);
	}
	
	public String get(final int index) {
		return this.value.get(index);
	}
	
	public void modify(final int index, final String value) {
		this.value.set(index, value);
	}
	
	public int size() {
		return this.value.size();
	}
}
