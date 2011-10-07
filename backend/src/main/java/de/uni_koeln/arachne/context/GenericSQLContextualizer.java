package de.uni_koeln.arachne.context;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.uni_koeln.arachne.dao.GenericFieldDao;
import de.uni_koeln.arachne.dao.SQLDao;
import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.service.ArachneConnectionService;
import de.uni_koeln.arachne.service.GenericFieldService;
import de.uni_koeln.arachne.sqlutil.ArachneGenericFieldSQLQueryBuilder;

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
	
	/**
	 * Constructor initializing the type of the context. The type is used to retrieve the links.
	 * @param contextType
	 * @param genericFieldService 
	 */
	public GenericSQLContextualizer(String contextType, ArachneConnectionService arachneConnectionService, GenericFieldService genericFieldService) {
		this.contextType = contextType;
		this.arachneConnectionService = arachneConnectionService;
		this.genericFieldService = genericFieldService;
	}
	
	@Override
	public String getContextType() {
		return contextType;
	}

	@Override
	public List<Link> retrieve(ArachneDataset parent, Integer offset,
			Integer limit) {
		String parentTableName = parent.getArachneId().getTableName();
		String tableName = arachneConnectionService.getTableName(parentTableName, contextType);
		List<Long> contextIds = genericFieldService.getIdByFieldId(tableName, parentTableName, parent.getArachneId().getInternalKey(), contextType);
		// TODO query arachneidentitytable for ids
		// TODO fill links and return list
		return null;
	}

}
