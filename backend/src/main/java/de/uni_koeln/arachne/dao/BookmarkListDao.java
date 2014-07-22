package de.uni_koeln.arachne.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.BookmarkList;

@Repository("BookmarkListDao")
public class BookmarkListDao {

	@Autowired
	private transient HibernateTemplate hibernateTemplate;	
	
	public BookmarkList getByBookmarkListId(final long bookmarkListId) {
		return (BookmarkList) hibernateTemplate.get(BookmarkList.class, bookmarkListId);
	}
	
	public List<BookmarkList> getByUid(final long uid) {
		final String hql = "from BookmarkList where uid = ?";
		@SuppressWarnings("unchecked")
		List<BookmarkList> result = (List<BookmarkList>) hibernateTemplate.find(hql, uid);
		if (result.size() < 1) {
			result = null;
		}
		return result;
	}
	
	public BookmarkList getByUidAndBookmarkListId(final long uid, final long bookmarkListId) {
		final String hql = "from BookmarkList where id = ? and uid = ?";
		return (BookmarkList) hibernateTemplate.find(hql, new long[] {bookmarkListId, uid}).get(0);
	}
	
	public BookmarkList saveOrUpdateBookmarkList(final BookmarkList bookmarkList) {
		hibernateTemplate.saveOrUpdate(bookmarkList);
		return bookmarkList;
	}
	
	public BookmarkList saveBookmarkList(final BookmarkList bookmarkList) {
		hibernateTemplate.save(bookmarkList);
		return bookmarkList;
	}
	
	public void destroyBookmarkList(final BookmarkList bookmarkList) {
		hibernateTemplate.delete(bookmarkList);
	}
	
	

}
