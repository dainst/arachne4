package de.uni_koeln.arachne.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.BookmarkList;

@Repository("BookmarkListDao")
public class BookmarkListDao {

	@Autowired
    private transient SessionFactory sessionFactory;
	
	@Transactional(readOnly=true)
	public BookmarkList getByBookmarkListId(final long bookmarkListId) {
		Session session = sessionFactory.getCurrentSession();
		return (BookmarkList) session.get(BookmarkList.class, bookmarkListId);
	}
	
	@Transactional(readOnly=true)
	public List<BookmarkList> getByUid(final long uid) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from BookmarkList where uid = :uid")
				.setLong("uid", uid);
		
		@SuppressWarnings("unchecked")
		List<BookmarkList> result = (List<BookmarkList>) query.list();
		if (result.size() < 1) {
			result = null;
		}
		return result;
	}
	
	@Transactional(readOnly=true)
	public BookmarkList getByUidAndBookmarkListId(final long uid, final long bookmarkListId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from BookmarkList where id = :bookmarkListId and uid = :uid")
				.setLong("bookmarkListId", bookmarkListId)
				.setLong("uid", uid);
		return (BookmarkList) query.list().get(0);
	}
	
	@Transactional
	public BookmarkList saveOrUpdateBookmarkList(final BookmarkList bookmarkList) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(bookmarkList);
		return bookmarkList;
	}
	
	@Transactional
	public BookmarkList saveBookmarkList(final BookmarkList bookmarkList) {
		Session session = sessionFactory.getCurrentSession();
		session.save(bookmarkList);
		return bookmarkList;
	}
	
	@Transactional
	public void destroyBookmarkList(final BookmarkList bookmarkList) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(bookmarkList);
	}
}
