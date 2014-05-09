package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.dao.BookmarkDao;
import de.uni_koeln.arachne.dao.BookmarkListDao;
import de.uni_koeln.arachne.mapping.Bookmark;
import de.uni_koeln.arachne.mapping.BookmarkList;
import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.service.IUserRightsService;

/**
 * Handles http requests for <code>/bookmark</code> and <code>/bookmarklist</code>.
 */
@Controller
public class BookmarkListController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkListController.class);
	
	@Autowired
	private transient IUserRightsService rightsService;
	
	@Autowired
	private transient BookmarkDao bookmarkDao;
	
	@Autowired
	private transient BookmarkListDao bookmarkListDao;
	
	/**
	 * Handles http GET request for <code>/bookmark/{bookmarkId}</code>.
	 * Returns a bookmark entity which is serialized into JSON or XML depending
	 * on the requested format.
	 * If the given id does not refer to a bookmark entity, a 404 error code is returned.
	 * if the bookmark is not owned by the current user or no user is signed in, 
	 * a 403 error code is returned. 
	 */
	@RequestMapping(value="/bookmark/{bookmarkId}", method=RequestMethod.GET)
	public @ResponseBody Bookmark handleGetBookmarkRequest(
			@PathVariable("bookmarkId") final Long bookmarkId,
			final HttpServletResponse response) {
		Bookmark result = null;
		final UserAdministration user = rightsService.getCurrentUser();
		
		LOGGER.debug("Request for bookmark: " + bookmarkId + " of user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			result = bookmarkDao.getByBookmarkId(bookmarkId);
			if (result == null) {
				response.setStatus(404);
			} else if (result.getBookmarkList().getUser().getId() != user.getId()) {
				result = null;
				response.setStatus(403);
			}
		} else {
			response.setStatus(403);
		}
		return result;
	}
	
	/**
	 * Handles http POST request for <code>/bookmark/{bookmarkId}/update</code>.
	 * Returns the bookmark created and 200 if the action is permitted.
	 * Returns null and 403 if no user is signed in or the signed in user 
	 * does not own the bookmark to be edited.
	 */
	@RequestMapping(value="/bookmark/{bookmarkId}", method=RequestMethod.POST)
	public @ResponseBody Bookmark handleUpdateBookmarkRequest(
			@PathVariable("bookmarkId") final Long bookmarkId,
			@RequestBody final Bookmark bookmark,
			final HttpServletResponse response) {
		final UserAdministration user = rightsService.getCurrentUser();
		final Bookmark result;
		final Bookmark oldBookmark;
		
		LOGGER.debug("Request to update bookmark: " + bookmarkId + " from user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			oldBookmark = bookmarkDao.getByBookmarkId(bookmarkId);
			if (oldBookmark != null
					&& (oldBookmark.getId() == bookmark.getId())
					&& (oldBookmark.getBookmarkList().getUser().getId() == user.getId())) {
				bookmark.setBookmarkList(oldBookmark.getBookmarkList());
				result = bookmarkDao.updateBookmark(bookmark);
			} else {
				result = null;
				response.setStatus(403);
			}
		} else {
			result = null;
			response.setStatus(403);
		}
		return result;
	}
	
	/**
	 * Handles http DELETE request for <code>/bookmark/{id}</code>.
	 * Deletes the specified <code>Bookmark</code>.
	 * Returns 204 on success. 
	 * Returns 403 if the specified bookmark is not owned by the current user.
	 * Returns 404 if the specified bookmark can not be retrieved.
	 */
	@RequestMapping(value="/bookmark/{bookmarkId}", method=RequestMethod.DELETE)
	public void handleBookmarkDestroyRequest(
			final HttpServletResponse response,
			@PathVariable("bookmarkId") final Long bookmarkId) {
		final UserAdministration user = rightsService.getCurrentUser();
		final Bookmark bookmark = bookmarkDao.getByBookmarkId(bookmarkId);
		
		LOGGER.debug("Request to destroy bookmark: " + bookmarkId + " from user: " + user.getId());
		
		if (bookmark == null) {
			response.setStatus(404);
		} else if (bookmark.getBookmarkList().getUser().getId() == user.getId()) {
			bookmarkDao.destroyBookmark(bookmark);
			response.setStatus(204);
		} else {
			response.setStatus(403);
		}
	}
	
	/**
	 * Handles http POST request for <code>/bookmarklist/{id}/add</code>.
	 * Creates the submitted <code>Bookmark</code> item and adds it to the 
	 * specified <code>BookmarkList</code>.
	 * Returns the <code>Bookmark</code> created and 200.
	 * Returns null and 403 if no user is signed in or the signed in user does
	 * not own the specified <code>BookmarkList</code>.
	 * If the submitted <code>Bookmark</code> contains an id value, that value 
	 * is ignored.
	 */
	@RequestMapping(value="/bookmarkList/{bookmarkListId}/add", method=RequestMethod.POST)
	public @ResponseBody Bookmark handleBookmarkCreateRequest(
			@PathVariable("bookmarkListId") final Long bookmarkListId,
			@RequestBody final Bookmark bookmark,
			final HttpServletResponse response) {
		final UserAdministration user = rightsService.getCurrentUser();
		final BookmarkList bookmarkList;
		final Bookmark result;
		
		LOGGER.debug("Request to create bookmark in bookmark list: " + bookmarkListId + "from user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			bookmarkList = bookmarkListDao.getByBookmarkListId(bookmarkListId);
			if (bookmarkList == null) {
				result = null;
				response.setStatus(404);
			} else {
				if (bookmarkList.getUser().getId() == user.getId()) {
					bookmark.setId(null);
					bookmark.setBookmarkList(bookmarkList);
					result = bookmarkDao.saveBookmark(bookmark);
				} else {
					result = null;
					response.setStatus(403);
				}
			}
		} else {
			result = null;
			response.setStatus(403);
		}
		return result;
	}
	
	/**
	 * Handles http GET request for <code>/bookmarklist</code>.
	 * Returns all bookmarkLists belonging to the user, that is signed in, serialized 
	 * into JSON or XML depending on the requested format.
	 * If the current user does not own any bookmarks an empty List is returned.
	 * If no user is signed in, a 403 error code is returned.
	 */
	@RequestMapping(value="/bookmarklist", method=RequestMethod.GET)
	public @ResponseBody List<BookmarkList> handleGetBookmarksRequest(
			final HttpServletResponse response) {
		List<BookmarkList> result = null;
		final UserAdministration user = rightsService.getCurrentUser();
		
		LOGGER.debug("Request for all bookmark lists of user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			result = bookmarkListDao.getByUid(user.getId());
			if (result == null || result.isEmpty()) {
				result = new ArrayList<BookmarkList>();
			}
		} else {
			response.setStatus(403);
		}
		return result;
	}
	
	/**
	 * Handles http GET request for <code>/bookmarklist/{bookmarkListId}</code>.
	 * Returns a bookmarkList entity which is serialized into JSON or XML depending
	 * on the requested format.
	 * If the given id does not refer to a bookmarkList, a 404 error code is returned.
	 * if the bookmarkList is not owned by the current user or no user is signed in, 
	 * a 403 error code is returned.
	 * 
	 */
	@RequestMapping(value="/bookmarklist/{bookmarkListId}", method=RequestMethod.GET)
	public @ResponseBody BookmarkList handleGetBookmarkListRequest(
			@PathVariable("bookmarkListId") final Long bookmarkListId,
			final HttpServletResponse response) {
		BookmarkList result = null;
		final UserAdministration user = rightsService.getCurrentUser();
		
		LOGGER.debug("Request for bookmarkList " + bookmarkListId + " of user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			result = bookmarkListDao.getByBookmarkListId(bookmarkListId);
			if (result == null) {
				response.setStatus(404);
			} else if (result.getUser().getId() != user.getId()) {
				result = null;
				response.setStatus(403);
			}
		} else {
			response.setStatus(403);
		}
		return result;
	}

	/**
	 * Handles http POST request for <code>/bookmarklist/{bookmarkListId}/update</code>.
	 * Returns the bookmarkList created and 200 if the action is permitted.
	 * Returns null and 403 if no user is signed in or the signed in user 
	 * does not own the bookmarkList to be edited. 
	 * This method accepts updates on nested <code>Bookmark</code> items' fields.
	 * and creates nested <code>Bookmark</code> items if they do not already exist.
	 * It does not automatically delete items, that are missing from the list of nested 
	 * <code>Bookmark</code> items.
	 */
	@RequestMapping(value="/bookmarklist/{requestedId}", method=RequestMethod.POST, consumes="application/json")
	public @ResponseBody BookmarkList handleBookmarkListUpdateRequest(
			@RequestBody final BookmarkList bookmarkList,
			@PathVariable("requestedId") final Long requestedId,
			final HttpServletResponse response) {
		final UserAdministration user = rightsService.getCurrentUser();
		final BookmarkList result;
		final BookmarkList oldBookmarkList;
		
		LOGGER.debug("Request to update bookmarkList: " + bookmarkList.getId() + " of user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			oldBookmarkList = bookmarkListDao.getByBookmarkListId(requestedId);
			if (oldBookmarkList != null 
					&& (oldBookmarkList.getId() == bookmarkList.getId())
					&& (user.getId() == oldBookmarkList.getUser().getId())) {
				bookmarkList.setUser(user);
				for (final Bookmark bookmark : bookmarkList.getBookmarks()) {
					bookmark.setBookmarkList(bookmarkList);
				}
				result = bookmarkListDao.saveOrUpdateBookmarkList(bookmarkList);
			} else {
				result = null;
				response.setStatus(403);
			}
		} else {
			result = null;
			response.setStatus(403);
		}
		return result;
	}
	
	/**
	 * Handles http POST request for <code>/bookmarklist/create</code>.
	 * Returns the bookmarkList created and 200 if the action is permitted.
	 * Returns null and 403 if no user is signed in.
	 * This method creates all nested <code>Bookmark</code> items. 
	 * Existing primary id values in nested <code>Bookmark</code> items are
	 * ignored.
	 */
	@RequestMapping(value="/bookmarklist", method=RequestMethod.POST, consumes="application/json")
	public @ResponseBody BookmarkList handleBookmarkListCreateRequest(
			@RequestBody final BookmarkList bookmarkList,
			final HttpServletResponse response) {
		final UserAdministration user = rightsService.getCurrentUser();
		final BookmarkList result;
		
		LOGGER.debug("Request to create bookmarkList for user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			bookmarkList.setUser(user);
			for (final Bookmark bookmark : bookmarkList.getBookmarks()) {
				bookmark.setId(null);
				bookmark.setBookmarkList(bookmarkList);
			}
			result = bookmarkListDao.saveBookmarkList(bookmarkList);
		} else {
			result = null;
			response.setStatus(403);
		}
		return result;
	}
	
	/**
	 * Handles http DELETE request for <code>/bookmarklist/{id}</code>.
	 * Deletes the specified <code>BookmarkList</code> and all associated
	 * <code>Bookmark</code> items.
	 * Returns 204 on success. 
	 * Returns 403 if the specified list is not owned by the current user.
	 * Returns 404 if the specified list can not be retrieved.
	 */
	@RequestMapping(value="/bookmarklist/{bookmarkListId}", method=RequestMethod.DELETE)
	public void handleBookmarkListDestroyRequest(
			final HttpServletResponse response,
			@PathVariable("bookmarkListId") final Long bookmarkListId) {
		final UserAdministration user = rightsService.getCurrentUser();
		final BookmarkList bookmarkList = bookmarkListDao.getByBookmarkListId(bookmarkListId);
		
		LOGGER.debug("Request to destroy bookmarkList: " + bookmarkListId + " from user: " + user.getId());
		
		if (bookmarkList == null) {
			response.setStatus(404);
		} else if (bookmarkList.getUser().getId() == user.getId()) {
			bookmarkListDao.destroyBookmarkList(bookmarkList);
			response.setStatus(204);
		} else {
			response.setStatus(403);
		}
	}

}
