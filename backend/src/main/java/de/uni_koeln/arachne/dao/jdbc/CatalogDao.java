package de.uni_koeln.arachne.dao.jdbc;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mysql.jdbc.Statement;

import de.uni_koeln.arachne.dao.hibernate.ArachneEntityDao;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.service.UserRightsService;

@Repository("CatalogDao")
public class CatalogDao extends SQLDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(CatalogDao.class);
	
	@Autowired
	private transient CatalogEntryDao catalogEntryDao;
	
	@Autowired
	private transient ArachneEntityDao arachneEntityDao;
	
	private transient UserRightsService userRightsService;
	
	// needed to inject mock for testing - it should work without this setter but it does not
	@Autowired
	public void setUserRightsService(final UserRightsService userRightsService) {
		this.userRightsService = userRightsService;
	}
	
	public Catalog getById(final long catalogId) {
		return getById(catalogId, false, 0, 0);
	}

	/**
	 * Retrieve a catalog by Id.
	 * @param catalogId The id of the catalog.
	 * @param full Indicates if the full catalog (including all entries) or only the 'first level' of the catalog shall
	 * be retrieved. Defaults to <code>false</code>.
	 * @param limit If <code>full == false</code> then this parameter limits the children of the root entry.
	 * @param offset If <code>full == false</code> then this parameter is an offset into the children of the root entry.
	 * @return The catalog with the given id.
	 */
	@Transactional(readOnly=true)
	public Catalog getById(final long catalogId, final boolean full, final int limit, final int offset) {	
		final String sql = "SELECT * "
				+ "FROM catalog "
				+ "WHERE id = " 
				+ catalogId
				+ userRightsService.getSQL("catalog");
		return queryForObject(sql, (rs, rowNum) -> {
			final Catalog catalog = new Catalog();
			catalog.setId(rs.getLong("id"));
			catalog.setRoot(catalogEntryDao.getById(rs.getLong("root_id"), full, limit, offset));
			catalog.setAuthor(rs.getString("author"));
			catalog.setPublic(rs.getBoolean("public"));
			catalog.setDatasetGroup(rs.getString("DatensatzGruppeCatalog"));
			catalog.setUserIds(getUserIds(catalog.getId()));
			return catalog;
		});
	}
	
	private Set<Long> getUserIds(final long catalogId) {
		final String sql = "SELECT uid FROM catalog_benutzer WHERE catalog_id = " + catalogId;
		@SuppressWarnings("unchecked")
		final List<Long> uids = (List<Long>) queryForList(sql, Long.class);
		return new HashSet<Long>(uids);
	}

	public List<Catalog> getByUserId(final long uid, final boolean full) {
		return getByUserId(uid, full, 0, 0);
	}
	
	@Transactional(readOnly=true)
	public List<Catalog> getByUserId(final long uid, final boolean full, final int limit, final int offset) {
		List<Catalog> result = query(con -> {
			final String sql = "SELECT catalog.*, uid "
					+ "FROM catalog "
					+ "LEFT JOIN catalog_benutzer "
					+ "ON id = catalog_id "
					+ "WHERE uid = ?"
					+ userRightsService.getSQL("catalog")
					+ " ORDER BY id";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setLong(1, uid);
			return ps;
		}, (rs, rowNum) -> {
			final Catalog catalog = new Catalog();
			catalog.setId(rs.getLong("id"));
			catalog.setRoot(catalogEntryDao.getById(rs.getLong("root_id"), full, limit, offset));
			catalog.setAuthor(rs.getString("author"));
			catalog.setPublic(rs.getBoolean("public"));
			catalog.setDatasetGroup(rs.getString("DatensatzGruppeCatalog"));
			catalog.setUserIds(getUserIds(catalog.getId()));
			return catalog;
		});
		
		return result;
	}
	
	/**
	 * Gets a list containing public catalog identifiers and corresponding catalog paths that are connected to an 
	 * entity. The list is in ascending order.
	 * @param entityId The entity identifier of interest.
	 * @return A list of <code>Object[2]</code>. Id first, then path.
	 */
	public List<Object[]> getPublicCatalogIdsAndPathsByEntityId(final long entityId) {
		// TODO replace by custom query
		final List<Object[]> result = new ArrayList<Object[]>();
		for (final CatalogEntry catalogEntry : catalogEntryDao.getByEntityId(entityId)) {
			final Catalog catalog = getById(catalogEntry.getCatalogId()); 
			if (catalog.isPublic()) {
				result.add(new Object[] {catalog.getId(), catalogEntry.getPath()}); // NOPMD
			}
		}
		
		return result;
	}
	
	/**
	 * Gets a list of private catalog identifiers that are connected to an entity. The list is in ascending order.
	 * @param entityId The entity identifier of interest.
	 * @return A list of catalog ids. 
	 */
	@Transactional(readOnly=true)
	public List<Long> getPrivateCatalogIdsByEntityId(final long entityId) {
		// TODO replace by custom query
		final List<Long> result = new ArrayList<Long>();
		if (userRightsService.isSignedInUser()) {
			for (final CatalogEntry catalogEntry : catalogEntryDao.getByEntityId(entityId)) {
				final Catalog catalog = getById(catalogEntry.getCatalogId()); 
				if (!catalog.isPublic() && catalog.isCatalogOfUserWithId(userRightsService.getCurrentUser().getId())) {
					result.add(catalog.getId());
				}
			}
		}
		return result;
	}
	
	@Transactional
	public Catalog saveCatalog(final Catalog catalog) {
		CatalogEntry root = catalog.getRoot();
		catalog.setPublic(false);
		if (catalog.getId() == null && root != null && root.getId() == null && (root.getArachneEntityId() == null
				|| arachneEntityDao.getByEntityID(root.getArachneEntityId()) != null)) {
			
			try {
				catalog.setId(updateReturnKey(con -> {
					final String sql = "INSERT INTO catalog "
							+ "(author, public, DatensatzGruppeCatalog) "
							+ "VALUES "
							+ "(?, 0, ?)";
					PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
					ps.setString(1, catalog.getAuthor());
					ps.setString(2, catalog.getDatasetGroup());
					return ps;
				}));
			} catch (DataIntegrityViolationException e) {
				LOGGER.error(e.getMessage());
				return null;
			} 
			
			update(con -> {
				final String sql = "INSERT INTO catalog_benutzer "
						+ "(catalog_id, uid) "
						+ "VALUES "
						+ "(?, ?)";
				PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, catalog.getId());
				ps.setLong(2, userRightsService.getCurrentUser().getId());
				return ps;
			});
			
			root.setCatalogId(catalog.getId());
			root = catalogEntryDao.saveCatalogEntry(root);
			final long rootId = root.getId();
			
			update(con -> {
				final String sql = "UPDATE catalog "
						+ "SET root_id = ? "
						+ "WHERE "
						+ "catalog.id = ?";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setLong(1, rootId);
				ps.setLong(2, catalog.getId());
				return ps;
			});
			
			return catalog;
		}
		return null;
	}
	
	@Transactional
	public Catalog updateCatalog(final Catalog newCatalog) {
		if (newCatalog.getId() != null) {
			final Catalog oldCatalog = getById(newCatalog.getId());
			final long userId = userRightsService.getCurrentUser().getId();
			if (oldCatalog.isCatalogOfUserWithId(userId)) {
				// if no datasetGroup is given use the old one
				newCatalog.setDatasetGroup(newCatalog.getDatasetGroup() != null ? newCatalog.getDatasetGroup() 
						: oldCatalog.getDatasetGroup());
				update(con -> {
					final String sql = "UPDATE catalog "
							+ "SET author = ?, public = ?, DatensatzGruppeCatalog = ? "
							+ "WHERE "
							+ "catalog.id = ?";
					PreparedStatement ps = con.prepareStatement(sql);
					ps.setString(1, newCatalog.getAuthor());
					ps.setBoolean(2, newCatalog.isPublic());
					ps.setString(3, newCatalog.getDatasetGroup());
					ps.setLong(4, newCatalog.getId());
					return ps;
				});
				
				if (newCatalog.getUserIds() != null) {
					if (!oldCatalog.getUserIds().equals(newCatalog.getUserIds())) {
						update(con -> {
							final String sql = "DELETE FROM catalog_benutzer "
									+ "WHERE catalog_id = ?";
							PreparedStatement ps = con.prepareStatement(sql);
							ps.setLong(1, newCatalog.getId());
							return ps;
						});

						for (long uid : newCatalog.getUserIds()) {
							update(con -> {
								final String sql = "INSERT INTO catalog_benutzer "
										+ "(catalog_id, uid) "
										+ "VALUES "
										+ "(?, ?)";
								PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
								ps.setLong(1, newCatalog.getId());
								ps.setLong(2, uid);
								return ps;
							});
						}
					}
				} else {
					newCatalog.setUserIds(oldCatalog.getUserIds());
				}
				
				newCatalog.setRoot(oldCatalog.getRoot());
				return newCatalog;
			}
		}
		return null;
	}
	
	@Transactional
	public boolean deleteCatalog(final Long catalogId) throws DataAccessException {
		final Catalog catalog = getById(catalogId);
		if (catalog != null && catalog.isCatalogOfUserWithId(userRightsService.getCurrentUser().getId())) {
			catalogEntryDao.delete(catalog.getRoot().getId());

			final int updatedRows = update(con -> {
				final String sql = "DELETE FROM catalog WHERE id = ?";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setLong(1, catalogId);
				return ps;
			});

			return updatedRows == 1;
		}
		return false;
	}	
}
