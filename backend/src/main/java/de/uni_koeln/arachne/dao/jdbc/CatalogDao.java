package de.uni_koeln.arachne.dao.jdbc;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_koeln.arachne.util.sql.CatalogEntryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mysql.jdbc.Statement;

import de.uni_koeln.arachne.dao.hibernate.ArachneEntityDao;
import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.service.UserRightsService;

/**
 * JDBC based data access object for catalogs.
 * 
 * @author Karen Schwane
 * @author Reimar Grabowski
 * @author Jan G. Wieners
 * 
 */
@Repository("CatalogDao")
public class CatalogDao extends SQLDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogDao.class);

    @Autowired
    private transient CatalogEntryDao catalogEntryDao;

    @Autowired
    private transient ArachneEntityDao arachneEntityDao;

    private transient UserRightsService userRightsService;

    /**
     * Injects the user rights service.<br>
     * This is needed to inject a mock for testing. It should work without this setter (by autowiring the property) but 
     * it does not (bug in Spring).
     * @param userRightsService The {@link UserRightsService}.
     */
    @Autowired
    public void setUserRightsService(final UserRightsService userRightsService) {
        this.userRightsService = userRightsService;
    }

    /**
     * Convenience method to retrieve a catalog by id. It retrieves only the 'first level' (root and it's direct 
     * children).
     * @param catalogId The catalog id.
     * @return The catalog.
     */
    public Catalog getById(final long catalogId) {
        return getById(catalogId, false, -1, 0);
    }

    /**
     * Retrieves a catalog by Id.
     *
     * @param catalogId The id of the catalog.
     * @param full      Indicates if the full catalog (including all entries) or only the 'first level' of the catalog shall
     *                  be retrieved. Defaults to <code>false</code>.
     * @param limit     If <code>full == false</code> then this parameter limits the children of the root entry (-1 for
     *                  no limit).
     * @param offset    If <code>full == false</code> then this parameter is an offset into the children of the root entry.
     * @return The catalog with the given id.
     */
    @Transactional(readOnly = true)
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

    /**
     * Retrieves catalogs by user Id.
     *
     * @param uid       The user id.
     * @param full      Indicates if the full catalog (including all entries) or only the 'first level' of the catalog shall
     *                  be retrieved. Defaults to <code>false</code>.
     * @param limit     If <code>full == false</code> then this parameter limits the children of the root entry (-1 for
     *                  no limit).
     * @param offset    If <code>full == false</code> then this parameter is an offset into the children of the root entry.
     * @return A list of catalogs belonging to the given user id.
     */
    @Transactional(readOnly = true)
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
     *
     * @param entityId The entity identifier of interest.
     * @return A list of <code>Object[2]</code>. Id first, then path.
     */
    @Transactional(readOnly = true)
    public List<CatalogEntryInfo> getPublicCatalogIdsAndPathsByEntityId(final long entityId) {
        final List<CatalogEntryInfo> result = query(con -> {
            final String sql = "SELECT catalog_id, catalog_entry.id, path, catalog.public "
                    + "FROM catalog_entry "
                    + "LEFT JOIN catalog "
                    + "ON catalog_id = catalog.id "
                    + "WHERE arachne_entity_id = ? "
                    + "AND public = 1 "
                    + userRightsService.getSQL("catalog")
                    + " ORDER BY catalog_id";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, entityId);
            return ps;
        }, (rs, rowNum) -> {
            return new CatalogEntryInfo(rs.getLong("catalog_id"), rs.getString("path"), rs.getLong("id"));
        });

        return result;
    }

    /**
     * Gets a list of private catalog identifiers that are connected to an entity. The list is in ascending order.
     *
     * @param entityId The entity identifier of interest.
     * @return A list of catalog ids.
     */
    @Transactional(readOnly = true)
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

    /**
     * Persists a catalog in the DB.
     * @param catalog The catalog to store.
     * @return The stored catalog retrieved from the DB or <code>null</code> if it could not be created.
     */
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

            root = saveEntryRecursive(root, catalog.getId());
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

    private CatalogEntry saveEntryRecursive(final CatalogEntry entry, final long catalogId) {
        entry.setCatalogId(catalogId);
        catalogEntryDao.saveCatalogEntry(entry);
        CatalogEntry child;
        if (entry.getChildren() != null && entry.getChildren().size() > 0) {
            for (int i = 0; i < entry.getChildren().size(); i++) {
                child = entry.getChildren().get(i);
                child.setIndexParent(i);
                child.setParentId(entry.getId());
                saveEntryRecursive(child, catalogId);
            }
        }
        return entry;
    }

    /**
     * Updates a catalog in the DB.
     * @param newCatalog The updated catalog.
     * @return The updated catalog retrieved from the DB.
     */
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

    /**
     * Deletes a catalog in the DB.
     * @param catalogId The id of the catalog.
     * @return if the catalog could be deleted.
     */
    @Transactional
    public boolean deleteCatalog(final Long catalogId) {
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
