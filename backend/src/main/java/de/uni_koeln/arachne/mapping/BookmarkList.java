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

@XmlRootElement
@Entity
@Table(name="bookmark_list")
@SuppressWarnings("PMD")
public class BookmarkList {

	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="uid", nullable=false, insertable=true, updatable=false)
	private UserAdministration user;
	
	@Column(name="name")
	private String name;

	@OneToMany(mappedBy="bookmarkList", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<Bookmark> bookmarks;
	
	@Column(name="commentary")
	private String commentary;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setBookmarkListId(final Long id) {
		this.id = id;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the user
	 * Not serialized, issues with recursion
	 */
	@JsonIgnore
	@XmlTransient
	public UserAdministration getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(final UserAdministration user) {
		this.user = user;
	}

	/**
	 * @return the bookmarks
	 */
	public Set<Bookmark> getBookmarks() {
		return bookmarks;
	}

	/**
	 * @param bookmarks the bookmarks to set
	 */
	public void setBookmarks(final Set<Bookmark> bookmarks) {
		this.bookmarks = bookmarks;
	}

	/**
	 * @return the commentary
	 */
	public String getCommentary() {
		return commentary;
	}

	/**
	 * @param commentary the commentary to set
	 */
	public void setCommentary(final String commentary) {
		this.commentary = commentary;
	}


	
}
