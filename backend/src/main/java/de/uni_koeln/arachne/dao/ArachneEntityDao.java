package de.uni_koeln.arachne.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import de.uni_koeln.arachne.mapping.ArachneEntity;

@Repository("ArachneEntityDao")
public class ArachneEntityDao extends AbstractHibernateTemplateDao {

	/**
	 * Retrieves alternative Identifiers by Arachne Entity ID
	 * @param ArachneEntityID The Arachne Entity ID
	 * @return Returns a Instance of the Arachne Entity Table Mapping
	 */
	public ArachneEntity getByEntityID(final Long ArachneEntityID) {
		return (ArachneEntity) hibernateTemplate.get(ArachneEntity.class, ArachneEntityID);
	}
	
	/**
	 * Retrieves alternative Identifiers by table and Table key
	 * @param table Arachne Table name
	 * @param internalId Primary Key of the Table
	 * @return Returns a Instance of the Arachne Entity Table Mapping
	 */
	public ArachneEntity getByTablenameAndInternalKey(final String table, final Long internalId) {
		@SuppressWarnings("unchecked")
		final List<ArachneEntity> list =  (List<ArachneEntity>) hibernateTemplate.find(
				"from ArachneEntity where ForeignKey like "+internalId+" and TableName like '"+table+"'" );
		if (list.isEmpty()) {
			return null;
		} else {
			return (ArachneEntity) list.get(0);
		}
	}
	
	/**
	 * Retrieves alternative Identifiers by range of primary keys.
	 * @param start First id in the range.
	 * @param end Last id in the range.
	 * @return Returns a List of Arachne Entity Table Mappings.
	 */
	public List<ArachneEntity> getByEntityIdRange(final long start, final long end) {
		long startId;
		long endId;
		if (start>end) {
			startId = end;
			endId = start;
		} else {
			startId = start;
			endId = end;
		}
		@SuppressWarnings("unchecked")
		final List<ArachneEntity> list = (List<ArachneEntity>) hibernateTemplate.find(
				"from ArachneEntity where ArachneEntityID <= "+endId+" and ArachneEntityID >= '"+startId+"'" );
		if (list.isEmpty()) {
			return null;
		} else {
			return list;
		}
	}
}
