package de.uni_koeln.arachne.mapping.jdbc;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Mapping class for catalogs.
 * @author Reimar Grabowski
 *
 */
@JsonInclude(Include.NON_EMPTY)
public class Catalog {

	private Long id = null;
	
	private Set<Long> userIds;
	
	private CatalogEntry root;
	
	private String author;

	private Boolean isPublic;

	private String datasetGroup = "Arachne";

	private String projectId;

	/**
	 * Getter for the id.
	 * @return The catalog id.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Setter for the id.
	 * @param id A catalog id.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Getter for the users ids that are allowed to see/modify this catalog.
	 * @return The set of user ids.
	 */
	public Set<Long> getUserIds() {
		return userIds;
	}
	
	/**
	 * Setter for the users ids that are allowed to see/modify this catalog.
	 * @param userIds A set of user ids.
	 */
	public void setUserIds(final Set<Long> userIds) {
		this.userIds = userIds;
	}

	/**
	 * Getter for the root catalog entry.
	 * @return The root entry.
	 */
	public CatalogEntry getRoot() {
		return root;
	}

	/**
	 * Setter fot the root catalog entry.
	 * @param root A catalog entry.
	 */
	public void setRoot(CatalogEntry root) {
		this.root = root;
	}

	/**
	 * Getter for the projectId field.
	 * @return The projectId.
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * Setter for the projectId field.
	 * @param projectId An projectId.
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

    /**
     * Getter for the author field.
     * @return The author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Setter for the author field.
     * @param author An author.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

	/**
	 * If this catalog is publicly readable.
	 * @return Whether this catalog is public.
	 */
	public Boolean isPublic() {
		return isPublic;
	}

	/**
	 * Sets the catalog to public or private.
	 * @param isPublic Whether htis catalog is public.
	 */
	public void setPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	/**
	 * Getter for the dataset group of this catalog.
	 * @return The dataset group.
	 */
	public String getDatasetGroup() {
		return datasetGroup;
	}

	/**
	 * Setter for the dataset group.
	 * @param datasetGroup A dataset group.
	 */
	public void setDatasetGroup(String datasetGroup) {
		this.datasetGroup = datasetGroup;
	}
	
	/**
	 * Tests if this catalog is accessible by the given user id.
	 * @param userId The user id to test.
	 * @return <code>true</code> if the user id is in the set of user ids, <code>false</code> otherwise.
	 */
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
