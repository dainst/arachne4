package de.uni_koeln.arachne.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.CatalogHeading;

@Repository("CatalogHeadingDao")
public class CatalogHeadingDao {
	
	@Autowired
    private transient SessionFactory sessionFactory;
	
	@Transactional(readOnly=true)
	public CatalogHeading getByCatalogHeadingId(final long catalogHeadingId) {
		Session session = sessionFactory.getCurrentSession();
		return (CatalogHeading) session.get(CatalogHeading.class, catalogHeadingId);
	}
	
	@Transactional
	public CatalogHeading updateCatalogHeading(final CatalogHeading catalogHeading) {
		Session session = sessionFactory.getCurrentSession();
		session.update(catalogHeading);
		return catalogHeading;
	}
	
	@Transactional
	public CatalogHeading saveCatalogHeading(final CatalogHeading catalogHeading) {
		Session session = sessionFactory.getCurrentSession();
		session.save(catalogHeading);
		return catalogHeading;
	}
	
	@Transactional
	public void deleteCatalogHeading(final CatalogHeading catalogHeading) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(catalogHeading);
	}

}
