package de.uni_koeln.arachne.dao.hibernate;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.hibernate.Catalog;
import de.uni_koeln.arachne.mapping.hibernate.CatalogEntry;

@Repository("CatalogDao")
public class CatalogDao {

	@Autowired
    private transient SessionFactory sessionFactory;
	
	@Transactional(readOnly=true)
	public Catalog getByCatalogId(final long catalogId) {
		Session session = sessionFactory.getCurrentSession();
		return eagerFetch((Catalog) session.get(Catalog.class, catalogId));
	}
	
	@Transactional(readOnly=true)
	public List<Catalog> getByUid(final long uid) {
		Session session = sessionFactory.getCurrentSession();		
		Query query = session.createQuery("select c from Catalog c join c.users u where u.id = :uid")		
				.setLong("uid", uid);
		
		@SuppressWarnings("unchecked")
		List<Catalog> result = (List<Catalog>) query.list();
		if (result.size() < 1) {
			result = null;
		} else {
			for (Catalog catalog : result) {
				eagerFetch(catalog);
			}
		}
		return result;
	}
	
	@Transactional(readOnly=true)
	public Catalog getByUidAndCatalogId(final long uid, final long catalogId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("select c from Catalog c left join c.users u where u.id = :uid and c.id = :catalogId")
				.setLong("catalogId", catalogId)
				.setLong("uid", uid);
		return eagerFetch((Catalog) query.list().get(0));
	}
	
	@Transactional
	public Catalog saveOrUpdateCatalog(final Catalog catalog) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(catalog);
		return eagerFetch(catalog);
	}
	
	@Transactional
	public Catalog saveCatalog(final Catalog catalog) {
		Session session = sessionFactory.getCurrentSession();
		session.save(catalog);
		return eagerFetch(catalog);
	}
	
	@Transactional
	public void destroyCatalog(final Catalog catalog) {
		Session session = sessionFactory.getCurrentSession();
		// hack: get catalog by ID in order to prevent NonUniqueObjectException
		Object attachedCatalog = session.get(Catalog.class, catalog.getId());
		session.delete(attachedCatalog);
	}
	
	/**
	 * Enforce (recursive) eager loading of all connected objects.
	 * Can be used to circumvent org.hibernate.LazyInitializationException.
	 * @param catalog The catalog to be loaded
	 * @return The catalog with all connected objects fetched.
	 */
	private Catalog eagerFetch(Catalog catalog) {
		Hibernate.initialize(catalog.getUsers());
		eagerFetchChildren(catalog.getRoot());
		return catalog;
	}

	private void eagerFetchChildren(CatalogEntry entry) {
		if (entry != null && entry.getChildren() != null) {
			Hibernate.initialize(entry.getChildren());
			for (CatalogEntry child : entry.getChildren()) {
				eagerFetchChildren(child);
			}
		}
	}
	
}
