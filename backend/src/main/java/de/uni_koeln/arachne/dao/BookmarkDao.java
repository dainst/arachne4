package de.uni_koeln.arachne.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.Bookmark;

@Repository("BookmarkDao")
public class BookmarkDao {
	
	@Autowired
    private transient SessionFactory sessionFactory;
	
	@Transactional(readOnly=true)
	public Bookmark getByBookmarkId(final long bookmarkId) {
		Session session = sessionFactory.getCurrentSession();
		return (Bookmark) session.get(Bookmark.class, bookmarkId);
	}
	
	@Transactional
	public Bookmark updateBookmark(final Bookmark bookmark) {
		Session session = sessionFactory.getCurrentSession();
		session.update(bookmark);
		return bookmark;
	}
	
	@Transactional
	public Bookmark saveBookmark(final Bookmark bookmark) {
		Session session = sessionFactory.getCurrentSession();
		session.save(bookmark);
		return bookmark;
	}
	
	@Transactional
	public void deleteBookmark(final Bookmark bookmark) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(bookmark);
	}

}
