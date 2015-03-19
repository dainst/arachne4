package de.uni_koeln.arachne.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.uni_koeln.arachne.mapping.ArachneEntity;

@Repository("ArachneEntityDao")
public class ArachneEntityDao {

	@Autowired
	private transient SessionFactory sessionFactory;
	
	/**
	 * Retrieves alternative Identifiers by Arachne Entity ID
	 * @param ArachneEntityID The Arachne Entity ID
	 * @return Returns a Instance of the Arachne Entity Table Mapping
	 */
	@Transactional(readOnly=true)
	public ArachneEntity getByEntityID(final long arachneEntityID) {
		final Session session = sessionFactory.getCurrentSession();
		return (ArachneEntity) session.get(ArachneEntity.class, arachneEntityID);
	}
	
	/**
	 * Retrieves alternative Identifiers by table and table key
	 * @param tableName Arachne table name
	 * @param internalId Primary key of the table
	 * @return Returns a Instance of the ArachneEntity mapping
	 */
	@Transactional(readOnly=true)
	public ArachneEntity getByTablenameAndInternalKey(final String tableName, final long internalId) {
		final Session session = sessionFactory.getCurrentSession();
		final Criteria criteria = session.createCriteria(ArachneEntity.class);
		criteria.add(Restrictions.eq("foreignKey", internalId));
		criteria.add(Restrictions.like("tableName", tableName));
		return (ArachneEntity) criteria.uniqueResult();
	}
	
	/**
	 * Retrieves alternative Identifiers by range of primary keys.
	 * @param start First id in the range.
	 * @param limit Maximum number of ids.
	 * @return Returns a list of <code>ArachneEntity</code> mappings which may be empty.
	 */
	@Transactional(readOnly=true)
	public List<ArachneEntity> getByLimitedEntityIdRange(final long startId, final int limit) {
		final Session session = sessionFactory.getCurrentSession();
		final Criteria criteria = session.createCriteria(ArachneEntity.class);
		criteria.add(Restrictions.gt("entityId", startId));
		criteria.addOrder(Order.asc("entityId"));
		criteria.setMaxResults(limit);
				
		@SuppressWarnings("unchecked")
		final List<ArachneEntity> list = (List<ArachneEntity>) criteria.list();
				
		if (list.isEmpty()) {
			return new ArrayList<ArachneEntity>();
		} else {
			return list;
		}
	}
}
