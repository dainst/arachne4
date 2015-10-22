package de.uni_koeln.arachne.mapping.hibernate;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@XmlRootElement
@Entity
@Table(name="catalog_entry")
@JsonInclude(Include.NON_EMPTY)
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
	
	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@OrderColumn(name="index_parent")
	@JsonInclude(Include.NON_NULL)
	private List<CatalogEntry> children;

	@Column(name="arachne_entity_id")
	private Long arachneEntityId;
	
	@Column(name="label")
	private String label;
	
	@Column(name="text")
	private String text;
	
	@Column(name="path")
	private String path;
	
	@Column(name="parent_id", nullable=true, insertable=false, updatable=false)
	private Long parentId;
	
	@Column(name="index_parent", nullable=true, insertable=false, updatable=false)
	private Integer indexParent;
	
	@Column(name="catalog_id", nullable=false, insertable=false, updatable=false)
	private Long catalogId;
	
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
				if (child != null){
					child.setParent(this);
					catalog.addToCatalogEntries(child);
					child.setCatalog(catalog);
				}
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
	public List<CatalogEntry> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(List<CatalogEntry> children) {
		this.children = children;
	}
	
	/**
	 * Adds CatalogEntry to list of children
	 * @param child the child to add
	 */
	public void addToChildren(CatalogEntry child){
		if (this.children == null){
			this.children = new ArrayList<CatalogEntry>();
		}
		this.children.add(child);
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
					if (child != null){
						child.generatePath();
					}
				}	
		 }
	}
	
	/**
	 * Recursively removes this entry and all its descendants from the catalog
	 */
	public void removeFromCatalog(){
		
		if (this.children != null){
			for (CatalogEntry child : this.getChildren()){
				child.removeFromCatalog();
			}	
		}
		this.catalog.getCatalogEntries().remove(this);
	}

	/**
	 * @return the parentId
	 */
	public Long getParentId() {
		return parentId;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return the indexParent
	 */
	public Integer getIndexParent() {
		return indexParent;
	}

	/**
	 * @param indexParent the indexParent to set
	 */
	public void setIndexParent(Integer indexParent) {
		this.indexParent = indexParent;
	}

	/**
	 * @return the catalogId
	 */
	public Long getCatalogId() {
		return catalogId;
	}

	/**
	 * @param catalogId the catalogId to set
	 */
	public void setCatalogId(Long catalogId) {
		this.catalogId = catalogId;
	}
	
}
