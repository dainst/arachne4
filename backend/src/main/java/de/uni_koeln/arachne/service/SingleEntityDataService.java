package de.uni_koeln.arachne.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.jdbc.DataMapDao;
import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;
import de.uni_koeln.arachne.util.sql.SQLToolbox;
import de.uni_koeln.arachne.util.sql.TableConnectionDescription;

/**
 * Service to retrieve single datasets from the database.
 */
@Service("arachneSingleEntityDataService")
public class SingleEntityDataService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SingleEntityDataService.class);
	
	@Autowired
	private transient DataMapDao arachneDataMapDao; 
	
	@Autowired
	private transient GenericSQLDao genericSqlDao; 
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil; 
	
	private transient final List<String> authFreeTables;
	
	@Autowired
	public SingleEntityDataService(final @Value("#{config.authFreeTables}") String authFreeTablesAsCSS) {
		authFreeTables = StrUtils.getCommaSeperatedStringAsList(authFreeTablesAsCSS);
	}
	
	/**
	 * Retrieves the dataset group of an entity.
	 * If an entity does not have a dataset group () <code>"Arachne"</code> is returned.
	 * @param arachneId The id of the entity of interest. 
	 * @return The dataset group of the entity or "Arachne" for tables without a dataset group.
	 */
	public String getDatasetGroup(final EntityId arachneId) {
		final String tableName = arachneId.getTableName();
		
		if (authFreeTables.contains(tableName)) {
			return "Arachne";
		} else {
		
			final Long field1Id = arachneId.getInternalKey();
			final String field2 = SQLToolbox.generateDatasetGroupName(tableName);
		
			// disable rights checking to allow retrieval of the dataset group
			final String result = genericSqlDao.getStringField(tableName, tableName, field1Id, field2, true);
		
			if (StrUtils.isEmptyOrNull(result)) {
				return "Arachne";
			} else {
				return result;
			}
		}
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
		//If there are Arachne categories that require the retrieval of other tables than the table of the category
		final List<TableConnectionDescription> categorySubs = xmlConfigUtil.getSubCategories(tableName);
		LOGGER.debug("Trying to retrieve sub data...");
		for (final TableConnectionDescription tCD: categorySubs) {
			if (tCD.linksTable(entityId.getTableName())) {
				final Map<String, String> temp = arachneDataMapDao.getBySubDataset(result, tCD);
				result.appendFields(temp);
			}
		}
								
		return result;
	}
}
