package de.uni_koeln.arachne.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import de.uni_koeln.arachne.mapping.ArachneEntity;

@Repository("ArachneEntityDao")
public class ArachneEntityDao extends AbstractHibernateTemplateDao {

	/**
	 * Retrives alternative Identifierys by Arachne Entity ID
	 * @param ArachneEntityID The Arachne Entity ID
	 * @return Returns a Instance of the Arachne Entity Table Mapping
	 */
	public ArachneEntity getByEntityID(Long ArachneEntityID){
		return (ArachneEntity) hibernateTemplate.get(ArachneEntity.class, ArachneEntityID);
	}
	
	/**
	 * Retrives alternative Identifierys by table and Table key
	 * @param table Arachne Table name
	 * @param id Primary Key of the Table
	 * @return Returns a Instance of the Arachne Entity Table Mapping
	 */
	public ArachneEntity getByTablenameAndInternalKey(String table, Long id){
		@SuppressWarnings("unchecked")
		List<ArachneEntity> liste =  (List<ArachneEntity>) hibernateTemplate.find("from ArachneEntity where ForeignKey like "+id+" and TableName like '"+table+"'" );
		if (liste.isEmpty()) {
			return null;
		} else {
			return (ArachneEntity) liste.get(0);
		}
	}
	
	
}
