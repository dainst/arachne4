package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Response object class that returns preformatted output to the frontend.
 * This class is serialized to JSON using <code>Jackson</code>.
 */
@XmlRootElement(name="entity")
@XmlSeeAlso({Section.class,Field.class,FieldList.class})
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
	protected AbstractContent sections;
	
	/**
	 * The date of the last Modification of the dataset.
	 */
	protected Date lastModified;
	
	/**
	 * The context map Contains the Contexts of the dataset.
	 */
	protected AbstractContent context;
	
	/**
	 * The Images that are associated with the dataset
	 */
	protected List<Image> images;
	
	/**
	 * The image id of the thumbnail of the dataset
	 */
	protected Long thumbnailId; 
	
	/**
	 * The facets as defined in the xml file for the dataset.
	 */
	protected List<Facet> facets;
	
	/**
	 * Parameterless constructor initializing title and subtitle.
	 */
	public FormattedArachneEntity() {
		title = "";
		subtitle = "";
		facets = new ArrayList<Facet>(); 
	}	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(final String subtitle) {
		this.subtitle = subtitle;
	}
	
	public AbstractContent getSections() {
		return sections;
	}

	public void setSections(final AbstractContent content) {
		sections = content;
	}
	
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(final Date lastModified) {
		this.lastModified = lastModified;
	}

	public AbstractContent getContext() {
		return context;
	}

	public void setContext(final AbstractContent context) {
		this.context = context;
	}
	
	public List<Image> getImages() {
		return images;
	}
	
	public Long getThumbnailId() {
		return thumbnailId;
	}

	public List<Facet> getFacets() {
		return this.facets;
	}
	
	public void setImages(final List<Image> images) {
		this.images = images;
	}
	
	public void setThumbnailId(final Long thumbnailId) {
		this.thumbnailId = thumbnailId;
	}
	
	public void setFacets(final List<Facet> facets) {
		this.facets = facets;
	}
	
	// Convenience function to add a facet
	public void addFacet(final Facet facet) {
		this.facets.add(facet);
	}
}
