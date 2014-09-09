package de.uni_koeln.arachne.dao;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.Session;

@Repository("SessionDao")
public class SessionDao {
	
	@Autowired
    private transient SessionFactory sessionFactory;
	
	@Transactional(readOnly=true)
	public Session findById(final String sid) {
		org.hibernate.Session session = sessionFactory.getCurrentSession();
		return (Session) session.get(Session.class, sid);
	}
	
	@Transactional
	public Session saveSession(final Session session) {
		org.hibernate.Session hibernateSession = sessionFactory.getCurrentSession();
		hibernateSession.save(session);
		return session;
	}
	
	@Transactional
	public int deleteAllSessionsForUser(final long uid) {
		org.hibernate.Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("delete Session where uid = :uid")
				.setLong("uid", uid);
		return query.executeUpdate();
	}
	
	@Transactional
	public void deleteSession(final String sid) {
		org.hibernate.Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("delete Session where sid = :sid")
				.setString("sid", sid);
		query.executeUpdate();
	}
}
