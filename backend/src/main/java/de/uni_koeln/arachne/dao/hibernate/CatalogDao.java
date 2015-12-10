package de.uni_koeln.arachne.dao.hibernate;

import java.util.List;
import java.util.ListIterator;

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
		return getByCatalogId(catalogId, true, 0, 0);
	}
	
	/**
	 * Retrieve a catalog by Id.
	 * @param catalogId The id of the catalog.
	 * @param full Indicates if the full catalog (including all entries) or only the 'first level' of the catalog shall
	 * be retrieved. Defaults to <code>false</code>.
	 * @return The catalog with the given id.
	 */
	@Transactional(readOnly=true)
	public Catalog getByCatalogId(final long catalogId, final boolean full, final int limit, final int offset) {
		Session session = sessionFactory.getCurrentSession();
		Catalog result = null;
		if (full) {
			return eagerFetch((Catalog) session.get(Catalog.class, catalogId));
		} else {
			// a customized query might be more efficient but currently this is fast enough
			final Catalog catalog = (Catalog) session.get(Catalog.class, catalogId);
			if (catalog != null) {
				int count = 0;
				final CatalogEntry root = catalog.getRoot();
				final List<CatalogEntry> children = root.getChildren();
				final ListIterator<CatalogEntry> it = children.listIterator();
				while (it.hasNext()) {
					CatalogEntry catalogEntry = (CatalogEntry) it.next();
					count++;
					if (limit > 0 && (count <= offset || limit + offset < count)) {
						it.remove();
					} else {
						catalogEntry.removeChildren();
					}
				}
				// TODO investigate if deep copying is really necessary
				result = new Catalog();
				result.setAuthor(catalog.getAuthor());
				result.setId(catalog.getId());
				result.setPublic(catalog.isPublic());
				result.setRoot(root);
				result.setUsers(catalog.getUsers());
			}
			return result;
		}
	}
	
	@Transactional(readOnly=true)
	public List<Catalog> getByUid(final long uid, final boolean full) {
		Session session = sessionFactory.getCurrentSession();		
		Query query = session.createQuery("select c from Catalog c join c.users u where u.id = :uid")		
				.setLong("uid", uid);
		
		@SuppressWarnings("unchecked")
		List<Catalog> result = (List<Catalog>) query.list();
		if (result.size() < 1) {
			result = null;
		} else {
			if (!full) {
				for (final Catalog catalog: result) {
					catalog.getRoot().removeChildren();
				}
			} else {
				for (Catalog catalog : result) {
					eagerFetch(catalog);
				}
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
	@Transactional
	private Catalog eagerFetch(Catalog catalog) {
		Hibernate.initialize(catalog.getUsers());
		eagerFetchChildren(catalog.getRoot());
		return catalog;
	}

	@Transactional
	private void eagerFetchChildren(CatalogEntry entry) {
		if (entry != null && entry.getChildren() != null) {
			Hibernate.initialize(entry.getChildren());
			for (CatalogEntry child : entry.getChildren()) {
				eagerFetchChildren(child);
			}
		}
	}
	
}
