package de.uni_koeln.arachne.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.Bookmark;
import de.uni_koeln.arachne.mapping.BookmarkList;

@Repository("BookmarkListDao")
public class BookmarkListDao extends AbstractHibernateTemplateDao {
	
	public BookmarkList getByBookmarkListId(final Long bookmarkListId) {
		return (BookmarkList) hibernateTemplate.get(BookmarkList.class, bookmarkListId);
	}
	
	public List<BookmarkList> getByUid(final Long uid) {
		final String hql = "from BookmarkList where uid = ?";
		List<BookmarkList> result = (List<BookmarkList>) hibernateTemplate.find(hql, uid);
		if (result.size() < 1) {
			result = null;
		}
		return result;
	}
	
	public BookmarkList getByUidAndBookmarkListId(final Long uid, final long bookmarkListId) {
		final String hql = "from BookmarkList where id = ? and uid = ?";
		BookmarkList result = (BookmarkList) hibernateTemplate.find(hql, bookmarkListId, uid).get(0);
		return result;
	}
	
	public BookmarkList saveOrUpdateBookmarkList(final BookmarkList bookmarkList) {
		hibernateTemplate.saveOrUpdate(bookmarkList);
		return bookmarkList;
	}
	
	

}
