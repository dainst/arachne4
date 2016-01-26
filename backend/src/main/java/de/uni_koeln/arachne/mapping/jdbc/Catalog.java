package de.uni_koeln.arachne.mapping.jdbc;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import de.uni_koeln.arachne.mapping.hibernate.User;

@JsonInclude(Include.NON_EMPTY)
public class Catalog {

	private long id;
	
	private Set<User> users;
	
	private Set<CatalogEntry> catalogEntries;
	
	private CatalogEntry root;
	
	private String author;
	
	private Boolean isPublic;

	private String datasetGroup;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public Set<CatalogEntry> getCatalogEntries() {
		return catalogEntries;
	}

	public void setCatalogEntries(Set<CatalogEntry> catalogEntries) {
		this.catalogEntries = catalogEntries;
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
	
	public void addToCatalogEntries(CatalogEntry entry) {
		if (this.catalogEntries == null){
			this.catalogEntries = new HashSet<CatalogEntry>();
		}
		this.catalogEntries.add(entry);
	}

	public boolean isCatalogOfUserWithId(long userId) {
		Boolean ownsCatalog = false;
		for (User catalogUser: this.users) {
			if (catalogUser.getId() == userId){
				ownsCatalog = true;
				break;
			}
		}
		return ownsCatalog;
	}
}
