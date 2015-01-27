package de.uni_koeln.arachne.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.Catalog;
import de.uni_koeln.arachne.mapping.CatalogEntry;

@Repository("CatalogEntryDao")
public class CatalogEntryDao {
	
	@Autowired
    private transient SessionFactory sessionFactory;
	
	@Transactional(readOnly=true)
	public CatalogEntry getByCatalogEntryId(final long catalogEntryId) {
		Session session = sessionFactory.getCurrentSession();
		return (CatalogEntry) session.get(CatalogEntry.class, catalogEntryId);
	}
	
	@Transactional(readOnly=true)
	public Set<CatalogEntry> getOrphanedCatalogEntries(final Catalog catalog) {
		Set<CatalogEntry> orphans = new HashSet<CatalogEntry>();
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("select c from CatalogEntry c left join c.catalog cat where cat.id = :catalogId")
				.setLong("catalogId", catalog.getId());
		@SuppressWarnings("unchecked")
		List<CatalogEntry> result = query.list();
		for (CatalogEntry entry : result){
			boolean found = false;
			for (CatalogEntry referenced : catalog.getCatalogEntries()){
				if (referenced.getId() == entry.getId()){
					found = true;
					break;
				}
			}
			if (!found){
				orphans.add(entry);
			}
		}
		return orphans;
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
