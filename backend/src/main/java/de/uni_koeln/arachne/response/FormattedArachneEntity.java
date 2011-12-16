package de.uni_koeln.arachne.response;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Response object class that returns preformatted output to the frontend.
 * This class is serialized to JSON using <codeJackson</code>.
 */
@XmlRootElement(name="entity")
@XmlSeeAlso({Section.class,Field.class})
public class FormattedArachneEntity extends BaseArachneEntity {
	
	/**
	 * The Title of the Dataset
	 */
	protected String title;
	
	/**
	 * The Subtitle of the Dataset
	 */
	protected String subtitle;
	
	/**
	 * Hierachical structured information of the dataset.
	 */
	protected Content sections;
	
	/**
	 * The date of the last Modification of the dataset.
	 */
	protected Date lastModified;
	
	/**
	 * The context map Contains the Contexts of the dataset.
	 */
	protected Content context;
	
	/**
	 * The Images that are asociated with the dataset
	 */
	//protected List<ArachneImage> images;
	protected List<Image> images;
	
	/**
	 * 
	 */
	protected Map<String, String> facets;
	
	/**
	 * Parameterless constructor initializing title and subtitle.
	 */
	public FormattedArachneEntity() {
		title = "";
		subtitle = "";
		facets = new HashMap<String, String>(); 
	}	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	
	public Content getSections() {
		return sections;
	}

	public void setSections(Content content) {
		sections = content;
	}
	
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Content getContext() {
		return context;
	}

	public void setContext(Content context) {
		this.context = context;
	}
	
	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}
	/*public List<ArachneImage> getImages() {
		return images;
	}

	public void setImages(List<ArachneImage> images) {
		this.images = images;
	}*/
	
	public Map<String, String> getFacets() {
		return this.facets;
	}
	
	public void setFacets(Map<String, String> facets) {
		this.facets = facets;
	}
	
	// Convenience function to add a facet
	public void addFacet(String key, String value) {
		this.facets.put(key, value);
	}
}