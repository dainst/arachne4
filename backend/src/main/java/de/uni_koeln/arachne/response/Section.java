package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for organizing and holding content of either type <code>Field</code> or <code>Section</code>.
 */
public class Section extends Content {
	/**
	 * Parameterless constructor.
	 */
	public Section() {
		content = new ArrayList<Content>();
	}
	
	/**
	 * The label used by the frontend.
	 */
	private String label;

	/**
	 * A list of content (either <code>Field</code> or <code>Section</code>).
	 */
	private List<Content> content;
	
	/**
	 * Convenient function that adds a content object to the list of <code>Content</code>.
	 * @param content the <code>Content</code> object to be added.
	 * @return a <code>boolean</code> indicating success.
	 */
	public boolean add(Content content) {
		return this.content.add(content);
	}
	
	public List<Content> getContent() {
		return content;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
