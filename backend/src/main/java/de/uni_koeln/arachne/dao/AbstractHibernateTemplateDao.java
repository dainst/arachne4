package de.uni_koeln.arachne.dao;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;


/**
 * This is the Base class for a Hibernate Dao, 
 * it initialises a <code>HibernateTemplate</code> with the right <code>DataSource</code> 
 * 
 * @author Rasmus Krempel
 *
 */
// In general abstract classes without abstract methods make no sense. But since this class should never be instantiated and just
// be used as a base class, making it abstract is an acceptable workaround.
public abstract class AbstractHibernateTemplateDao { 
	
	protected transient HibernateTemplate hibernateTemplate;
	/**
	 * Let the context inject the session Factory
	 * @param sessionFactory
	 */
	@Autowired
    public void setSessionFactory(final SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }
	
}
