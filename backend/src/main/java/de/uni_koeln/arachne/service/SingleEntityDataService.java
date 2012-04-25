package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.DataMapDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.sqlutil.TableConnectionDescription;
import de.uni_koeln.arachne.util.EntityId;

/**
 * This Service Provides a Method to get a Single Dataset out of the Database
 *
 */
@Service("arachneSingleEntityDataService")
public class SingleEntityDataService {
	
	@Autowired
	DataMapDao arachneDataMapDao; // NOPMD
	
	private final transient List<TableConnectionDescription> subProjects;
	
	public SingleEntityDataService() {
		
		// TODO Make This more Flexible
		// This manages The TableConnectionDescriptions which provides Information about the sub projects
		// The subCategories contain additional Information to an Entitie of a Category 
		subProjects = new ArrayList<TableConnectionDescription>(9);
		// objekt sub projects
		
		final String PrimaryKey = "PrimaryKey";
		
		subProjects.add( new TableConnectionDescription("objekt",PrimaryKey,"objektbauornamentik",PrimaryKey));
		subProjects.add( new TableConnectionDescription("objekt",PrimaryKey,"objektgemaelde",PrimaryKey));
		subProjects.add( new TableConnectionDescription("objekt",PrimaryKey,"objektlebewesen",PrimaryKey));
		subProjects.add( new TableConnectionDescription("objekt",PrimaryKey,"objektmosaik",PrimaryKey));
		subProjects.add( new TableConnectionDescription("objekt",PrimaryKey,"objektplastik",PrimaryKey));
		subProjects.add( new TableConnectionDescription("objekt",PrimaryKey,"objektplomben",PrimaryKey));
		subProjects.add( new TableConnectionDescription("objekt",PrimaryKey,"objektsiegel",PrimaryKey));
		subProjects.add( new TableConnectionDescription("objekt",PrimaryKey,"objektterrakotten",PrimaryKey));
		// The display of book always requires the Zenon data
		subProjects.add( new TableConnectionDescription("buch","bibid","zenon","001"));
		
	}
	
	/**
	 * This Function Retrives an Arachne Dataset by <code>EntityId</code>.
	 * This Function handles Exceptions!
	 * @param entityId an Identifier of the Type ArachneId.
	 * @return Instance of ArachneDataset that Represents the Dataset.
	 */
	public Dataset getSingleEntityByArachneId(final EntityId entityId) {
		Dataset result;
		final Map<String, String> tempDataMap = arachneDataMapDao.getById(entityId);
		result = new Dataset();
		result.setArachneId(entityId);
		if (tempDataMap != null) {
			result.appendFields(tempDataMap);
		}
		
		final String tableName =  entityId.getTableName(); 
		//If There are Arachne Categories that require the Retrival of other Tables than the table of the Category
		if ("object".equals(tableName) || "buch".equals(tableName)) {
			for (TableConnectionDescription tCD : subProjects) {
				if(tCD.linksTable(entityId.getTableName())){
					final Map<String, String> temp = arachneDataMapDao.getBySubDataset(result, tCD);
					result.appendFields(temp);
				}
			}
		}
		
		return result;
	}
}
