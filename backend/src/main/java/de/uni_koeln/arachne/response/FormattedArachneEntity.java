package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uni_koeln.arachne.response.link.ExternalLink;
import de.uni_koeln.arachne.response.search.Suggestion;

/**
 * Response object class that returns preformatted output to the frontend and the index.
 * This class is serialized to JSON using <code>Jackson</code>.
 * 
 * @author Reimar Grabowski
 */
@XmlRootElement(name="entity")
@XmlSeeAlso({Section.class,Field.class,FieldList.class})
@JsonInclude(Include.NON_EMPTY)
public class FormattedArachneEntity extends BaseArachneEntity {
	
	/**
	 * The Title of the Dataset
	 */
	protected String title = "";
	
	/**
	 * The Subtitle of the Dataset
	 */
	protected String subtitle = "";
	
	protected List<String> ids;
	
	protected String filename;
	
	@JsonProperty("@id")
	protected String uri;
	
	/**
	 * Hierachical structured information of the dataset.
	 */
	protected List<AbstractContent> sections;
	
	/**
	 * Section for editor fields. Only shown if the GID of the user is >600.
	 */
	protected Section editorSection;
	
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
	 * The dates this entity is connected to.
	 */
	protected List<DateAssertion> dates = new ArrayList<DateAssertion>();
	
	/**
	 * A list of catalogs this entity is part of.
	 */
	protected Set<Long> catalogIds;
	
	/**
	 * A list of catalogEntry paths.
	 */
	protected List<String> catalogPaths;
	
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
	 * Elasticsearch suggestion terms
	 */
	private Suggestion suggest = new Suggestion();
	
	/**
	 * Passes construction to the {@link BaseArachneEntity} constructor.
	 * @param type The type of the entity.
	 */
	public FormattedArachneEntity(final String type) {
		super(type);
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
	
	public String getUri() {
		return uri;
	}

	public void setUri(final String uri) {
		this.uri = uri;
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

	public void setExternalLinks(final List<ExternalLink> externalLinks) {
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
	
	public List<DateAssertion> getDates() {
		return dates;
	}
	
	/**
	 * Adds a date to the dates list.
	 * @param date The date to add.
	 */
	public void addDate(final DateAssertion date) {
		dates.add(date);
	}

	/**
	 * @return the catalogIds
	 */
	public Set<Long> getCatalogIds() {
		return catalogIds;
	}

	/**
	 * @param catalogIds the catalogIds to set
	 */
	public void setCatalogIds(final Set<Long> catalogIds) {
		this.catalogIds = catalogIds;
	}
	
	/**
	 * @return the catalogPaths
	 */
	public List<String> getCatalogPaths() {
		return catalogPaths;
	}

	/**
	 * Stter for ctalogPaths.
	 * @param catalogPaths A list catalog paths.
	 */
	public void setCatalogPaths(final List<String> catalogPaths) {
		this.catalogPaths = catalogPaths;
	}

	/**
	 * @return the editorSection
	 */
	public Section getEditorSection() {
		return editorSection;
	}

	/**
	 * @param editorSection the editorSection to set
	 */
	public void setEditorSection(final Section editorSection) {
		this.editorSection = editorSection;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(final List<String> ids) {
		this.ids = ids;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public Suggestion getSuggest() {
		return suggest;
	}

	public void setSuggest(final Suggestion suggest) {
		this.suggest = suggest;
	}
}
