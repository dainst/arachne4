package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import de.uni_koeln.arachne.dao.hibernate.CatalogDao;
import de.uni_koeln.arachne.dao.hibernate.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.hibernate.Catalog;
import de.uni_koeln.arachne.mapping.hibernate.CatalogEntry;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.network.CustomMediaType;

/**
 * Handles http requests for <code>/catalogEntry</code> and
 * <code>/catalog</code>.
 */
@Controller
@Transactional
public class CatalogController {

	//private static final Logger LOGGER = LoggerFactory.getLogger(CatalogController.class);

	@Autowired
	private transient UserRightsService userRightsService;

	@Autowired
	private transient CatalogEntryDao catalogEntryDao;

	@Autowired
	private transient CatalogDao catalogDao;

	/**
	 * Handles http GET request for <code>/catalogEntry/{catalogEntryId}</code>.
	 * Returns a catalogEntry entity which is serialized into JSON or XML
	 * depending on the requested format. If the given id does not refer to a
	 * catalogEntry entity, a 404 error code is returned. if the catalogEntry is
	 * not owned by the current user or no user is signed in, a 403 error code
	 * is returned.
	 */
	@RequestMapping(value = "/catalogentry/{catalogEntryId}", 
			method = RequestMethod.GET, 
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<CatalogEntry> handleGetCatalogEntryRequest(
			@PathVariable("catalogEntryId") final Long catalogEntryId,
			@RequestParam(value = "full", required = false) Boolean full) {
		
		full = (full == null) ? false : full;
		CatalogEntry result = null;
		final User user = userRightsService.getCurrentUser();
		result = catalogEntryDao.getByCatalogEntryId(catalogEntryId, full);
		
		if (result == null) {
			return new ResponseEntity<CatalogEntry>(HttpStatus.NOT_FOUND);
		} else if (!result.getCatalog().isCatalogOfUserWithId(user.getId())	&& !result.getCatalog().isPublic()) {
			return new ResponseEntity<CatalogEntry>(HttpStatus.FORBIDDEN);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Handles http PUT request for <code>/catalogEntry/{catalogEntryId}</code>.
	 * Returns the catalogEntry created and 200 if the action is permitted.
	 * Returns null and 403 if no user is signed in or the signed in user does
	 * not own the catalogEntry to be edited.
	 */
	@RequestMapping(value = "/catalogentry/{catalogEntryId}", 
			method = RequestMethod.PUT, 
			consumes = CustomMediaType.APPLICATION_JSON_UTF8_VALUE,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<CatalogEntry> handleUpdateCatalogEntryRequest(
			@PathVariable("catalogEntryId") final Long catalogEntryId,
			@RequestBody final CatalogEntry catalogEntry) {
		
		final User user = userRightsService.getCurrentUser();
		final CatalogEntry result;
		final CatalogEntry oldCatalogEntry;

		if (userRightsService.isSignedInUser()) {
			oldCatalogEntry = catalogEntryDao
					.getByCatalogEntryId(catalogEntryId);
			if (oldCatalogEntry != null
					&& (oldCatalogEntry.getId().equals(catalogEntry.getId()))
					&& (oldCatalogEntry.getCatalog().isCatalogOfUserWithId(user
							.getId()))) {
				catalogEntry.setCatalog(oldCatalogEntry.getCatalog());
				catalogEntry.setParent(oldCatalogEntry.getParent());
				result = catalogEntryDao.updateCatalogEntry(catalogEntry);
				result.generatePath();
				catalogEntryDao.updateCatalogEntry(result);
			} else {
				return new ResponseEntity<CatalogEntry>(HttpStatus.NOT_FOUND);
			}
		} else {
			return new ResponseEntity<CatalogEntry>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Handles http DELETE request for <code>/catalogEntry/{id}</code>. Deletes
	 * the specified <code>CatalogEntry</code>. Returns 204 on success. Returns
	 * 403 if the specified catalogEntry is not owned by the current user.
	 */
	@RequestMapping(value = "/catalogentry/{catalogEntryId}", 
			method = RequestMethod.DELETE, 
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Void> handleCatalogEntryDestroyRequest(
			@PathVariable("catalogEntryId") final Long catalogEntryId) {
		
		final User user = userRightsService.getCurrentUser();
		final CatalogEntry catalogEntry = catalogEntryDao.getByCatalogEntryId(catalogEntryId);
		
		if (catalogEntry != null) {
			if (catalogEntry.getCatalog().isCatalogOfUserWithId(user.getId())) {
				CatalogEntry parent = catalogEntry.getParent();
				parent.getChildren().remove((int) catalogEntry.getIndexParent());
				catalogEntryDao.updateCatalogEntry(parent);
				catalogEntryDao.deleteCatalogEntry(catalogEntry);
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@RequestMapping(value = "/catalogentry", 
			method = RequestMethod.POST,
			consumes = CustomMediaType.APPLICATION_JSON_UTF8_VALUE,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<CatalogEntry> handleCatalogEntryCreateRequest(
			@RequestBody final CatalogEntry catalogEntry) {
		
		final User user = userRightsService.getCurrentUser();
		final CatalogEntry catalogEntryParent;
		final Catalog catalog;
		final CatalogEntry result;

		if (userRightsService.isSignedInUser()
				&& catalogEntry.getParentId() != null) {

			catalogEntryParent = catalogEntryDao
					.getByCatalogEntryId(catalogEntry.getParentId());

			if (catalogEntryParent == null) {
				return new ResponseEntity<CatalogEntry>(HttpStatus.BAD_REQUEST);
			} else {

				catalog = catalogEntryParent.getCatalog();
				if (catalog.isCatalogOfUserWithId(user.getId())) {
					catalogEntry.setId(null);
					catalogEntry.setParent(catalogEntryParent);
					if (catalogEntry.getIndexParent() == null
							|| catalogEntry.getIndexParent() >= catalogEntryParent
									.getChildren().size()) {
						catalogEntryParent.addToChildren(catalogEntry);
					} else {
						catalogEntryParent.getChildren().add(
								catalogEntry.getIndexParent(), catalogEntry);
					}
					catalogEntry.setCatalog(catalog);
					catalogEntryDao.updateCatalogEntry(catalogEntryParent);
					catalogEntry.generatePath();
					result = catalogEntryDao.updateCatalogEntry(catalogEntry);
				} else {
					return new ResponseEntity<CatalogEntry>(HttpStatus.FORBIDDEN);
				}
			}
		} else {
			return new ResponseEntity<CatalogEntry>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(result);
	}

	/**
	 * Handles http GET request for <code>/catalog</code>. Returns all catalogs
	 * belonging to the user, that is signed in, serialized into JSON or XML
	 * depending on the requested format. If the current user does not own any
	 * catalogs an empty List is returned. If no user is signed in, a 403 error
	 * code is returned.
	 */
	@RequestMapping(value = "/catalog", 
			method = RequestMethod.GET,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<List<Catalog>> handleGetCatalogsRequest(
			@RequestParam(value = "full", required = false) Boolean full) {
		
		full = (full == null) ? false : full;
		List<Catalog> result = null;
		final User user = userRightsService.getCurrentUser();
		
		if (userRightsService.isSignedInUser()) {
			result = catalogDao.getByUid(user.getId(), full);
			if (result == null || result.isEmpty()) {
				result = new ArrayList<Catalog>();
			}
		} else {
			return new ResponseEntity<List<Catalog>>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(result);
	}

	/**
	 * Handles http GET request for <code>/catalog/{catalogId}</code>. Returns a
	 * catalog entity which is serialized into JSON or XML depending on the
	 * requested format. If the given id does not refer to a catalog, a 404
	 * error code is returned. if the catalog is not owned by the current user
	 * or no user is signed in, a 403 error code is returned.
	 * @param catalogId The ctalog id of interest.
	 * @param full If the full catalog shall be retrieved (with all entries) or only root and it's direct children.
	 * @return The catalog.
	 */
	@RequestMapping(value = "/catalog/{catalogId}", 
			method = RequestMethod.GET,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Catalog> handleGetCatalogRequest(
			@PathVariable("catalogId") final Long catalogId,
			@RequestParam(value = "full", required = false) Boolean full) {
		
		full = (full == null) ? false : full;
		final User user = userRightsService.getCurrentUser();
		Catalog result = catalogDao.getByCatalogId(catalogId, full);
		if (result == null) {
			return new ResponseEntity<Catalog>(HttpStatus.NOT_FOUND);
		} else if (!result.isCatalogOfUserWithId(user.getId()) && !result.isPublic()) {
			result = null;
			return new ResponseEntity<Catalog>(HttpStatus.FORBIDDEN);
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Handles http PUT request for <code>/catalog/{catalogId}</code>. Returns
	 * the catalog created and 200 if the action is permitted. Returns null and
	 * 403 if no user is signed in or the signed in user does not own the
	 * catalog to be edited. This method accepts updates on nested
	 * <code>CatalogEntry</code> items' fields. and creates nested
	 * <code>CatalogEntry</code> items if they do not already exist. It does not
	 * automatically delete items, that are missing from the list of nested
	 * <code>CatalogEntry</code> items.
	 */
	@RequestMapping(value = "/catalog/{requestedId}", 
			method = RequestMethod.PUT, 
			consumes = CustomMediaType.APPLICATION_JSON_UTF8_VALUE,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Catalog> handleCatalogUpdateRequest(
			@RequestBody final Catalog catalog,
			@PathVariable("requestedId") final Long requestedId) {
		
		final User user = userRightsService.getCurrentUser();
		final Catalog result;
		final Catalog oldCatalog;

		if (userRightsService.isSignedInUser()) {
			oldCatalog = catalogDao.getByCatalogId(requestedId);
			if (oldCatalog != null
					&& (oldCatalog.getId().equals(catalog.getId()))
					&& (oldCatalog.isCatalogOfUserWithId(user.getId()))) {
				catalog.setUsers(oldCatalog.getUsers());
				catalog.setCatalogEntries(null);
				catalog.addToCatalogEntries(catalog.getRoot());
				catalog.getRoot().setCatalog(catalog);

				result = catalogDao.saveOrUpdateCatalog(catalog);

				// Get catalogEntries that were included before but are not
				// anymore and delete them
				catalogEntryDao.deleteOrphanedCatalogEntries(result);

				result.getRoot().generatePath();
				catalogEntryDao.updateCatalogEntry(result.getRoot());

			} else {
				return new ResponseEntity<Catalog>(HttpStatus.FORBIDDEN);
			}
		} else {
			return new ResponseEntity<Catalog>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(result);
	}

	/**
	 * Handles http POST request for <code>/catalog/create</code>. Returns the
	 * catalog created and 200 if the action is permitted. Returns null and 403
	 * if no user is signed in. This method creates all nested
	 * <code>CatalogEntry</code> items. Existing primary id values in nested
	 * <code>CatalogEntry</code> items are ignored.
	 */
	@RequestMapping(value = "/catalog", 
			method = RequestMethod.POST, 
			consumes = CustomMediaType.APPLICATION_JSON_UTF8_VALUE,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Catalog> handleCatalogCreateRequest(@RequestBody final Catalog catalog) {

		final User user = userRightsService.getCurrentUser();
		final Catalog result;

		if (userRightsService.isSignedInUser()) {
			Set<User> users = new HashSet<User>();
			users.add(user);
			catalog.setUsers(users);

			catalog.setCatalogEntries(null);
			CatalogEntry root = catalog.getRoot();
			root.setId(null);
			root.setCatalog(catalog);
			catalog.addToCatalogEntries(root);
			result = catalogDao.saveCatalog(catalog);

			result.getRoot().generatePath();
			catalogDao.saveOrUpdateCatalog(result);

		} else {
			return new ResponseEntity<Catalog>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(result);
	}

	/**
	 * Handles http DELETE request for <code>/catalog/{id}</code>. Deletes the
	 * specified <code>Catalog</code> and all associated
	 * <code>CatalogEntry</code> items. Returns 204 on success. Returns 403 if
	 * the specified list is not owned by the current user.
	 */
	@RequestMapping(value = "/catalog/{catalogId}", 
			method = RequestMethod.DELETE)
	public ResponseEntity<String> handleCatalogDestroyRequest(@PathVariable("catalogId") final Long catalogId) {

		final User user = userRightsService.getCurrentUser();
		final Catalog catalog = catalogDao.getByCatalogId(catalogId);

		if (catalog != null) {
			if (!catalog.isCatalogOfUserWithId(user.getId())) {
				return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
			} else {
				catalogDao.destroyCatalog(catalog);
			}
		}
		
		return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
	}
	
	@RequestMapping(value = "/catalogByEntity/{entityId}", 
			method = RequestMethod.GET,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<CatalogIdList> handleGetCatalogByEntityRequest(
			@PathVariable("entityId") final Long entityId) {
		
		final List<Long> result = catalogEntryDao.getPrivateCatalogIdsByEntityId(entityId);
		if (result == null || result.isEmpty()) {
			return ResponseEntity.ok(new CatalogIdList(new ArrayList<Long>()));
		}
		return ResponseEntity.ok(new CatalogIdList(result));
	}
	
	// handleGetCatalogByEntityRequest() return type (better JSON response than the pure list)
	@JsonInclude(value=Include.NON_EMPTY)
	private class CatalogIdList {
		private List<Long> catalogIds;

		CatalogIdList(final List<Long> catalogIds) {
			this.catalogIds = catalogIds;
		}
		
		@SuppressWarnings("unused")
		public List<Long> getCatalogIds() {
			return catalogIds;
		}
	}
}
