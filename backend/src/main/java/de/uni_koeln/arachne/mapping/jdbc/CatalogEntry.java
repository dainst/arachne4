package de.uni_koeln.arachne.mapping.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class CatalogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

	private Long id = null;

	private List<CatalogEntry> children;

	private Long arachneEntityId = null;
	
	private String label = null;
	
	private String text = null;
	
	private String path = null;
	
	private Long parentId = null;
	
	private int indexParent = 0;
	
	private Long catalogId = null;

	private int totalChildren;

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
	public int getIndexParent() {
		return indexParent;
	}

	/**
	 * @param indexParent the indexParent to set
	 */
	public void setIndexParent(int indexParent) {
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

	/**
	 * @param totalChildren the total number of children
     */
	public void setTotalChildren(int totalChildren) {
		this.totalChildren = totalChildren;
	}

	/**
	 * @return the total number of children
     */
	public int getTotalChildren() {
		if (children != null) {
			return children.size();
		} else {
			return this.totalChildren;
		}
	}

	/**
	 * @return true if entry has children, false otherwise
     */
	public boolean hasChildren() {
		if (children == null) return this.totalChildren > 0;
		else return children.size() > 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arachneEntityId == null) ? 0 : arachneEntityId.hashCode());
		result = prime * result + ((catalogId == null) ? 0 : catalogId.hashCode());
		result = prime * result + totalChildren;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + indexParent;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CatalogEntry other = (CatalogEntry) obj;
		if (arachneEntityId == null) {
			if (other.arachneEntityId != null)
				return false;
		} else if (!arachneEntityId.equals(other.arachneEntityId))
			return false;
		if (catalogId == null) {
			if (other.catalogId != null)
				return false;
		} else if (!catalogId.equals(other.catalogId))
			return false;
		if (totalChildren != other.totalChildren)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (indexParent != other.indexParent)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (parentId == null) {
			if (other.parentId != null)
				return false;
		} else if (!parentId.equals(other.parentId))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}