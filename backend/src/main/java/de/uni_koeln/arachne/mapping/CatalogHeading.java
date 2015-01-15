package de.uni_koeln.arachne.mapping;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@XmlRootElement
@Entity
@Table(name="catalog_heading")
@SuppressWarnings("PMD")
public class CatalogHeading {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name="catalog_id", nullable=false, insertable=true, updatable=true)
	private Catalog catalog;
	
	@ManyToOne
	@JoinColumn(name="parent_id", nullable=true, insertable=true, updatable=true)
	private CatalogHeading parent;
	
	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<CatalogHeading> children;
	
	@OneToMany(mappedBy="catalogHeading", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<CatalogEntry> catalogEntries;

	@Column(name="arachne_entity_id")
	private Long arachneEntityId;
	
	@Column(name="label")
	private String label;
	
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
	 * @return the parent
	 */
	@JsonIgnore
	@XmlTransient
	public CatalogHeading getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(CatalogHeading parent) {
		this.parent = parent;
	}

	/**
	 * @return the catalogEntries
	 */
	public Set<CatalogEntry> getCatalogEntries() {
		return catalogEntries;
	}

	/**
	 * @param catalogEntries the catalogEntries to set
	 */
	public void setCatalogEntries(Set<CatalogEntry> catalogEntries) {
		this.catalogEntries = catalogEntries;
	}

	/**
	 * @return the children
	 */
	public Set<CatalogHeading> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(Set<CatalogHeading> children) {
		this.children = children;
	}
	
}
