package de.uni_koeln.arachne.dao;

import org.springframework.stereotype.Repository;

import de.uni_koeln.arachne.mapping.Session;

@Repository("SessionDao")
public class SessionDao extends HibernateTemplateDao {
	
	public Session findById(String sid) {
		return (Session) hibernateTemplate.get(Session.class, sid);
	}
	
	public Session saveSession(Session session) {
		hibernateTemplate.save(session);
		return session;
	}
	
	public int deleteAllSessionsForUser(long uid) {
		return hibernateTemplate.bulkUpdate("delete Session where uid = ?", uid);
	}
	
	public void deleteSession(String sid) {
		hibernateTemplate.bulkUpdate("delete Session where sid = ?", sid);
	}

}
