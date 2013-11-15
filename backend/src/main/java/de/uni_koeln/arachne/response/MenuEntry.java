package de.uni_koeln.arachne.response;

import java.util.List;

public class MenuEntry {
	
	private String id;  
	private String parent;
	private String title;
	private String path;
	private List<String> children;
	
	public String getId() {
		return id;
	}
	public void setId(final String id) { 
		this.id = id;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(final String parent) {
		this.parent = parent;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(final String title) {
		this.title = title;
	}
	public String getPath() {
		return path;
	}
	public void setPath(final String path) {
		this.path = path;
	}
	public List<String> getChildren() {
		return children;
	}
	public void setChildren(final List<String> children) {
		this.children = children;
	}

}
