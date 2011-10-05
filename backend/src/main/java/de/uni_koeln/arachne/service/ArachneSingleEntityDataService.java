package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.ArachneDataMapDao;
import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.sqlutil.TableConnectionDescription;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * This Service Provides a Method to get a Single Dataset out of the Database
 *
 */
@Service("arachneSingleEntityDataService")
public class ArachneSingleEntityDataService {
	
	@Autowired
	ArachneDataMapDao arachneDataMapDao;
	List<TableConnectionDescription> subProjects;
	
	public ArachneSingleEntityDataService() {
		
		//TODO Make This more Flexible
		//This Manages The TableConnectionDescriptions which provide Infor About the Subprojects
		subProjects = new ArrayList<TableConnectionDescription>(9);
		// objekt Sub Projects
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektbauornamentik","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektgemaelde","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektlebewesen","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektmosaik","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektplastik","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektplomben","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektsiegel","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektterrakotten","PrimaryKey"));
		//The Display of Book allways Requires The Zenon Data
		subProjects.add( new TableConnectionDescription("buch","bibid","zenon","001"));
	
	}
	
	/**
	 * This Function Retrives an Arachne Dataset by <code>ArachneId</code>.
	 * This Function handles Exceptions!
	 * @param id an Identifier of the Type ArachneId.
	 * @return Instance of ArachneDataset that Represents the Dataset.
	 */
	public ArachneDataset getSingleEntityByArachneId(ArachneId id) {
		ArachneDataset result;
		Map<String, String> tempDataMap = arachneDataMapDao.getById(id);
		result = new ArachneDataset();
		result.setArachneId(id);
		result.setFields(tempDataMap);
		
		String tableName =  id.getTableName(); 
		if (tableName.equals("objekt")|| tableName.equals("buch")) {
			
			
			for (TableConnectionDescription tCD : subProjects) {
				if(tCD.linksTable(id.getTableName())){
					
					Map<String, String> temp = arachneDataMapDao.getBySubDataset(result, tCD);
					result.appendFields(temp);
					
				}
				
				
				
			}
			
			
			
		}
		return result;
	}
}
