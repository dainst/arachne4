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
	
	protected transient final List<AbstractContent> content = new ArrayList<AbstractContent>();

	/**
	 * Convenient function that adds a content object to the list of <code>Content</code>.
	 * @param content the <code>Content</code> object to be added.
	 * @return a <code>boolean</code> indicating success.
	 */
	public boolean add(final AbstractContent content) {
		return this.content.add(content);
	}
	
	/**
	 * Getter for context entities.
	 * @return The list of context entities. 
	 */
	@XmlElementWrapper
	public List<AbstractContent> getContent() {
		return this.content;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((content == null) ? 0 : content.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ContextEntity other = (ContextEntity) obj;
		if (content == null) {
			if (other.content != null) {
				return false;
			}
		} else {
			if (!content.equals(other.content)) {
				return false;
			}
		}
		return true;
	}

}
