package de.uni_koeln.arachne.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.Session;

@Repository("SessionDao")
public class SessionDao {
	
	@Autowired
	private transient HibernateTemplate hibernateTemplate;
	
	public Session findById(final String sid) {
		return (Session) hibernateTemplate.get(Session.class, sid);
	}
	
	public Session saveSession(final Session session) {
		hibernateTemplate.save(session);
		return session;
	}
	
	public int deleteAllSessionsForUser(final long uid) {
		return hibernateTemplate.bulkUpdate("delete Session where uid = ?", uid);
	}
	
	public void deleteSession(final String sid) {
		hibernateTemplate.bulkUpdate("delete Session where sid = ?", sid);
	}

}
