package de.uni_koeln.arachne.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.Catalog;
import de.uni_koeln.arachne.mapping.CatalogEntry;

@Repository("CatalogEntryDao")
public class CatalogEntryDao {
	
	@Autowired
    private transient SessionFactory sessionFactory;
	
	// ugly but needed for the 'getPublicCatalogIdsByEntityId'-workaround
	@Autowired
    private transient CatalogDao catalogDao;
	
	@Transactional(readOnly=true)
	public CatalogEntry getByCatalogEntryId(final long catalogEntryId) {
		final Session session = sessionFactory.getCurrentSession();
		return (CatalogEntry) session.get(CatalogEntry.class, catalogEntryId);
	}
	
	/**
	 * Gets a list of catalog identifiers that are connected to an entity. The list is in ascending order.
	 * @param entityId The entity identifier of interest.
	 * @return A list of catalog ids. 
	 */
	@Transactional(readOnly=true)
	@SuppressWarnings({ "PMD", "unchecked" })
	public List<Long> getPublicCatalogIdsByEntityId(final long entityId) {
		final List<Long> result = new ArrayList<Long>();
		// ugly native SQL workaround for mapping issues - but it should be faster :)
		final Session session = sessionFactory.getCurrentSession();
		final SQLQuery query = session.createSQLQuery(
				"SELECT catalog_id FROM arachne.catalog_entry WHERE arachne_entity_id = :entity_id");
		query.setParameter("entity_id", entityId);
		query.addScalar("catalog_id", LongType.INSTANCE);
		final List<Long> queryResult = query.list();
		if (!queryResult.isEmpty()) {
			for (Long catalogId : queryResult) {
				Catalog catalog = catalogDao.getByCatalogId(catalogId);
				if (catalog.isPublic()) {
					result.add(catalogId);
				}
			}
		}
		
		// TODO reenable when the mapping is fixed - currently the query sometimes works and sometimes does not
		// if it doesn't it throws a "null index column for collection"-exception
		/*final Session session = sessionFactory.getCurrentSession();
		final Criteria criteria = session.createCriteria(CatalogEntry.class);
		criteria.add(Restrictions.eq("arachneEntityId", entityId));
		criteria.setProjection(Projections.property("catalog"));
		criteria.addOrder(Order.asc("catalog.id"));
		
		for (Catalog catalog : (List<Catalog>) (List<?>) criteria.list()) {
			if (catalog.isPublic()) {
				result.add(catalog.getId());
			}
		}*/
		
		return result;
	}
	
	@Transactional
	public void deleteOrphanedCatalogEntries(final Catalog catalog) {
		List<Long> ids = new ArrayList<Long>();
		String querystring = "DELETE catalog_entry FROM catalog_entry LEFT JOIN catalog ON catalog_entry.catalog_id = "
				+ "catalog.id WHERE catalog.id = :catalogId";
		Query query;
		
		Session session = sessionFactory.getCurrentSession();
		if (catalog.getCatalogEntries() != null){
			for (CatalogEntry referenced : catalog.getCatalogEntries()){
				ids.add(referenced.getId());
			}
			query = session.createSQLQuery(querystring + " AND catalog_entry.id NOT IN (:ids)")
					.setLong("catalogId", catalog.getId())
					.setParameterList("ids", ids);
			
		}
		else {
			query = session.createSQLQuery(querystring)
					.setLong("catalogId", catalog.getId());
		}
		query.executeUpdate();
	}
	
	@Transactional
	public CatalogEntry updateCatalogEntry(final CatalogEntry catalogEntry) {
		Session session = sessionFactory.getCurrentSession();
		session.update(catalogEntry);
		return catalogEntry;
	}
	
	@Transactional
	public CatalogEntry saveCatalogEntry(final CatalogEntry catalogEntry) {
		Session session = sessionFactory.getCurrentSession();
		session.save(catalogEntry);
		return catalogEntry;
	}
	
	@Transactional
	public void deleteCatalogEntry(final CatalogEntry catalogEntry) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(catalogEntry);
	}

}
