package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import de.uni_koeln.arachne.dao.CatalogEntryDao;
import de.uni_koeln.arachne.dao.CatalogDao;
import de.uni_koeln.arachne.dao.CatalogHeadingDao;
import de.uni_koeln.arachne.mapping.CatalogEntry;
import de.uni_koeln.arachne.mapping.Catalog;
import de.uni_koeln.arachne.mapping.CatalogHeading;
import de.uni_koeln.arachne.mapping.User;
import de.uni_koeln.arachne.service.IUserRightsService;

/**
 * Handles http requests for <code>/catalogentry</code> and <code>/catalog</code>.
 */
@Controller
public class CatalogController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CatalogController.class);
	
	@Autowired
	private transient IUserRightsService rightsService;
	
	@Autowired
	private transient CatalogEntryDao catalogEntryDao;	
	
	@Autowired
	private transient CatalogHeadingDao catalogHeadingDao;
	
	@Autowired
	private transient CatalogDao catalogDao;
	
	
	/**
	 * Handles http GET request for <code>/catalogEntry/{catalogEntryId}</code>.
	 * Returns a catalogEntry entity which is serialized into JSON or XML depending
	 * on the requested format.
	 * If the given id does not refer to a catalogEntry entity, a 404 error code is returned.
	 * if the catalogEntry is not owned by the current user or no user is signed in, 
	 * a 403 error code is returned. 
	 */
	@RequestMapping(value="/catalogentry/{catalogEntryId}", method=RequestMethod.GET)
	public @ResponseBody CatalogEntry handleGetCatalogEntryRequest(
			@PathVariable("catalogEntryId") final Long catalogEntryId,
			final HttpServletResponse response) {
		CatalogEntry result = null;
		final User user = rightsService.getCurrentUser();

		LOGGER.debug("Request for catalogEntry: " + catalogEntryId + " of user: " + user.getId());

		result = catalogEntryDao.getByCatalogEntryId(catalogEntryId);
		if (result == null) {
			response.setStatus(404);
		} else if (!result.getCatalog().isCatalogOfUserWithId(user.getId()) 
				&& !result.getCatalog().isPublic()){
			result = null;
			response.setStatus(403);				
		}

		return result;
	}

	/**
	 * Handles http POST request for <code>/catalogEntry/{catalogEntryId}/update</code>.
	 * Returns the catalogEntry created and 200 if the action is permitted.
	 * Returns null and 403 if no user is signed in or the signed in user 
	 * does not own the catalogEntry to be edited.
	 */
	@RequestMapping(value="/catalogentry/{catalogEntryId}", method=RequestMethod.POST)
	public @ResponseBody CatalogEntry handleUpdateCatalogEntryRequest(
			@PathVariable("catalogEntryId") final Long catalogEntryId,
			@RequestBody final CatalogEntry catalogEntry,
			final HttpServletResponse response) {
		final User user = rightsService.getCurrentUser();
		final CatalogEntry result;
		final CatalogEntry oldCatalogEntry;
		
		LOGGER.debug("Request to update catalogEntry: " + catalogEntryId + " from user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			oldCatalogEntry = catalogEntryDao.getByCatalogEntryId(catalogEntryId);
			if (oldCatalogEntry != null
					&& (oldCatalogEntry.getId().equals(catalogEntry.getId()))
					&& (oldCatalogEntry.getCatalog().isCatalogOfUserWithId(user.getId()))) {
				catalogEntry.setCatalog(oldCatalogEntry.getCatalog());
				result = catalogEntryDao.updateCatalogEntry(catalogEntry);
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
	 * Handles http DELETE request for <code>/catalogEntry/{id}</code>.
	 * Deletes the specified <code>CatalogEntry</code>.
	 * Returns 204 on success. 
	 * Returns 403 if the specified catalogEntry is not owned by the current user.
	 * Returns 404 if the specified catalogEntry can not be retrieved.
	 */
	@RequestMapping(value="/catalogentry/{catalogEntryId}", method=RequestMethod.DELETE)
	public void handleCatalogEntryDestroyRequest(
			final HttpServletResponse response,
			@PathVariable("catalogEntryId") final Long catalogEntryId) {
		final User user = rightsService.getCurrentUser();
		final CatalogEntry catalogEntry = catalogEntryDao.getByCatalogEntryId(catalogEntryId);
		
		LOGGER.debug("Request to destroy catalogEntry: " + catalogEntryId + " from user: " + user.getId());
		
		if (catalogEntry == null) {
			response.setStatus(404);
		} else if (catalogEntry.getCatalog().isCatalogOfUserWithId(user.getId())) {
			catalogEntryDao.deleteCatalogEntry(catalogEntry);
			response.setStatus(204);
		} else {
			response.setStatus(403);
		}
	}
	
	
	/**
	 * Handles http GET request for <code>/catalogHeading/{catalogHeadingId}</code>.
	 * Returns a catalogHeading entity which is serialized into JSON or XML depending
	 * on the requested format.
	 * If the given id does not refer to a catalogHeading entity, a 404 error code is returned.
	 * if the catalogHeading is not owned by the current user or no user is signed in, 
	 * a 403 error code is returned. 
	 */
	@RequestMapping(value="/catalogheading/{catalogHeadingId}", method=RequestMethod.GET)
	public @ResponseBody CatalogHeading handleGetCatalogHeadingRequest(
			@PathVariable("catalogHeadingId") final Long catalogHeadingId,
			final HttpServletResponse response) {
		CatalogHeading result = null;
		final User user = rightsService.getCurrentUser();

		LOGGER.debug("Request for catalogHeading: " + catalogHeadingId + " of user: " + user.getId());

		result = catalogHeadingDao.getByCatalogHeadingId(catalogHeadingId);
		if (result == null) {
			response.setStatus(404);
		} else if (!result.getCatalog().isCatalogOfUserWithId(user.getId())
				&& !result.getCatalog().isPublic()){
			result = null;
			response.setStatus(403);				
		}

		return result;
	}
	
	/**
	 * Handles http POST request for <code>/catalogHeading/{catalogHeadingId}/update</code>.
	 * Returns the catalogHeading created and 200 if the action is permitted.
	 * Returns null and 403 if no user is signed in or the signed in user 
	 * does not own the catalogHeading to be edited.
	 */
	@RequestMapping(value="/catalogheading/{catalogHeadingId}", method=RequestMethod.POST)
	public @ResponseBody CatalogHeading handleUpdateCatalogHeadingRequest(
			@PathVariable("catalogHeadingId") final Long catalogHeadingId,
			@RequestBody final CatalogHeading catalogHeading,
			final HttpServletResponse response) {
		final User user = rightsService.getCurrentUser();
		final CatalogHeading result;
		final CatalogHeading oldCatalogHeading;
		
		LOGGER.debug("Request to update catalogHeading: " + catalogHeadingId + " from user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			oldCatalogHeading = catalogHeadingDao.getByCatalogHeadingId(catalogHeadingId);
			if (oldCatalogHeading != null
					&& (oldCatalogHeading.getId().equals(catalogHeading.getId()))
					&& (oldCatalogHeading.getCatalog().isCatalogOfUserWithId(user.getId()))) {
				catalogHeading.setCatalog(oldCatalogHeading.getCatalog());
				result = catalogHeadingDao.updateCatalogHeading(catalogHeading);
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
	 * Handles http DELETE request for <code>/catalogHeading/{id}</code>.
	 * Deletes the specified <code>CatalogHeading</code>.
	 * Returns 204 on success. 
	 * Returns 403 if the specified catalogHeading is not owned by the current user.
	 * Returns 404 if the specified catalogHeading can not be retrieved.
	 */
	@RequestMapping(value="/catalogheading/{catalogHeadingId}", method=RequestMethod.DELETE)
	public void handleCatalogHeadingDestroyRequest(
			final HttpServletResponse response,
			@PathVariable("catalogHeadingId") final Long catalogHeadingId) {
		final User user = rightsService.getCurrentUser();
		final CatalogHeading catalogHeading = catalogHeadingDao.getByCatalogHeadingId(catalogHeadingId);
		
		LOGGER.debug("Request to destroy catalogHeading: " + catalogHeadingId + " from user: " + user.getId());
		
		if (catalogHeading == null) {
			response.setStatus(404);
		} else if (catalogHeading.getCatalog().isCatalogOfUserWithId(user.getId())) {
			catalogHeadingDao.deleteCatalogHeading(catalogHeading);
			response.setStatus(204);
		} else {
			response.setStatus(403);
		}
	}
	
	/**
	 * Handles http POST request for <code>/catalog/{id}/add</code>.
	 * Creates the submitted <code>CatalogHeading</code> item and adds it to the 
	 * specified <code>Catalog</code>.
	 * Returns the <code>CatalogHeading</code> created and 200.
	 * Returns null and 403 if no user is signed in or the signed in user does
	 * not own the specified <code>Catalog</code>.
	 * If the submitted <code>CatalogHeading</code> contains an id value, that value 
	 * is ignored.
	 */
	@RequestMapping(value="/catalog/{catalogId}/add", method=RequestMethod.POST)
	public @ResponseBody CatalogHeading handleCatalogHeadingCreateRequest(
			@PathVariable("catalogId") final Long catalogId,
			@RequestBody final CatalogHeading catalogHeading,
			final HttpServletResponse response) {
		final User user = rightsService.getCurrentUser();
		final Catalog catalog;
		final CatalogHeading result;
		
		LOGGER.debug("Request to create catalogHeading in catalog: " + catalogId + "from user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			catalog = catalogDao.getByCatalogId(catalogId);
			if (catalog == null) {
				result = null;
				response.setStatus(404);
			} else {
				if (catalog.isCatalogOfUserWithId(user.getId())) {
					catalogHeading.setId(null);
					catalogHeading.setCatalog(catalog);
					result = catalogHeadingDao.saveCatalogHeading(catalogHeading);
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
	 * Handles http POST request for <code>/catalogHeading/{id}/add</code>.
	 * Creates the submitted <code>CatalogEntry</code> item and adds it to the 
	 * specified <code>CatalogHeading</code>.
	 * Returns the <code>CatalogEntry</code> created and 200.
	 * Returns null and 403 if no user is signed in or the signed in user does
	 * not own the <code>Catalog</code> of the specified <code>CatalogHeading</code>.
	 * If the submitted <code>CatalogEntry</code> contains an id value, that value 
	 * is ignored.
	 */
	@RequestMapping(value="/catalogheading/{catalogHeadingId}/add", method=RequestMethod.POST)
	public @ResponseBody CatalogEntry handleCatalogEntryCreateRequest(
			@PathVariable("catalogHeadingId") final Long catalogHeadingId,
			@RequestBody final CatalogEntry catalogEntry,
			final HttpServletResponse response) {
		final User user = rightsService.getCurrentUser();
		final CatalogHeading catalogHeading;
		final Catalog catalog;
		final CatalogEntry result;
		
		LOGGER.debug("Request to create catalogEntry in catalogHeading: " + catalogHeadingId + "from user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			catalogHeading = catalogHeadingDao.getByCatalogHeadingId(catalogHeadingId);
			
			if (catalogHeading == null) {
				result = null;
				response.setStatus(404);
			} else {
				catalog = catalogHeading.getCatalog();
				if (catalog == null){
					result = null;
					response.setStatus(404);
				}
				else {
					if (catalog.isCatalogOfUserWithId(user.getId())) {
						catalogEntry.setId(null);
						catalogEntry.setCatalog(catalog);
						catalogEntry.setHeading(catalogHeading);
						result = catalogEntryDao.saveCatalogEntry(catalogEntry);
					} else {
						result = null;
						response.setStatus(403);
					}
				}
			}
		} else {
			result = null;
			response.setStatus(403);
		}
		return result;
	}
	
	/**
	 * Handles http GET request for <code>/catalog</code>.
	 * Returns all catalogs belonging to the user, that is signed in, serialized 
	 * into JSON or XML depending on the requested format.
	 * If the current user does not own any catalogs an empty List is returned.
	 * If no user is signed in, a 403 error code is returned.
	 */
	@RequestMapping(value="/catalog", method=RequestMethod.GET)
	public @ResponseBody List<Catalog> handleGetCatalogsRequest(
			final HttpServletResponse response) {
		List<Catalog> result = null;
		final User user = rightsService.getCurrentUser();
		
		LOGGER.debug("Request for all catalogs of user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			result = catalogDao.getByUid(user.getId());
			if (result == null || result.isEmpty()) {
				result = new ArrayList<Catalog>();
			}
		} else {
			response.setStatus(403);
		}
		return result;
	}
	
	/**
	 * Handles http GET request for <code>/catalog/{catalogId}</code>.
	 * Returns a catalog entity which is serialized into JSON or XML depending
	 * on the requested format.
	 * If the given id does not refer to a catalog, a 404 error code is returned.
	 * if the catalog is not owned by the current user or no user is signed in, 
	 * a 403 error code is returned.
	 * 
	 */
	@RequestMapping(value="/catalog/{catalogId}", method=RequestMethod.GET)
	public @ResponseBody Catalog handleGetCatalogRequest(
			@PathVariable("catalogId") final Long catalogId,
			final HttpServletResponse response) {
		Catalog result = null;
		final User user = rightsService.getCurrentUser();

		LOGGER.debug("Request for catalog " + catalogId + " of user: " + user.getId());

		result = catalogDao.getByCatalogId(catalogId);
		if (result == null) {
			response.setStatus(404);
		} else if (!result.isCatalogOfUserWithId(user.getId())
				&& !result.isPublic()) {
			result = null;
			response.setStatus(403);
		}

		return result;
	}

	/**
	 * Handles http POST request for <code>/catalog/{catalogId}/update</code>.
	 * Returns the catalog created and 200 if the action is permitted.
	 * Returns null and 403 if no user is signed in or the signed in user 
	 * does not own the catalog to be edited. 
	 * This method accepts updates on nested <code>CatalogEntry</code> items' fields.
	 * and creates nested <code>CatalogEntry</code> items if they do not already exist.
	 * It does not automatically delete items, that are missing from the list of nested 
	 * <code>CatalogEntry</code> items.
	 */
	@RequestMapping(value="/catalog/{requestedId}", method=RequestMethod.POST, consumes="application/json")
	public @ResponseBody Catalog handleCatalogUpdateRequest(
			@RequestBody final Catalog catalog,
			@PathVariable("requestedId") final Long requestedId,
			final HttpServletResponse response) {
		final User user = rightsService.getCurrentUser();
		final Catalog result;
		final Catalog oldCatalog;
		
		LOGGER.debug("Request to update catalog: " + catalog.getId() + " of user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			oldCatalog = catalogDao.getByCatalogId(requestedId);
			if (oldCatalog != null 
					&& (oldCatalog.getId().equals(catalog.getId()))
					&& (oldCatalog.isCatalogOfUserWithId(user.getId()))) {
				catalog.setUsers(oldCatalog.getUsers());
				if (catalog.getCatalogEntries() != null) {
					for (final CatalogEntry catalogEntry : catalog.getCatalogEntries()) {
						catalogEntry.setCatalog(catalog);
					}
				}
				result = catalogDao.saveOrUpdateCatalog(catalog);
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
	 * Handles http POST request for <code>/catalog/create</code>.
	 * Returns the catalog created and 200 if the action is permitted.
	 * Returns null and 403 if no user is signed in.
	 * This method creates all nested <code>CatalogEntry</code> items. 
	 * Existing primary id values in nested <code>CatalogEntry</code> items are
	 * ignored.
	 */
	@RequestMapping(value="/catalog", method=RequestMethod.POST, consumes="application/json")
	public @ResponseBody Catalog handleCatalogCreateRequest(
			@RequestBody final Catalog catalog,
			final HttpServletResponse response) {
		final User user = rightsService.getCurrentUser();
		final Catalog result;
		
		LOGGER.debug("Request to create catalog for user: " + user.getId());
		
		if (rightsService.isSignedInUser()) {
			Set<User> users = new HashSet<User>();
			users.add(user);
			catalog.setUsers(users);
			if (catalog.getCatalogHeadings() != null){
				for (final CatalogHeading catalogHeading : catalog.getCatalogHeadings()) {
					catalogHeading.setId(null);
					catalogHeading.setCatalog(catalog);
				}
			}
			result = catalogDao.saveCatalog(catalog);
		} else {
			result = null;
			response.setStatus(403);
		}
		return result;
	}
	
	/**
	 * Handles http DELETE request for <code>/catalog/{id}</code>.
	 * Deletes the specified <code>Catalog</code> and all associated
	 * <code>CatalogEntry</code> items.
	 * Returns 204 on success. 
	 * Returns 403 if the specified list is not owned by the current user.
	 * Returns 404 if the specified list can not be retrieved.
	 */
	@RequestMapping(value="/catalog/{catalogId}", method=RequestMethod.DELETE)
	public void handleCatalogDestroyRequest(
			final HttpServletResponse response,
			@PathVariable("catalogId") final Long catalogId) {
		final User user = rightsService.getCurrentUser();
		final Catalog catalog = catalogDao.getByCatalogId(catalogId);
		
		LOGGER.debug("Request to destroy catalog: " + catalogId + " from user: " + user.getId());
		
		if (catalog == null) {
			response.setStatus(404);
		} else if (catalog.isCatalogOfUserWithId(user.getId())) {
			catalogDao.destroyCatalog(catalog);
			response.setStatus(204);
		} else {
			response.setStatus(403);
		}
	}

}
