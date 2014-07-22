package de.uni_koeln.arachne.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom replacement for the deprecated Spring HibernateTemplate to have similar functionality using hibernate 4.
 * Can be autowired as usual. 
 */
@Repository
public class HibernateTemplate { 
	
	@Autowired
    private transient SessionFactory sessionFactory;
	
	/**
	 * Execute a query for persistent instances.
	 * @param queryString A query expressed in Hibernate's query language.
	 * @return A list of the persistent instances.
	 */
	@Transactional(readOnly=true)
	public List<?> find(final String queryString) {
		return find(queryString, (Object[]) null);
	}
	
	/**
	 * Execute a query for persistent instances, binding one value to a "?".
	 * @param queryString A query expressed in Hibernate's query language.
	 * @return A list of the persistent instances.
	 */
	@Transactional(readOnly=true)
	public List<?> find(final String queryString, final Object value) {
		return find(queryString, new Object[] {value}) ;
	}
	
	/**
	 * Execute a query for persistent instances, binding a number of values to a "?".
	 * @param queryString A query expressed in Hibernate's query language.
	 * @return A list of the persistent instances.
	 */
	@Transactional(readOnly=true)
	public List<?> find(final String queryString, final Object[] values) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(queryString);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				query.setParameter(i, values[i]);
			}
		}
		List<?> result = query.list();
		return result;
	}
	
	/**
	 * Return the persistent instance of the given entity class with the given identifier.
	 * @param entityClass A persistent class.
	 * @param id An identifier of the persistent instance.
	 * @return The instance fetched from the db or null if not found.
	 */
	@Transactional(readOnly=true)
	public Object get(final Class<?> entityClass, final Serializable id) {
		Session session = sessionFactory.getCurrentSession();
		return session.get(entityClass, id);
	}
	
	/**
	 * Delete the given persistent instance.
	 * @param object The persistent instance to delete.
	 */
	@Transactional
	public void delete(final Object object) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(object);
	}

	/**
	 * Persist the given transient instance.
	 * @param object The transient instance to persist.
	 * @return The generated identifier.
	 */
	@Transactional
	public Serializable save(final Object object) {
		Session session = sessionFactory.getCurrentSession();
		return session.save(object);
	}

	/**
	 * Save or update the given persistent instance, according to its id (matching the configured "unsaved-value"?). 
	 * Associates the instance with the current Hibernate Session.
	 * @param object The persistent instance to save or update (to be associated with the Hibernate Session).
	 */
	@Transactional
	public void saveOrUpdate(final Object object) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(object);
	}
	
	/**
	 * Update the given persistent instance, associating it with the current Hibernate Session.
	 * @param object The persistent instance to update.
	 */
	@Transactional
	public void update(final Object object) {
		Session session = sessionFactory.getCurrentSession();
		session.update(object);
	}
	
	/**
	 * Update/delete all objects according to the given query, binding a number of values to "?" parameters in the 
	 * query string.
	 * @param queryString An update/delete query expressed in Hibernate's query language.
	 * @param value The value of the parameter.
	 * @return The number of instances updated/deleted.
	 */
	@Transactional
	public int bulkUpdate(final String queryString, final Object value) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(queryString);
		if (value != null) {
			query.setParameter(0, value);
		}
		return query.executeUpdate();
	}
}
