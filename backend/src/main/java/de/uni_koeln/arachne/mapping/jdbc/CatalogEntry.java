package de.uni_koeln.arachne.mapping.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class CatalogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

	private Long id;

	private Catalog catalog;
	
	private CatalogEntry parent;
	
	private List<CatalogEntry> children;

	private long arachneEntityId;
	
	private String label;
	
	private String text;
	
	private String path;
	
	private Long parentId;
	
	private Integer indexParent;
	
	private long catalogId;

	private boolean hasChildren;

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
	public long getArachneEntityId() {
		return arachneEntityId;
	}

	/**
	 * @param arachneEntityId the arachneEntityId to set
	 */
	public void setArachneEntityId(final long arachneEntityId) {
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
	public long getCatalogId() {
		return catalogId;
	}

	/**
	 * @param catalogId the catalogId to set
	 */
	public void setCatalogId(long catalogId) {
		this.catalogId = catalogId;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	
	public boolean isHasChildren() {
		if (children != null) {
			return !children.isEmpty();
		} else {
			return this.hasChildren;
		}
	}
}
