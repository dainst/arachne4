package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A class for organizing and holding content of either type <code>Field</code> or <code>Section</code>.
 */
@XmlRootElement
public class Section extends AbstractContent {
	/**
	 * The label used by the frontend.
	 */
	private String label;

	/**
	 * A list of content (either <code>Field</code> or <code>Section</code>).
	 */
	private List<AbstractContent> content = new ArrayList<AbstractContent>();;
	
	/**
	 * Convenient function that adds a content object to the list of <code>Content</code>.
	 * @param content the <code>Content</code> object to be added.
	 * @return a <code>boolean</code> indicating success.
	 */
	public boolean add(AbstractContent content) {
		return this.content.add(content);
	}
	
	@XmlElementWrapper
	public List<AbstractContent> getContent() {
		return this.content;
	}
	
	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return this.label + ": " + this.content;
	}
}