package de.uni_koeln.arachne.dao.hibernate;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.hibernate.ResetPasswordRequest;

/**
 * Data access object for {@link de.uni_koeln.arachne.mapping.hibernate.ResetPasswordRequest}.
 * @author Reimar Grabowski
 *
 */
@Repository
public class ResetPasswordRequestDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResetPasswordRequest.class);

	@Autowired
    private transient SessionFactory sessionFactory;
	
	@SuppressWarnings({ "PMD", "unchecked" })
	@Transactional(readOnly=true)
	public ResetPasswordRequest getByToken(final String token) {
		final Session session = sessionFactory.getCurrentSession();
		final Criteria criteria = session.createCriteria(ResetPasswordRequest.class);
		criteria.add(Restrictions.eq("token", token));
		final List<ResetPasswordRequest> queryResult = criteria.list();
		if (!queryResult.isEmpty()) {
			return queryResult.get(0);
		}
		return null;
	}
	
	@Transactional
	public ResetPasswordRequest saveOrUpdate(final ResetPasswordRequest resetPasswordRequest) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(resetPasswordRequest);
		return resetPasswordRequest;
	}
	
	@Transactional
	public ResetPasswordRequest save(final ResetPasswordRequest resetPasswordRequest) {
		Session session = sessionFactory.getCurrentSession();
		session.save(resetPasswordRequest);
		return resetPasswordRequest;
	}
	
	@Transactional
	public void delete(final ResetPasswordRequest resetPasswordRequest) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(resetPasswordRequest);
	}
	
	// TODO use scheduler to run periodically
	@SuppressWarnings({ "PMD", "unchecked" })
	@Transactional
	public void deleteExpiredRequests() {
		Session session = sessionFactory.getCurrentSession();
		final Calendar calender = Calendar.getInstance();
		final Timestamp now = new Timestamp(calender.getTime().getTime());
		final Criteria criteria = session.createCriteria(ResetPasswordRequest.class);
		criteria.add(Restrictions.lt("expiration_date", now));
		final List<ResetPasswordRequest> queryResult = criteria.list();
		for (final ResetPasswordRequest resetPasswordRequest : queryResult) {
			session.delete(resetPasswordRequest);
			LOGGER.info("Deleted expired reset password request " + resetPasswordRequest.getId());
		}
	}
}
