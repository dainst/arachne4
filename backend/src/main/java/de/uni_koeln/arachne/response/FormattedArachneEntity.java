package de.uni_koeln.arachne.response;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Response object class that returns preformatted output to the frontend.
 * This class is serialized to JSON using <code>Jackson</code>.
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
	 * The find spot this entity is connected to
	 */
	protected String findSpot = null;
	
	/**
	 * The geo coordinates of the find spot.
	 */
	protected String findSpotLocation = null;
	
	/**
	 * The depository this entity is connected to
	 */
	protected String depository = null;
	
	/**
	 * The geo coordinates of the depository.
	 */
	protected String depositoryLocation = null;
	
	/**
	 * The number of connections this entity has
	 */
	protected double degree = 1;

	/**
	 * The number of fields this entitiy has
	 */
	protected int fields; 
			
	/**
	 * The document boost
	 */
	protected double boost = 1;
	
	/**
	 * 
	 */
	protected List<Long> connectedEntities;
	
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
	
	public void setImages(final List<Image> images) {
		this.images = images;
	}
	
	public void setThumbnailId(final Long thumbnailId) {
		this.thumbnailId = thumbnailId;
	}

	public String getFindSpot() {
		return this.findSpot;
	}
	
	public void setFindSpot(final String findSpot) {
		this.findSpot = findSpot;		
	}

	public String getFindSpotLocation() {
		return this.findSpotLocation;
	}
	
	public void setFindSpotLocation(final String findSpotLocation) {
		this.findSpotLocation = findSpotLocation;
	}
	
	public String getDepository() {
		return this.depository;
	}
	
	public void setDepository(final String depository) {
		this.depository = depository;		
	}

	public String getDepositoryLocation() {
		return this.depositoryLocation;
	}
	
	public void setDepositoryLocation(final String depositoryLocation) {
		this.depositoryLocation = depositoryLocation;
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
}
