package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

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
		return this.content.add(content);
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
}