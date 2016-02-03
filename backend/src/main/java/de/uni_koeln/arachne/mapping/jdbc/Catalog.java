package de.uni_koeln.arachne.mapping.jdbc;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class Catalog {

	private Long id = null;
	
	private Set<Long> userIds;
	
	private CatalogEntry root;
	
	private String author;
	
	private Boolean isPublic;

	private String datasetGroup;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<Long> getUserIds() {
		return userIds;
	}
	
	@JsonIgnore
	public void setUserIds(final Set<Long> userIds) {
		this.userIds = userIds;
	}

	public CatalogEntry getRoot() {
		return root;
	}

	public void setRoot(CatalogEntry root) {
		this.root = root;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Boolean isPublic() {
		return isPublic;
	}

	public void setPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public String getDatasetGroup() {
		return datasetGroup;
	}

	public void setDatasetGroup(String datasetGroup) {
		this.datasetGroup = datasetGroup;
	}
	
	public boolean isCatalogOfUserWithId(long userId) {
		return userIds.contains(userId);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((datasetGroup == null) ? 0 : datasetGroup.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((isPublic == null) ? 0 : isPublic.hashCode());
		result = prime * result + ((root == null) ? 0 : root.hashCode());
		result = prime * result + ((userIds == null) ? 0 : userIds.hashCode());
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
		Catalog other = (Catalog) obj;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (datasetGroup == null) {
			if (other.datasetGroup != null)
				return false;
		} else if (!datasetGroup.equals(other.datasetGroup))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isPublic == null) {
			if (other.isPublic != null)
				return false;
		} else if (!isPublic.equals(other.isPublic))
			return false;
		if (root == null) {
			if (other.root != null)
				return false;
		} else if (!root.equals(other.root))
			return false;
		if (userIds == null) {
			if (other.userIds != null)
				return false;
		} else if (!userIds.equals(other.userIds))
			return false;
		return true;
	}
}
