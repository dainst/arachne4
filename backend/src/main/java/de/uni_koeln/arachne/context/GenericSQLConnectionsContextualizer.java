package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.ConnectionService;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.service.GenericSQLService;
import de.uni_koeln.arachne.sqlutil.SQLToolbox;
import de.uni_koeln.arachne.util.ArachneId;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * This is the default <code>Contextualizer</code> the <code>ContextService</code> uses if 
 * no specialized one is specified.
 * <br>
 * It retrieves the links via SQL based on the <code>parent</code> type and the 'Verknuepfungen' table.  
 */
public class GenericSQLConnectionsContextualizer implements IContextualizer {

	/**
	 * Service to access the 'Verknuepfungen' table. The information stored in that table is used
	 * to determine which table to query for context entities.
	 */	
	private ConnectionService arachneConnectionService;
	
	/**
	 * The type of <code>Context<code> the <code>Contextualizer</code> retrieves.
	 */
	private String contextType;

	/**
	 * Used to query ids in 'cross tables'.
	 */
	private GenericSQLService genericSQLService;

	private EntityIdentificationService arachneEntityIdentificationService;

	private SingleEntityDataService arachneSingleEntityDataService;
	
	/**
	 * Constructor initializing the type of the context. The type is used to retrieve the links.
	 * @param contextType
	 * @param genericSQLService 
	 * @param arachneEntityIdentificationService 
	 * @param arachneSingleEntityDataService 
	 */
	public GenericSQLConnectionsContextualizer(String contextType, ConnectionService arachneConnectionService
			, GenericSQLService genericSQLService, EntityIdentificationService arachneEntityIdentificationService
			, SingleEntityDataService arachneSingleEntityDataService) {
		this.contextType = contextType;
		this.arachneConnectionService = arachneConnectionService;
		this.genericSQLService = genericSQLService;
		this.arachneEntityIdentificationService = arachneEntityIdentificationService;
		this.arachneSingleEntityDataService = arachneSingleEntityDataService;
	}
	
	@Override
	public String getContextType() {
		return contextType;
	}

	private Long linkCount = 0l;	
	
	@Override
	public List<Link> retrieve(Dataset parent, Integer offset,
			Integer limit) {
		List<Link> result = new ArrayList<Link>();
		String parentTableName = parent.getArachneId().getTableName();
		// get 'cross table' name from the 'Verknuepfungen' table
		System.out.println("parentTableName: " + parentTableName + " - contextType: " + contextType);
		String tableName = arachneConnectionService.getTableName(parentTableName, contextType);
		System.out.println("tableName: " + tableName);
		if (!StrUtils.isEmptyOrNull(tableName)) {
			// get context ids from 'cross table'
			List<Long> contextIds = genericSQLService.getIdByFieldId(tableName, parentTableName, parent.getArachneId()
					.getInternalKey(), SQLToolbox.generateForeignKeyName(contextType));
			
			// get datasets, assemble the links and add them to the result list
			if (contextIds != null) {
				ListIterator<Long> contextId = contextIds.listIterator(offset);
				while (contextId.hasNext() && linkCount < limit) {
					ArachneLink link = new ArachneLink();
					link.setEntity1(parent);

					// contexts can not be retrieved from the same table as the parent, so if the tables are the same
					// set it to the contexts one (needed for example for relief)
					if (tableName.equals(parentTableName)) {
						tableName = contextType;
					}
					
					ArachneId arachneId = arachneEntityIdentificationService.getId(tableName, contextId.next());
					if (arachneId == null) {
						// The magic number zero ("0L") means that the entity is not in the "arachneentityidentificaton" table
						arachneId = new ArachneId(tableName, contextId.next(), 0L, false);
					}
					
					link.setEntity2(arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId));

					linkCount += 1;
					System.out.println("Adding Link " + contextType + " number " + linkCount + "/" + limit + " of " + contextIds.size());
					result.add(link);
				}
			}
		}
		return result;
	}
}