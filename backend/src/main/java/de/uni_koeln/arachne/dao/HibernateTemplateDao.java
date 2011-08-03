package de.uni_koeln.arachne.dao;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;



public abstract class HibernateTemplateDao {
	protected HibernateTemplate hibernateTemplate;
	
	@Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }
	
}
