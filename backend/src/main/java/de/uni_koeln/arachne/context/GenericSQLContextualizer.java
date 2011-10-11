package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.springframework.beans.factory.annotation.Autowired;

import de.uni_koeln.arachne.dao.GenericFieldDao;
import de.uni_koeln.arachne.dao.SQLDao;
import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.service.ArachneConnectionService;
import de.uni_koeln.arachne.service.ArachneEntityIdentificationService;
import de.uni_koeln.arachne.service.ArachneSingleEntityDataService;
import de.uni_koeln.arachne.service.GenericFieldService;
import de.uni_koeln.arachne.sqlutil.ArachneGenericFieldSQLQueryBuilder;
import de.uni_koeln.arachne.util.ArachneId;

/**
 * This is the default <code>Contextualizer</code> the <code>ContextService</code> uses if 
 * no specialized one is specified.
 * <br>
 * It retrieves the links via SQL based on the <code>parent</code> type and the 'Verknuepfungen' table.  
 */
public class GenericSQLContextualizer implements IContextualizer {

	/**
	 * Service to access the 'Verknuepfungen' table. The information stored in that table is used
	 * to determine which table to query for context entities.
	 */	
	private ArachneConnectionService arachneConnectionService;
	
	/**
	 * The type of <code>Context<code> the <code>Contextualizer</code> retrieves.
	 */
	private String contextType;

	/**
	 * Used to query ids in 'cross tables'.
	 */
	private GenericFieldService genericFieldService;

	private ArachneEntityIdentificationService arachneEntityIdentificationService;

	private ArachneSingleEntityDataService arachneSingleEntityDataService;
	
	/**
	 * Constructor initializing the type of the context. The type is used to retrieve the links.
	 * @param contextType
	 * @param genericFieldService 
	 * @param arachneEntityIdentificationService 
	 * @param arachneSingleEntityDataService 
	 */
	public GenericSQLContextualizer(String contextType, ArachneConnectionService arachneConnectionService
			, GenericFieldService genericFieldService, ArachneEntityIdentificationService arachneEntityIdentificationService
			, ArachneSingleEntityDataService arachneSingleEntityDataService) {
		this.contextType = contextType;
		this.arachneConnectionService = arachneConnectionService;
		this.genericFieldService = genericFieldService;
		this.arachneEntityIdentificationService = arachneEntityIdentificationService;
		this.arachneSingleEntityDataService = arachneSingleEntityDataService;
	}
	
	@Override
	public String getContextType() {
		return contextType;
	}

	private Long linkCount = 0l;	
	
	@Override
	public List<Link> retrieve(ArachneDataset parent, Integer offset,
			Integer limit) {
		List<Link> result = new ArrayList<Link>();
		String parentTableName = parent.getArachneId().getTableName();
		// get 'cross table' name from the 'Verknuepfungen' table
		String tableName = arachneConnectionService.getTableName(parentTableName, contextType);
		// get context ids from 'cross table'
		List<Long> contextIds = genericFieldService.getIdByFieldId(tableName, parentTableName, parent.getArachneId().getInternalKey(), contextType);
		// get datasets, assemble the links and add them to the result list
		if (contextIds != null) {
			ListIterator<Long> contextId = contextIds.listIterator(offset);
			while (contextId.hasNext() && linkCount < limit) {
				ArachneLink link = new ArachneLink();
				link.setEntity1(parent);
				
				
				//improved Performance for testing ... less SQL-queries
					ArachneDataset aDs = new ArachneDataset();
					//Performance ... the arachneId is build without the identification-service
				
					ArachneId id = new ArachneId(tableName, contextId.next(), (long) 0, false);
					aDs.setArachneId(id);
					link.setEntity2(aDs);
				
				
					//for performance changed to empty dataset ONLY with arachneId
					//link.setEntity2(arachneSingleEntityDataService.getSingleEntityByArachneId(id));
					
				linkCount += 1;
				System.out.println("Adding Link " + contextType + " number " + linkCount + "/" + limit + " of " + contextIds.size());
				result.add(link);
			}
		}
		return result;
	}
}