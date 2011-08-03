package de.uni_koeln.arachne.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Utility class to retrieve the current SessionFactory or create a new one if none exists.
 */
public class SessionUtil {
	
    private static final SessionFactory sessionFactory;
    
    static {
    	try {
    		sessionFactory = new Configuration().configure().buildSessionFactory();
    	} catch (Throwable ex) {
    		System.err.println("Initial SessionFactory creation failed." + ex);
    		throw new ExceptionInInitializerError(ex);
    	}
    }

    /**
     * Returns the current SessionFactory.
     * 
     * @return the current sessionFactory instance.
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
