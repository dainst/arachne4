package de.uni_koeln.arachne.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.ArachneDataMapDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.sqlutil.TableConnectionDescription;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * This Service Provides a Method to get a Single Dataset out of the Database
 *
 */
@Service("arachneSingleEntityDataService")
public class SingleEntityDataService {
	
	@Autowired
	ArachneDataMapDao arachneDataMapDao;
	List<TableConnectionDescription> subProjects;
	
	public SingleEntityDataService() {
		/*
		// TODO Make This more Flexible
		// This manages The TableConnectionDescriptions which provides Information about the sub projects
		subProjects = new ArrayList<TableConnectionDescription>(9);
		// objekt sub projects
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektbauornamentik","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektgemaelde","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektlebewesen","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektmosaik","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektplastik","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektplomben","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektsiegel","PrimaryKey"));
		subProjects.add( new TableConnectionDescription("objekt","PrimaryKey","objektterrakotten","PrimaryKey"));
		// The display of book always requires the Zenon data
		subProjects.add( new TableConnectionDescription("buch","bibid","zenon","001"));
		*/
	}
	
	/**
	 * This Function Retrives an Arachne Dataset by <code>ArachneId</code>.
	 * This Function handles Exceptions!
	 * @param id an Identifier of the Type ArachneId.
	 * @return Instance of ArachneDataset that Represents the Dataset.
	 */
	public Dataset getSingleEntityByArachneId(ArachneId id) {
		Dataset result;
		Map<String, String> tempDataMap = arachneDataMapDao.getById(id);
		result = new Dataset();
		result.setArachneId(id);
		if (tempDataMap != null) {
			result.appendFields(tempDataMap);
		}
		/*
		String tableName =  id.getTableName(); 
		if (tableName.equals("objekt") || tableName.equals("buch")) {
			for (TableConnectionDescription tCD : subProjects) {
				if(tCD.linksTable(id.getTableName())){
					Map<String, String> temp = arachneDataMapDao.getBySubDataset(result, tCD);
					result.appendFields(temp);
				}
			}
		}
		*/
		return result;
	}
}
