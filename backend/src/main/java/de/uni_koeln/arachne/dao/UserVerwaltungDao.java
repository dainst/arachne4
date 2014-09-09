package de.uni_koeln.arachne.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.UserAdministration;
import de.uni_koeln.arachne.util.RegisterFormValidationUtil;


@Repository("UserVerwaltungDao")
public class UserVerwaltungDao {

	@Autowired
    private transient SessionFactory sessionFactory;
	
	@Transactional(readOnly=true)
	public UserAdministration findById(final long uid) {
		Session session = sessionFactory.getCurrentSession();
		return (UserAdministration) session.get(UserAdministration.class, uid);
	}

	@Transactional(readOnly=true)
	public UserAdministration findByName(final String user) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from UserAdministration as user WHERE user.username LIKE :user")
				.setString("user", user);
		
		UserAdministration result = null;
		List<?> queryResult = query.list();
		if (queryResult.size() > 0) {
			result = (UserAdministration)queryResult.get(0);
		}
		return result;
	}
	
	/**
	 * Method to find an user by the email-authentification token
	 * @param token
	 * @return
	 */
	@Transactional(readOnly=true)
	public UserAdministration findByAuthToken(final String token) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from UserAdministration as user WHERE user.emailAuth LIKE :token")
				.setString("token", token);
		UserAdministration result = null;
		List<?> queryResult = query.list();
		if (queryResult.size() > 0) {
			result = (UserAdministration)queryResult.get(0);
		}
		return result;
	}
	
	@Transactional
	public UserAdministration updateUser(final UserAdministration user) {
		if(user != null) {
			Session session = sessionFactory.getCurrentSession();
			session.update(user);
		}
		return user;
	}
	
	/**
	 * Method to create a new User-Account in the database
	 * @param user
	 * @return
	 */
	@Transactional
	public boolean newUser(RegisterFormValidationUtil user) {
		Session session = sessionFactory.getCurrentSession();
		Serializable serializable = session.save(user);
		
		if(serializable != null) {
			return true;
		}
		
		return false;
	}
}
