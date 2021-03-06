package de.uni_koeln.arachne.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.hibernate.DatasetGroup;
import de.uni_koeln.arachne.mapping.hibernate.User;

/**
 * Hibernate DAO for the 'verwaltung_benutzer' table.
 * @author Reimar Grabowski
 */
@Repository("UserDao")
public class UserDao {

	@Autowired
    private transient SessionFactory sessionFactory;
	
	@Transactional(readOnly=true)
	public User findById(final long uid) {
		Session session = sessionFactory.getCurrentSession();
		return (User) session.get(User.class, uid);
	}

	@Transactional(readOnly=true)
	public User findByName(final String user) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from User as user WHERE user.username LIKE :user")
				.setString("user", user);
		
		User result = null;
		List<?> queryResult = query.list();
		if (!queryResult.isEmpty()) {
			result = (User)queryResult.get(0);
		}
		return result;
	}
	
	@Transactional(readOnly=true)
	public User findByEMailAddress(final String eMailAddress) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from User as user WHERE user.email LIKE :eMailAddress")
				.setString("eMailAddress", eMailAddress);
		
		User result = null;
		List<?> queryResult = query.list();
		if (!queryResult.isEmpty()) {
			result = (User)queryResult.get(0);
		}
		return result;
	}
	
	@Transactional(readOnly=true)
	public DatasetGroup findDatasetGroupByName(final String name) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from DatasetGroup as group WHERE group.name LIKE :name")
				.setString("name", name);
		return (DatasetGroup) query.uniqueResult();
	}
	
	@Transactional
	public User updateUser(final User user) {
		if(user != null) {
			Session session = sessionFactory.getCurrentSession();
			session.update(user);
		}
		return user;
	}
	
	@Transactional
	public void createUser(final User user) {
		Session session = sessionFactory.getCurrentSession();
		session.save(user);
	}

	@Transactional
	public void deleteUser(final User user) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(user);
	}
}
