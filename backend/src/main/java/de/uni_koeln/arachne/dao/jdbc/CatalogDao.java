package de.uni_koeln.arachne.dao.jdbc;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mysql.jdbc.Statement;

import de.uni_koeln.arachne.dao.hibernate.ArachneEntityDao;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.service.UserRightsService;

@Repository("CatalogDao")
public class CatalogDao extends SQLDao {

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
		final String sql = "SELECT catalog.*, catalog_benutzer.uid "
				+ "FROM catalog "
				+ "LEFT JOIN catalog_benutzer "
				+ "ON id = catalog_id WHERE id = " 
				+ catalogId
				+ " AND "
				+ userRightsService.getSQL("Catalog");
		return queryForObject(sql, (rs, rowNum) -> {
			final Catalog catalog = new Catalog();
			catalog.setId(rs.getLong("id"));
			catalog.setRoot(catalogEntryDao.getById(rs.getLong("root_id"), full, limit, offset));
			catalog.setAuthor(rs.getString("author"));
			catalog.setPublic(rs.getBoolean("public"));
			catalog.setDatasetGroup(rs.getString("DatensatzGruppeCatalog"));
			final Set<Long> userIds = new HashSet<Long>();
			userIds.add(rs.getLong("uid"));
			catalog.setUserIds(userIds);
			return catalog;
		});
	}
	
	public List<Catalog> getByUserId(final long uid, final boolean full) {
		return getByUserId(uid, full, 0, 0);
	}
	
	@Transactional(readOnly=true)
	public List<Catalog> getByUserId(final long uid, final boolean full, final int limit, final int offset) {
		List<Catalog> result = query(con -> {
			final String sql = "SELECT catalog.*, catalog_benutzer.uid "
					+ "FROM catalog "
					+ "LEFT JOIN catalog_benutzer "
					+ "ON id = catalog_id WHERE uid = ? AND "
					+ userRightsService.getSQL("Catalog")
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
			final Set<Long> userIds = new HashSet<Long>();
			userIds.add(rs.getLong("uid"));
			catalog.setUserIds(userIds);
			return catalog;
		});
		
		return result;
	}
	/*
	@Transactional(readOnly=true)
	public Catalog getByUidAndCatalogId(final long uid, final long catalogId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("select c from Catalog c left join c.users u where u.id = :uid and c.id = :catalogId")
				.setLong("catalogId", catalogId)
				.setLong("uid", uid);
		return (Catalog) query.list().get(0);
	}
	*/
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
	/*
	@Transactional
	public Catalog saveOrUpdateCatalog(final Catalog catalog) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(catalog);
		return catalog;
	}

    @Transactional
    public void merge(final Catalog catalog) {
        Session session = sessionFactory.getCurrentSession();
        session.merge(catalog);
    }
	*/
	
}
