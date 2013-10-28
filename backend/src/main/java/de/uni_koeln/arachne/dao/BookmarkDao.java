package de.uni_koeln.arachne.dao;

import org.springframework.stereotype.Repository;
import de.uni_koeln.arachne.mapping.Bookmark;

@Repository("BookmarkDao")
public class BookmarkDao extends AbstractHibernateTemplateDao {
	
	public Bookmark getByBookmarkId(final Long bookmarkId) {
		return (Bookmark) hibernateTemplate.get(Bookmark.class, bookmarkId);
	}

}
