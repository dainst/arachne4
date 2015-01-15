package de.uni_koeln.arachne.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement
@Entity
@Table(name="catalog_entry")
@SuppressWarnings("PMD")
public class CatalogEntry {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name="catalog_id", nullable=false, insertable=true, updatable=true)
	private Catalog catalog;
	
	@ManyToOne
	@JoinColumn(name="heading_id", nullable=false, insertable=true, updatable=true)
	private CatalogHeading catalogHeading;

	@Column(name="arachne_entity_id")
	private Long arachneEntityId;
	
	@Column(name="label")
	private String label;
	
	@Column(name="text")
	private String text;
	
	@Column(name="path")
	private String path;
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(final Long id) {
		this.id = id;
	}
	
	/**
	 * @return the catalog
	 * Not serialized, issues with recursion
	 */
	@JsonIgnore
	@XmlTransient
	public Catalog getCatalog() {
		return catalog;
	}

	/**
	 * @param catalog the catalog to set
	 */
	public void setCatalog(final Catalog catalog) {
		this.catalog = catalog;
	}

	/**
	 * @return the arachneEntityId
	 */
	public Long getArachneEntityId() {
		return arachneEntityId;
	}

	/**
	 * @param arachneEntityId the arachneEntityId to set
	 */
	public void setArachneEntityId(final Long arachneEntityId) {
		this.arachneEntityId = arachneEntityId;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the heading
	 */
	@JsonIgnore
	@XmlTransient
	public CatalogHeading getHeading() {
		return catalogHeading;
	}

	/**
	 * @param heading the heading to set
	 */
	public void setHeading(CatalogHeading heading) {
		this.catalogHeading = heading;
	}
	
}
