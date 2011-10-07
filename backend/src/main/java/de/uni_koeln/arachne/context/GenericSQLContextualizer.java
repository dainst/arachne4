package de.uni_koeln.arachne.context;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.service.ArachneConnectionService;

/**
 * This is the default <code>Contextualizer</code> the <code>ContextService</code> uses if 
 * no specialized one is specified.
 * <br>
 * It retrieves the links via SQL based on the <code>parent</code> type and the 'Verknuepfungen' table.  
 */
public class GenericSQLContextualizer implements IContextualizer {

	/**
	 * Service to access the 'Verknuepfungen' table. The information stored in that table is used
	 * to determine which contexts the <code>addContext</code> method adds to a given dataset.
	 */	
	private ArachneConnectionService arachneConnectionService;
	
	/**
	 * The type of <code>Context<code> the <code>Contextualizer</code> retrieves.
	 */
	private String contextType;
	
	/**
	 * Constructor initializing the type of the context. The type is used to retrieve the links.
	 * @param contextType
	 */
	public GenericSQLContextualizer(String contextType, ArachneConnectionService arachneConnectionService) {
		this.contextType = contextType;
		this.arachneConnectionService = arachneConnectionService;
	}
	
	@Override
	public String getContextType() {
		return contextType;
	}

	@Override
	public List<Link> retrieve(ArachneDataset parent, Integer offset,
			Integer limit) {
		String tableName = arachneConnectionService.getTableName(parent.getArachneId().getTableName(), contextType);
		System.out.println("GenericSQLContextualizer: " + contextType + " tablename: " + tableName);
		return null;
	}

}
