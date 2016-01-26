package de.uni_koeln.arachne.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.jdbc.Catalog;
import de.uni_koeln.arachne.mapping.jdbc.CatalogEntry;
import de.uni_koeln.arachne.service.UserRightsService;

@Repository("CatalogEntryDao")
public class CatalogEntryDao extends SQLDao {
	
	@Autowired
    private transient SessionFactory sessionFactory;
	
	@Autowired
	private transient UserRightsService userRightsService;
	
	@Transactional(readOnly=true)
	public CatalogEntry getById(final long catalogEntryId) {
		return getById(catalogEntryId, false, 0, 0);
	}
	
	/**
	 * Retrieves a CatalogEntry from the DB. The parameters are used to restrict the children.
	 * @param catalogEntryId The entries id.
	 * @param full If all children of all children should be retrieved or only the direct children of the entry.
	 * @param limit If <code>full = false</code> then limit restricts the number of direct children to the desired 
	 * value.
	 * @param offset If <code>full = false</code> and <code>limit > 0</code> then offset gives an offset into the 
	 * direct children list.
	 * @return The CatalogEntry with the given id.
	 */
	@Transactional(readOnly=true)
	public CatalogEntry getById(final long catalogEntryId, final boolean full, final int limit
			, final int offset) {
		final String sqlQuery = "SELECT * from catalog_entry WHERE id = " + catalogEntryId;
		if (full) {
			final CatalogEntry result = queryForObject(sqlQuery, this::mapCatalogEntryFull);
			return result;
		} else {
			final CatalogEntry result = queryForObject(sqlQuery, this::mapCatalogEntryDirectChildsOnly);
			// TODO find a way to limit the result set at query time
			if (offset > 0) {
				final List<CatalogEntry> children = result.getChildren();
				final int childCount = children.size();
				if (offset < childCount) {
					children.subList(0, offset).clear();
				}
			}
			if (limit > 0) {
				final List<CatalogEntry> children = result.getChildren();
				final int childCount = children.size();
				if (childCount > limit) {
					children.subList(limit, childCount).clear();
				}
			}
			return result;
		}
		/*final Session session = sessionFactory.getCurrentSession();
		CatalogEntry result = session.get(CatalogEntry.class, catalogEntryId);
		if (!full) {
			int count = 0;
			final List<CatalogEntry> children = result.getChildren();
			final ListIterator<CatalogEntry> it = children.listIterator();
			while (it.hasNext()) {
				CatalogEntry catalogEntry = (CatalogEntry) it.next();
				count++;
				if (limit > 0 && (count <= offset || limit + offset < count)) {
					it.remove();
				} else {
					//catalogEntry.removeChildren();
				}
			}
		}
		return result;*/
	}
	
	@Transactional(readOnly=true)
	public List<CatalogEntry> getChildrenByParentId(final long parentId, final RowMapper<CatalogEntry> rowMapper) {
		final String sqlQuery = "SELECT * from catalog_entry WHERE parent_id = " + parentId + " ORDER BY index_parent";
		List<CatalogEntry> result = query(sqlQuery, rowMapper);
		if (result != null && result.isEmpty()) {
			result = null;
		}
		return result;
	}
	
	@Transactional(readOnly=true)
	public int getChildrenSizeByParentId(final long parentId) {
		final String sqlQuery = "SELECT COUNT(*) from catalog_entry WHERE parent_id = " + parentId;
		final Integer result = queryForInt(sqlQuery);
		return result;
	}
	
	private CatalogEntry mapCatalogEntryDirectChildsOnly(ResultSet rs, int rowNum) throws SQLException {
		final CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(rs.getLong("id"));
		catalogEntry.setCatalogId(rs.getLong("catalog_id"));
		catalogEntry.setLabel(rs.getString("label"));
		catalogEntry.setChildren(getChildrenByParentId(catalogEntry.getId(), this::mapCatalogEntryNoChilds));
		return catalogEntry;
	}
	
	private CatalogEntry mapCatalogEntryFull(ResultSet rs, int rowNum) throws SQLException {
		final CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(rs.getLong("id"));
		catalogEntry.setCatalogId(rs.getLong("catalog_id"));
		catalogEntry.setLabel(rs.getString("label"));
		catalogEntry.setChildren(getChildrenByParentId(catalogEntry.getId(), this::mapCatalogEntryFull));
		return catalogEntry;
	}
	
	private CatalogEntry mapCatalogEntryNoChilds(ResultSet rs, int rowNum) throws SQLException {
		final CatalogEntry catalogEntry = new CatalogEntry();
		catalogEntry.setId(rs.getLong("id"));
		catalogEntry.setCatalogId(rs.getLong("catalog_id"));
		catalogEntry.setLabel(rs.getString("label"));
		catalogEntry.setHasChildren(getChildrenSizeByParentId(catalogEntry.getId()) > 0);
		return catalogEntry;
	}
	
	/**
	 * Gets a list containing public catalog identifiers and corresponding catalog paths that are connected to an 
	 * entity. The list is in ascending order.
	 * @param entityId The entity identifier of interest.
	 * @return A list of <code>Object[2]</code>. Id first, then path.
	 */
	@Transactional(readOnly=true)
	public List<Object[]> getPublicCatalogIdsAndPathsByEntityId(final long entityId) {
		final List<Object[]> result = new ArrayList<Object[]>();
		for (final CatalogEntry catalogEntry : getByEntityId(entityId)) {
			final Catalog catalog = catalogEntry.getCatalog(); 
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
		final List<Long> result = new ArrayList<Long>();
		if (userRightsService.isSignedInUser()) {
			for (final CatalogEntry catalogEntry : getByEntityId(entityId)) {
				final Catalog catalog = catalogEntry.getCatalog(); 
				if (!catalog.isPublic() && catalog.isCatalogOfUserWithId(userRightsService.getCurrentUser().getId())) {
					result.add(catalog.getId());
				}
			}
		}
		return result;
	}
	
	@Transactional(readOnly=true)
	private List<CatalogEntry> getByEntityId(final long entityId) {
		final Session session = sessionFactory.getCurrentSession();
		final Criteria criteria = session.createCriteria(CatalogEntry.class);
		criteria.add(Restrictions.eq("arachneEntityId", entityId));
		@SuppressWarnings("unchecked")
		List<CatalogEntry> result = criteria.list();
		return result;
	}
	
	@Transactional
	public void deleteOrphanedCatalogEntries(final Catalog catalog) {
		final List<Long> catalogEntryIds = new ArrayList<Long>();
		final String querystring = "DELETE catalog_entry FROM catalog_entry LEFT JOIN catalog ON catalog_entry.catalog_id = "
				+ "catalog.id WHERE catalog.id = :catalogId";
		Query query;
		
		final Session session = sessionFactory.getCurrentSession();
		if (catalog.getCatalogEntries() != null) {
			for (final CatalogEntry referenced : catalog.getCatalogEntries()){
				catalogEntryIds.add(referenced.getId());
			}
			query = session.createSQLQuery(querystring + " AND catalog_entry.id NOT IN (:ids)")
					.setLong("catalogId", catalog.getId())
					.setParameterList("ids", catalogEntryIds);
			
		} else {
			query = session.createSQLQuery(querystring)
					.setLong("catalogId", catalog.getId());
		}
		query.executeUpdate();
	}
	
	@Transactional
	public CatalogEntry updateCatalogEntry(final CatalogEntry catalogEntry) throws HibernateException {
		final Session session = sessionFactory.getCurrentSession();
		session.update(catalogEntry);
		return catalogEntry;
	}
	
	@Transactional
	public CatalogEntry saveCatalogEntry(final CatalogEntry catalogEntry) throws HibernateException {
		final Session session = sessionFactory.getCurrentSession();
		session.save(catalogEntry);
		return catalogEntry;
	}
	
	@Transactional
	public void deleteCatalogEntry(final CatalogEntry catalogEntry) throws HibernateException {
		final Session session = sessionFactory.getCurrentSession();
		session.delete(catalogEntry);
	}

}
