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
public abstract class HibernateTemplateDao {
	protected HibernateTemplate hibernateTemplate;
	/**
	 * Let the context injet the session Factory
	 * @param sessionFactory
	 */
	@Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }
	
}
