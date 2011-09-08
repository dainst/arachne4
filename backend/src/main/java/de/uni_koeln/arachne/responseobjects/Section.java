package de.uni_koeln.arachne.responseobjects;

import java.util.List;

/**
 * A class for organizing and holding content of either <code>Field</code> or <code>Section</code>.
 */
public class Section extends Content {
	/**
	 * A list of content (either Field or Section).
	 */
	private List<Content> content;
	
	public boolean add(Content c) {
		return content.add(c);
	}
}
