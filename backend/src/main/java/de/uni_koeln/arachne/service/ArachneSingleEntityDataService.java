package de.uni_koeln.arachne.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.ArachneSingleDatasetDao;
import de.uni_koeln.arachne.responseobjects.ArachneDataset;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * This Service Provides a Method to get a Singe Dataset out of the Database
 * @author Rasmus Krempel
 *
 */
@Service("arachneSingleEntityDataService")
public class ArachneSingleEntityDataService {
	
	@Autowired
	ArachneSingleDatasetDao arachneSingleDatasetDao;
	
	public ArachneDataset getSingleEntityByArachneId( ArachneId id ){

		return arachneSingleDatasetDao.getById(id);
		
	}
	
	
	
	
}
