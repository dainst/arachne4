package de.uni_koeln.arachne.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.ArachneEntityDao;
import de.uni_koeln.arachne.mapping.ArachneEntity;
import de.uni_koeln.arachne.util.ArachneId;


@Service("arachneEntityIdentificationService")
public class ArachneEntityIdentificationService {
	
	@Autowired
	private ArachneEntityDao arachneEntityDao;
	/**
	 * get all Identifiers of a Dataset by Arachne Entity ID 
	 * This is the External Reference ID for the Dataset in arachne
	 * @param ArachneEntityID ArachneEntityID
	 * @return an <code>ArachneId</code> object that contains all the Identification information
	 */
	public ArachneId getByEntityID(Long ArachneEntityID){
		return constructArachneID(arachneEntityDao.getByEntityID(ArachneEntityID));
	}
	/**
	 * get all Identifiers of a Dataset by Tablename and Primary key
	 * @param table The Internal Table name in the Arachne Database
	 * @param id The Internal Table key of the Dataset
	 * @return an <code>ArachneId</code> object that contains all the Identification information
	 */
	public ArachneId getByTablenameAndInternalKey(String table, Long id){
		return constructArachneID(arachneEntityDao.getByTablenameAndInternalKey(table, id));
	}
	
	private ArachneId constructArachneID(ArachneEntity ae){
		ArachneId aid = new ArachneId(ae.getTableName(),ae.getForeignKey(),ae.getId(),ae.isDeleted());	
		return aid;
	}
	
	
}
