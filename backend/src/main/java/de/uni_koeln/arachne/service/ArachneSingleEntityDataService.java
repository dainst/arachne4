package de.uni_koeln.arachne.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.ArachneSingleDatasetDao;
import de.uni_koeln.arachne.responseobjects.ArachneDataset;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * This Service Provides a Method to get a Single Dataset out of the Database
 * @author Rasmus Krempel
 *
 */
@Service("arachneSingleEntityDataService")
public class ArachneSingleEntityDataService {
	
	@Autowired
	ArachneSingleDatasetDao arachneSingleDatasetDao;
	/**
	 * This Function Retrives an Arachne Dataset by <code>ArachneId</code>.
	 * @param id an Identifier of the Type ArachneId.
	 * @return Instance of ArachneDataset that Represents the Dataset.
	 */
	public ArachneDataset getSingleEntityByArachneId(ArachneId id) {
		ArachneDataset result;
		result = arachneSingleDatasetDao.getById(id);
		
		String tableName =  id.getTableName(); 
		if (!tableName.equals("marbilder")) {
			
		}
		return result;
	}
}
