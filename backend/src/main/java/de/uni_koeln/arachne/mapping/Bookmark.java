package de.uni_koeln.arachne.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement
@Entity
@Table(name="bookmark")
public class Bookmark {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name="bookmark_list_id", nullable=false, insertable=true, updatable=true)
	private BookmarkList bookmarkList;

	@Column(name="arachne_entity_id")
	private Long arachneEntityId;
	
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
	public void setId(final Long id) {
		this.id = id;
	}
	
	/**
	 * @return the bookmarkList
	 * Not serialized, issues with recursion
	 */
	@JsonIgnore
	@XmlTransient
	public BookmarkList getBookmarkList() {
		return bookmarkList;
	}

	/**
	 * @param bookmarkList the bookmarkList to set
	 */
	public void setBookmarkList(final BookmarkList bookmarkList) {
		this.bookmarkList = bookmarkList;
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
	 * @return the commentary
	 */
	public String getCommentary() {
		return commentary;
	}

	/**
	 * @param commentary the commentary to setlong
	 */
	public void setCommentary(final String commentary) {
		this.commentary = commentary;
	}
	
}
