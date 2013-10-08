package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.DataMapDao;
import de.uni_koeln.arachne.dao.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.sqlutil.SQLToolbox;
import de.uni_koeln.arachne.sqlutil.TableConnectionDescription;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * This Service Provides a Method to get a Single Dataset out of the Database
 *
 */
@Service("arachneSingleEntityDataService")
public class SingleEntityDataService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SingleEntityDataService.class);
	
	@Autowired
	private transient DataMapDao arachneDataMapDao; 
	
	@Autowired
	private transient GenericSQLDao genericSqlDao; 
	
	private final transient List<TableConnectionDescription> subCategories;
	
	private final transient List<String> parentCategoryList;
	
	public SingleEntityDataService() {
		
		// TODO Make This more Flexible
		// This manages The TableConnectionDescriptions which provides Information about the sub categories
		// The subCategories contain additional Information to an entity of a category 
		subCategories = new ArrayList<TableConnectionDescription>(11);
		// objekt sub categories
		
		final String PrimaryKey = "PrimaryKey";
		
		subCategories.add( new TableConnectionDescription("objekt",PrimaryKey,"objektbauornamentik",PrimaryKey));
		subCategories.add( new TableConnectionDescription("objekt",PrimaryKey,"objektgemaelde",PrimaryKey));
		subCategories.add( new TableConnectionDescription("objekt",PrimaryKey,"objektkermik",PrimaryKey));
		subCategories.add( new TableConnectionDescription("objekt",PrimaryKey,"objektlebewesen",PrimaryKey));
		subCategories.add( new TableConnectionDescription("objekt",PrimaryKey,"objektmosaik",PrimaryKey));
		subCategories.add( new TableConnectionDescription("objekt",PrimaryKey,"objektplastik",PrimaryKey));
		subCategories.add( new TableConnectionDescription("objekt",PrimaryKey,"objektplomben",PrimaryKey));
		subCategories.add( new TableConnectionDescription("objekt",PrimaryKey,"objektsiegel",PrimaryKey));
		subCategories.add( new TableConnectionDescription("objekt",PrimaryKey,"objektterrakotten",PrimaryKey));
		// The display of book always requires the Zenon data
		subCategories.add( new TableConnectionDescription("buch","bibid","zenon","001"));
		// Some images my be unstructured so look for additional data in 'marbilderbestand'
		subCategories.add( new TableConnectionDescription("marbilder", "DateinameMarbilder", "marbilderbestand", "DateinameMarbilderbestand"));
		// connect mainabstract-records with additional information from quantification
		subCategories.add( new TableConnectionDescription("mainabstract", "FS_QuantitiesID", "quantities", "PS_QuantitiesID"));
		
		// create list of tables that may be linked to subcategories
		parentCategoryList = new ArrayList<String>();
		for (final TableConnectionDescription tcd: subCategories) {
			if (!parentCategoryList.contains(tcd.getTable1())) {
				parentCategoryList.add(tcd.getTable1());
			}
		}
		
		LOGGER.debug("Subcategory parent tables: " + parentCategoryList.toString());
	}
	
	/**
	 * This Function retrieves an Arachne dataset by <code>EntityId</code>.
	 * @param entityId The unique identifier of the type <code>EntityId</code>.
	 * @return Instance of <code>Dataset</code> that represents the dataset.
	 */
	public Dataset getSingleEntityByArachneId(final EntityId entityId) {
		LOGGER.debug("Getting id: " + entityId.getArachneEntityID());
		Dataset result = null;
		
		final Map<String, String> tempDataMap = arachneDataMapDao.getById(entityId);
		result = new Dataset();
		result.setArachneId(entityId);
		if (tempDataMap != null) {
			result.appendFields(tempDataMap);
		}
			
		final String tableName =  entityId.getTableName(); 
		//If There are Arachne Categories that require the Retrival of other Tables than the table of the Category
		if (parentCategoryList.contains(tableName)) {
			LOGGER.debug("Trying to retrieve sub data...");
			for (final TableConnectionDescription tCD : subCategories) {
				if (tCD.linksTable(entityId.getTableName())) {
					final Map<String, String> temp = arachneDataMapDao.getBySubDataset(result, tCD);
					result.appendFields(temp);
				}
			} 
		}
						
		return result;
	}

	/**
	 * Retrieves the dataset group of an entity.
	 * If an entity does not have a dataset group () <code>"Arachne"</code> is returned.
	 * @param arachneId The id of the entity of interest. 
	 * @return The dataset group of the entity.
	 */
	public String getDatasetGroup(final EntityId arachneId) {
		final String tableName = arachneId.getTableName();
		final Long field1Id = arachneId.getInternalKey();
		final String field2 = SQLToolbox.generateDatasetGroupName(tableName);
		
		// disable rights checking to allow retrieval of the dataset group
		final List<String> result = genericSqlDao.getStringField(tableName, tableName, field1Id, field2, true);
		
		if (StrUtils.isEmptyOrNull(result)) {
			return "Arachne";
		} else {
			return result.get(0);
		}
	}
}
