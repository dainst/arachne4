package de.uni_koeln.arachne.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name="bookmark")
public class Bookmark {

	@Id
	@Column(name="id")
	private Long id;
	
	@Column(name="bookmark_list_id", insertable=true, updatable=false)
	private Long bookmarkListId;

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
	 * @return the bookmarkListId
	 */
	public Long getBookmarkListId() {
		return bookmarkListId;
	}

	/**
	 * @param bookmarkListId the bookmarkListId to set
	 */
	public void setBookmarkListId(final Long bookmarkListId) {
		this.bookmarkListId = bookmarkListId;
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
