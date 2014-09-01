package de.uni_koeln.arachne.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to retrieve the current SessionFactory or create a new one if none exists.
 */
public class SessionUtil { // NOPMD
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionUtil.class);
	
    private static final SessionFactory SESSIONFACTORY;
    
    static {
    	try {
    		Configuration cfg = new Configuration();
    		cfg.configure();
    		
    		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties())
    				.build();
    		
    		SESSIONFACTORY = cfg.buildSessionFactory(serviceRegistry);
    	} catch (Exception ex) {
    		LOGGER.error("Initial SessionFactory creation failed." + ex);
    		throw new ExceptionInInitializerError(ex);
    	}
    }

    /**
     * Returns the current SessionFactory.
     * 
     * @return the current sessionFactory instance.
     */
    public static SessionFactory getSessionFactory() {
        return SESSIONFACTORY;
    }
}
