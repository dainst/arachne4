package de.uni_koeln.arachne.mapping.hibernate;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@Entity
@Table(name="catalog")
@SuppressWarnings("PMD")
public class Catalog {

	@Id
	@GeneratedValue
	private Long id; // NOPMD
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name="catalog_benutzer",
		joinColumns={@JoinColumn(name="catalog_id")},
		inverseJoinColumns={@JoinColumn(name="uid")})
	private Set<User> users;
	
	@OneToMany(mappedBy="catalog", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<CatalogEntry> catalogEntries;
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "root_id")
	private CatalogEntry root;
	
	@Column(name="author")
	private String author;
	
	@Column(name="public")
	private Boolean isPublic;

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
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	public Boolean isPublic() {
		return isPublic;
	}

	public void setPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	/**
	 * @return the users
	 */
	@JsonIgnore
	@XmlTransient
	public Set<User> getUsers() {
		return users;
	}

	/**
	 * @param users the users to set
	 */
	public void setUsers(Set<User> users) {
		this.users = users;
	}
	
	/**
	 * @param userId Id of a user
	 * @return true if the user with the id is part of the group that owns the catalog
	 */
	public Boolean isCatalogOfUserWithId(long userId){
		Boolean ownsCatalog = false;
		for (User catalogUser : this.users){
			if (catalogUser.getId() == userId){
				ownsCatalog = true;
				break;
			}
		}
		return ownsCatalog;
	}

	/**
	 * @return the catalogEntries
	 */
	@JsonIgnore
	public Set<CatalogEntry> getCatalogEntries() {
		return catalogEntries;
	}

	/**
	 * @param catalogEntries the catalogEntries to set
	 */
	@JsonProperty("catalogEntries")
	public void setCatalogEntries(Set<CatalogEntry> catalogEntries) {
		this.catalogEntries = catalogEntries;
	}
	
	/**
	 * Add a new CatalogEntry to catalogEntries
	 * @param entry the CatalogEntry to add
	 */
	public void addToCatalogEntries(CatalogEntry entry){
		if (this.catalogEntries == null){
			this.catalogEntries = new HashSet<CatalogEntry>();
		}
		this.catalogEntries.add(entry);
	}

	/**
	 * @return the root
	 */
	public CatalogEntry getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(CatalogEntry root) {
		this.root = root;
	}

	
}
