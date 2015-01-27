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
	@JoinColumn(name="parent_id", nullable=true, insertable=true, updatable=true)
	private CatalogEntry parent;
	
	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<CatalogEntry> children;

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

		if (this.children != null){
			for (CatalogEntry child : this.getChildren()){
				child.setParent(this);
				child.setCatalog(catalog);
			}	
		}
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
	public CatalogEntry getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(CatalogEntry parent) {
		this.parent = parent;
	}	

	/**
	 * @return the children
	 */
	public Set<CatalogEntry> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(Set<CatalogEntry> children) {
		this.children = children;
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
	 * Recursively generates path attribute for the CatalogEntry and all its descendants
	 */
	public void generatePath(){
		String seperator = "/";
		 if (this.parent != null){
			 this.path = this.parent.getPath() + seperator + this.getId();
		 }
		 else {
			 this.path = this.getCatalog().getId() + seperator + this.getId();
		 }
		 if (this.children != null){
				for (CatalogEntry child : this.getChildren()){
					child.generatePath();
				}	
			}
	}
	
}
