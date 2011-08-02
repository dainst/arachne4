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
	public ArachneId getByEntityID(Long ArachneEntityID){
		return constructArachneID(arachneEntityDao.getByEntityID(ArachneEntityID));
	}
	
	public ArachneId getByTablenameAndInternalKey(String table, Long id){
		return constructArachneID(arachneEntityDao.getByTablenameAndInternalKey(table, id));
	}
	
	private ArachneId constructArachneID(ArachneEntity ae){
		ArachneId aid = new ArachneId(ae.getTableName(),ae.getForeignKey(),ae.getId(),ae.isDeleted());	
		return aid;
	}
	
	
}
