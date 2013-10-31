package de.uni_koeln.arachne.mapping;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name="bookmark_list")
public class BookmarkList {

	@Id
	@Column(name="id")
	private Long id;
	
	@Column(name="uid")
	private Long uid;
	
	@Column(name="name")
	private String name;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="bookmark_list_id", insertable=true, updatable=false)
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
	 * @return the uid
	 */
	public Long getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(final Long uid) {
		this.uid = uid;
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
