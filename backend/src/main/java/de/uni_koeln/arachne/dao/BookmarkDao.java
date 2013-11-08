package de.uni_koeln.arachne.dao;

import org.springframework.stereotype.Repository;
import de.uni_koeln.arachne.mapping.Bookmark;

@Repository("BookmarkDao")
public class BookmarkDao extends AbstractHibernateTemplateDao {
	
	public Bookmark getByBookmarkId(final Long bookmarkId) {
		return (Bookmark) hibernateTemplate.get(Bookmark.class, bookmarkId);
	}
	
	public Bookmark updateBookmark(final Bookmark bookmark) {
		hibernateTemplate.update(bookmark);
		return bookmark;
	}
	
	public Bookmark saveBookmark(final Bookmark bookmark) {
		hibernateTemplate.save(bookmark);
		return bookmark;
	}
	
	public void destroyBookmark(final Bookmark bookmark) {
		hibernateTemplate.delete(bookmark);
	}

}
