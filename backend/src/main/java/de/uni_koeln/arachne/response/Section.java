package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A class for organizing and holding content of either type <code>Field</code> or <code>Section</code>.
 */
@XmlRootElement
public class Section extends AbstractContent {
	/**
	 * The label used by the frontend.
	 */
	protected String label;
	
	protected String separator;

	/**
	 * A list of content (either <code>Field</code> or <code>Section</code>).
	 */
	protected transient final List<AbstractContent> content = new ArrayList<AbstractContent>();
	
	/**
	 * Convenient function that adds a content object to the list of <code>Content</code>.
	 * @param content the <code>Content</code> object to be added.
	 * @return a <code>boolean</code> indicating success.
	 */
	public boolean add(final AbstractContent content) {
		if (this.content.contains(content)) {
			return false;
		} else {
			return this.content.add(content);
		}
	}
	
	@XmlElementWrapper
	public List<AbstractContent> getContent() {
		return this.content;
	}
	
	public String getLabel() {
		return this.label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}
	
	@JsonIgnore
	@XmlTransient
	public String getSeparator() {
		return this.separator;
	}
	
	public void setSeparator(final String separator) {
		this.separator = separator;
	}
	
	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		
		final Iterator<AbstractContent> iterator = content.iterator();
		while (iterator.hasNext()) {
			final AbstractContent currentContent = (AbstractContent) iterator.next();
			stringBuilder.append(currentContent.toString());
			if (iterator.hasNext()) {
				stringBuilder.append(separator);
			}
		}
		
		if (label == null) {
			return stringBuilder.toString();
		}
		return label + ": " + stringBuilder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result
				+ ((separator == null) ? 0 : separator.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Section other = (Section) obj;
		if (content == null) {
			if (other.content != null) {
				return false;
			}
		} else {
			if (!content.equals(other.content)) {
				return false;
			}
		}
			
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else {
			if (!label.equals(other.label)) {
				return false;
			}
		}
			
		if (separator == null) {
			if (other.separator != null) {
				return false;
			}
		} else {
			if (!separator.equals(other.separator)) {
				return false;
			}
		}
		
		return true;
	}
	
}