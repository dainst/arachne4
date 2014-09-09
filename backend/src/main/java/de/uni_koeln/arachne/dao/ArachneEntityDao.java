package de.uni_koeln.arachne.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
		Session session = sessionFactory.getCurrentSession();
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
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from ArachneEntity where ForeignKey like :internalId and TableName like :tableName")
				.setLong("internalId", internalId)
				.setString("tableName", tableName);
						
		@SuppressWarnings("unchecked")
		final List<ArachneEntity> list = (List<ArachneEntity>) query.list();
		
		if (list.isEmpty()) {
			return null;
		} else {
			return (ArachneEntity) list.get(0);
		}
	}
	
	/**
	 * Retrieves alternative Identifiers by range of primary keys.
	 * @param start First id in the range.
	 * @param limit Maximum number of ids.
	 * @return Returns a List of Arachne Entity Table Mappings.
	 */
	@Transactional(readOnly=true)
	public List<ArachneEntity> getByLimitedEntityIdRange(final long startId, final int limit) {
		
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("from ArachneEntity")
				.setFirstResult((int)startId)
				.setMaxResults(limit);
				
		@SuppressWarnings("unchecked")
		final List<ArachneEntity> list = (List<ArachneEntity>) query.list();
				
		if (list.isEmpty()) {
			return null;
		} else {
			return list;
		}
	}
}
