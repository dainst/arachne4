package de.uni_koeln.arachne.controller;

import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.arachne.util.sql.CatalogEntryExtended;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_koeln.arachne.dao.jdbc.CatalogDao;
import de.uni_koeln.arachne.dao.jdbc.CatalogEntryDao;
import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.service.CatalogService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.util.network.CustomMediaType;

/**
 * Handles http requests for <code>/catalogEntry</code> and
 * <code>/catalog</code>.
 * 
 * @author Karen Schwane
 * @author Reimar Grabowski
 * @author Sebastian Cuy
 * @author Daniel Marreiros de Oliveira
 */
@Controller
@RequestMapping("/catalog")
public class CatalogController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CatalogController.class);

	@Autowired
	private transient UserRightsService userRightsService;

	@Autowired
	private transient CatalogEntryDao catalogEntryDao;

	@Autowired
	private transient CatalogDao catalogDao;

	@Autowired
	private transient CatalogService pdfService;
	
	/**
	 * Handles HTTP GET requests for <code>/catalog/entry/{catalogEntryId}</code>.
	 * Returns a catalogEntry entity which is serialized to JSON. If the given id does not refer to a
	 * catalogEntry entity, a 404 error code is returned. if the catalogEntry is
	 * not owned by the current user or no user is signed in, a 403 error code
	 * is returned.
	 * @param catalogEntryId The id of the catalog entry.
	 * @param full If all children or only direct children shall be included.
	 * @param limit If full = false then limit restricts the number of direct children to the desired value 
	 * (-1 for no limit).
	 * @param offset If full = false and limit > 0 then offset gives an offset into the direct children list.
	 * @return A {@link ResponseEntity} containing the catalog entry or the error code on failures.
	 */
	@RequestMapping(value = "entry/{catalogEntryId}", 
			method = RequestMethod.GET, 
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<CatalogEntry> handleGetCatalogEntryRequest(
			@PathVariable("catalogEntryId") final Long catalogEntryId,
			@RequestParam(value = "full", required = false) Boolean full,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) {
		
		full = (full == null) ? false : full;
		limit = (limit == null) ? -1 : limit;
		offset = (offset == null) ? 0 : offset;
		CatalogEntry result = null;
		final User user = userRightsService.getCurrentUser();
		result = catalogEntryDao.getById(catalogEntryId, full, limit, offset);
				
		if (result == null) {
			return new ResponseEntity<CatalogEntry>(HttpStatus.NOT_FOUND);
		} else {
			final Catalog catalog = catalogDao.getById(result.getCatalogId());
			if (!catalog.isCatalogOfUserWithId(user.getId()) && !catalog.isPublic()) {
				return new ResponseEntity<CatalogEntry>(HttpStatus.FORBIDDEN);
			}
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Handles HTTP PUT requests for <code>/catalog/entry/{catalogEntryId}</code>.
	 * Returns the catalogEntry created and 200 if the action is permitted.
	 * Returns null and 403 if no user is signed in or the signed in user does
	 * not own the catalogEntry to be edited.
	 * @param catalogEntryId The catalog id of the catalog the entry shall be added to.
	 * @param newCatalogEntry The catalog entry to add.
	 * @return A {@link ResponseEntity} containing the catalog entry or the corresponding error code on failure.
	 */
	@RequestMapping(value = "entry/{catalogEntryId}", 
			method = RequestMethod.PUT, 
			consumes = CustomMediaType.APPLICATION_JSON_UTF8_VALUE,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<CatalogEntry> handleUpdateCatalogEntryRequest(
			@PathVariable("catalogEntryId") final Long catalogEntryId,
			@RequestBody CatalogEntry newCatalogEntry) {
		
		final User user = userRightsService.getCurrentUser();
		final CatalogEntry oldCatalogEntry = catalogEntryDao.getById(catalogEntryId);
		if (oldCatalogEntry != null) {
			Catalog catalog = catalogDao.getById(oldCatalogEntry.getCatalogId());
			if (userRightsService.isSignedInUser() && catalog.isCatalogOfUserWithId(user.getId())) {
				if (oldCatalogEntry.getId().equals(newCatalogEntry.getId())) {
					newCatalogEntry = catalogEntryDao.updateCatalogEntry(newCatalogEntry);	
					if (newCatalogEntry != null) {
						return ResponseEntity.status(HttpStatus.OK).body(newCatalogEntry);
					} else {
						return new ResponseEntity<CatalogEntry>(HttpStatus.UNPROCESSABLE_ENTITY);
					}
				} else {
					return new ResponseEntity<CatalogEntry>(HttpStatus.UNPROCESSABLE_ENTITY);
				}
			} else {
				return new ResponseEntity<CatalogEntry>(HttpStatus.FORBIDDEN);
			}
		}
		return new ResponseEntity<CatalogEntry>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Handles HTTP DELETE requests for <code>catalog/entry/{catalogEntryId}</code>. Deletes
	 * the specified <code>CatalogEntry</code>. Returns 204 on success. Returns
	 * 403 if the specified catalogEntry is not owned by the current user.
	 * @param catalogEntryId The id of the catalog entry.
	 * @return A {@link ResponseEntity} without body and only an HTTP status code.
	 */
	@RequestMapping(value = "entry/{catalogEntryId}", 
			method = RequestMethod.DELETE, 
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Void> handleCatalogEntryDestroyRequest(
			@PathVariable("catalogEntryId") final Long catalogEntryId) {
		
		final User user = userRightsService.getCurrentUser();
		final CatalogEntry catalogEntry = catalogEntryDao.getById(catalogEntryId);
		
		if (catalogEntry != null) {
			final Catalog catalog = catalogDao.getById(catalogEntry.getCatalogId());
			if (catalog.isCatalogOfUserWithId(user.getId())) {
				catalogEntryDao.delete(catalogEntry.getId());
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Handles HTTP POST requests for <code>catalog/entry</code>.
	 * Adds/updates the posted catalog entry to the corresponding catalog.
	 * Returns an HTTP status code 403 if the user is not allowed to modify the catalog the entry belongs to or 400 if 
	 * the posted catalog entry is not semantically correct.
	 * @param catalogEntry The catalog entry.
	 * @return A {@link ResponseEntity} containing the persisted entry or the corresponding error code on failure. 
	 */
	@RequestMapping(value = "entry", 
			method = RequestMethod.POST,
			consumes = CustomMediaType.APPLICATION_JSON_UTF8_VALUE,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<CatalogEntry> handleCatalogEntryCreateRequest(
			@RequestBody final CatalogEntry catalogEntry) {
		
		final User user = userRightsService.getCurrentUser();
		final CatalogEntry catalogEntryParent;
		final Catalog catalog;
		
		if (userRightsService.isSignedInUser()) {
			if (catalogEntry.getParentId() != null) {
				catalogEntryParent = catalogEntryDao.getById(catalogEntry.getParentId());
				if (catalogEntryParent == null) {
					return new ResponseEntity<CatalogEntry>(HttpStatus.BAD_REQUEST);
				} else {
					catalog = catalogDao.getById(catalogEntryParent.getCatalogId());
					if (catalog.isCatalogOfUserWithId(user.getId())) {
						catalogEntry.setId(null);
						catalogEntry.setParentId(catalogEntryParent.getId());
						catalogEntry.setCatalogId(catalog.getId());
						try {
							return ResponseEntity.ok(catalogEntryDao.saveCatalogEntry(catalogEntry));
						} catch (Exception e) {
							LOGGER.error("Failed to save/update catalog entry.", e);
							return new ResponseEntity<CatalogEntry>(HttpStatus.BAD_REQUEST);
						}
					} else {
						return new ResponseEntity<CatalogEntry>(HttpStatus.FORBIDDEN);
					}
				}
			} else {
				return new ResponseEntity<CatalogEntry>(HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<CatalogEntry>(HttpStatus.FORBIDDEN);
		}
	}

	/**
	 * Handles http GET request for <code>/catalog</code>. Returns all catalogs belonging to the user 
	 * that is signed in, serialized to JSON. If the current user does not own any catalogs an empty List is returned. 
	 * If no user is signed in, a 403 error code is returned.
	 * @param full Indicates if the full catalog (including all entries) or only the 'first level' of the catalog shall 
	 * be retrieved. Defaults to false.
	 * @param limit If full == false then this parameter limits the children of the root entry (-1 for no limit).
	 * @param offset If full == false then this parameter is an offset into the children of the root entry.
	 * @return A {@link ResponseEntity} 
	 */
	@RequestMapping(value = "", 
			method = RequestMethod.GET,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<List<Catalog>> handleGetCatalogsRequest(
			@RequestParam(value = "full", required = false) Boolean full,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) {
		
		full = (full == null) ? false : full;
		limit = (limit == null) ? -1 : limit;
		offset = (offset == null) ? 0 : offset;
		List<Catalog> result = null;
		final User user = userRightsService.getCurrentUser();
		
		if (userRightsService.isSignedInUser()) {
			result = catalogDao.getByUserId(user.getId(), full, limit, offset);
			if (result == null || result.isEmpty()) {
				result = new ArrayList<Catalog>();
			}
			return ResponseEntity.ok(result);
		} else {
			return new ResponseEntity<List<Catalog>>(HttpStatus.FORBIDDEN);
		}
	}

	/**
	 * Handles http GET request for <code>/catalog/{catalogId}</code>. Returns a
	 * catalog which is serialized into JSON or XML depending on the
	 * requested format. If the given id does not refer to a catalog, a 404
	 * error code is returned. if the catalog is not owned by the current user
	 * or no user is signed in, a 403 error code is returned.
	 * @param catalogId The catalog id of interest.
	 * @param full If the full catalog shall be retrieved (with all entries) or only root and it's direct children.
	 * @param limit If full == false then this parameter limits the children of the root entry (-1 for no limit).
	 * @param offset If full == false then this parameter is an offset into the children of the root entry.
	 * @return The catalog.
	 */
	@RequestMapping(value = "{catalogId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> handleGetCatalogRequest(
			@PathVariable("catalogId") final Long catalogId,
			@RequestParam(value = "full", required = false) Boolean full,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) {
		
		full = (full == null) ? false : full;
		limit = (limit == null) ? -1 : limit;
		offset = (offset == null) ? 0 : offset;
		final User user = userRightsService.getCurrentUser();
		Catalog result = catalogDao.getById(catalogId, full, limit, offset);
		if (result == null) {
			return new ResponseEntity<Catalog>(HttpStatus.NOT_FOUND);
		} else if (!result.isCatalogOfUserWithId(user.getId()) && !result.isPublic()) {
			result = null;
			return new ResponseEntity<Catalog>(HttpStatus.FORBIDDEN);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}



	/**
	 * Handles http PUT request for <code>/catalog/{catalogId}</code>. 
	 * Updates an in the DB persisted catalog.
	 * Returns the catalog created and 200 if the action is permitted. Returns null and 403 if no user is signed in or 
	 * the signed in user does not own the catalog to be edited. This method accepts updates on nested 
	 * {@link CatalogEntry} items' fields. and creates nested {@link CatalogEntry} items if they do not already exist. 
	 * It does not automatically delete items, that are missing from the list of nested {@link CatalogEntry} items.
	 * @param catalog The updated catalog.
	 * @param requestedId The id of the catalog to update.
	 * @return A {@link ResponseEntity} containing the updated catalog as retrieved from the DB.
	 */
	@RequestMapping(value = "{requestedId}", 
			method = RequestMethod.PUT, 
			consumes = CustomMediaType.APPLICATION_JSON_UTF8_VALUE,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Catalog> handleCatalogUpdateRequest(
			@RequestBody final Catalog catalog,
			@PathVariable("requestedId") final Long requestedId) {

		if (!userRightsService.isSignedInUser())
            return new ResponseEntity<Catalog>(HttpStatus.FORBIDDEN);

		final Catalog oldCatalog = catalogDao.getById(requestedId);
        if (oldCatalog == null)
            return new ResponseEntity<Catalog>(HttpStatus.NOT_FOUND);

        return ResponseEntity.ok(catalogDao.updateCatalog(catalog));
	}

	/**
	 * Handles http POST request for <code>/catalog/create</code>.
	 * Creates a catalog and persists it in the DB. 
	 * Returns the catalog created and 200 if the action is permitted. Returns 403 if no user is signed in and 422 if 
	 * the catalog could not be created. 
	 * This method creates all nested {@link CatalogEntry} items. Existing primary id values in nested
	 * {@link CatalogEntry} items are ignored.
	 * @param catalog The catalog.
	 * @return A {@link ResponseEntity} containing the catalog as retrieved from the DB or a status code on failure.
	 */
	@RequestMapping(value = "", 
			method = RequestMethod.POST, 
			consumes = CustomMediaType.APPLICATION_JSON_UTF8_VALUE,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Catalog> handleCatalogCreateRequest(@RequestBody final Catalog catalog) {

		if (userRightsService.isSignedInUser()) {
			final Catalog savedCatalog = catalogDao.saveCatalog(catalog);
			if (savedCatalog != null) {
				return ResponseEntity.ok(savedCatalog);
			} else {
				return new ResponseEntity<Catalog>(HttpStatus.UNPROCESSABLE_ENTITY);
			}
		} else {
			return new ResponseEntity<Catalog>(HttpStatus.FORBIDDEN);
		}
	}

	/**
	 * Handles http DELETE request for <code>/catalog/{id}</code>. Deletes the
	 * specified <code>Catalog</code> and all associated
	 * <code>CatalogEntry</code> items. Returns 204 on success. Returns 403 if
	 * the specified list is not owned by the current user.
	 * @param catalogId The catalog id.
	 * @return A {@link ResponseEntity} containing no body, only a status code.
	 */
	@RequestMapping(value = "{catalogId}", 
			method = RequestMethod.DELETE)
	public ResponseEntity<String> handleCatalogDestroyRequest(@PathVariable("catalogId") final Long catalogId) {

		final User user = userRightsService.getCurrentUser();
		final Catalog catalog = catalogDao.getById(catalogId);

		if (catalog != null) {
			if (!catalog.isCatalogOfUserWithId(user.getId())) {
				return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
			} else {
				catalogDao.deleteCatalog(catalog.getId());
			}
		}
		
		return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
	}
	
	/**
	 * Handles HTTP GET requests for 'catalog/list/{entityId}'.
	 * Returns the catalog entries connected to the given entity id.
	 * @param entityId An entity id.
	 * @return A {@link ResponseEntity} containing a list of {@link CatalogEntryExtended}.
	 */
	@RequestMapping(value = "list/{entityId}", 
			method = RequestMethod.GET,
			produces = CustomMediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<List<CatalogEntryExtended>> handleGetCatalogByEntityRequest(
			@PathVariable("entityId") final Long entityId) {
		
		final List<CatalogEntryExtended> result = catalogEntryDao.getEntryInfoByEntityId(entityId);
		if (result == null || result.isEmpty()) {
			return ResponseEntity.ok(new ArrayList<>());
		}
		return ResponseEntity.ok(result);
	}

}
