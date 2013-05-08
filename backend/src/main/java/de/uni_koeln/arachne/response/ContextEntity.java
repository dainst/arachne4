package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A class for organizing and holding context-content of type <code>Field</code> 
 */
@XmlRootElement
public class ContextEntity extends AbstractContent {
	
	protected transient final List<AbstractContent> contextEntity = new ArrayList<AbstractContent>();

	/**
	 * Convenient function that adds a content object to the list of <code>Content</code>.
	 * @param content the <code>Content</code> object to be added.
	 * @return a <code>boolean</code> indicating success.
	 */
	public boolean add(final AbstractContent content) {
		return this.contextEntity.add(content);
	}
	
	@XmlElementWrapper
	public List<AbstractContent> getContextEntity() {
		return this.contextEntity;
	}
}
