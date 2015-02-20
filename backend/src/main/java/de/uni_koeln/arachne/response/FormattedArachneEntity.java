package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import de.uni_koeln.arachne.response.link.ExternalLink;

/**
 * Response object class that returns preformatted output to the frontend.
 * This class is serialized to JSON using <code>Jackson</code>.
 * @author Reimar Grabowski
 */
@XmlRootElement(name="entity")
@XmlSeeAlso({Section.class,Field.class,FieldList.class})
@JsonInclude(Include.NON_NULL)
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
	protected List<AbstractContent> sections;
	
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
	 * The places this entity is connected to.
	 */
	protected List<Place> places = new ArrayList<Place>();
	
	/**
	 * A list of catalogs this entity is part of.
	 */
	protected List<Long> catalogIds;
	
	/**
	 * The number of fields this entitiy has
	 */
	protected int fields; 
			
	/**
	 * The document boost
	 */
	protected double boost = 1;
	
	/**
	 * List of entities that are connected to this entity.
	 */
	protected List<Long> connectedEntities;
	
	/**
	 * The number of connections this entity has
	 */
	protected double degree = 0;
	
	/**
	 * Links to external resources (like browsers, viewers ...)
	 */
	private List<ExternalLink> externalLinks;
	
	/**
	 * Parameterless constructor initializing title and subtitle.
	 */
	public FormattedArachneEntity() {
		title = "";
		subtitle = "";
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
	
	public List<AbstractContent> getSections() {
		return sections;
	}

	public void setSections(final List<AbstractContent> content) {
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
	
	public void setImages(final List<Image> images) {
		this.images = images;
	}
	
	public void setThumbnailId(final Long thumbnailId) {
		this.thumbnailId = thumbnailId;
	}
	
	public double getDegree() {
		return degree;
	}
	
	public void setDegree(final double degree) {
		this.degree = degree;		
	}

	public int getFields() {
		return fields;
	}
	
	public void setFields(final int fields) {
		this.fields = fields;
	}
	
	public double getBoost() {
		return boost;
	}
	
	public void setBoost(final double boost) {
		this.boost = boost;		
	}
	
	public List<Long> getConnectedEntities() {
		return connectedEntities;
	}
	
	public void setConnectedEntities(final List<Long> connectedEntities) {
		this.connectedEntities = connectedEntities;
	}

	public List<ExternalLink> getExternalLinks() {
		return externalLinks;
	}

	public void setExternalLinks(List<ExternalLink> externalLinks) {
		this.externalLinks = externalLinks;
	}
	
	public List<Place> getPlaces() {
		return places;
	}
	
	/**
	 * Adds a place to the places list.
	 * @param place The place to add.
	 */
	public void addPlace(final Place place) {
		places.add(place);
	}

	/**
	 * @return the catalogIds
	 */
	public List<Long> getCatalogIds() {
		return catalogIds;
	}

	/**
	 * @param catalogIds the catalogIds to set
	 */
	public void setCatalogIds(List<Long> catalogIds) {
		this.catalogIds = catalogIds;
	}
}
