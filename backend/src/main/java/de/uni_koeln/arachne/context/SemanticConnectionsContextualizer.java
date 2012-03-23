package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.persistence.Entity;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.GenericSQLService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.util.ArachneId;
import de.uni_koeln.arachne.util.StrUtils;

public class SemanticConnectionsContextualizer implements IContextualizer {

	/**
	 * The type of <code>Context<code> the <code>Contextualizer</code> retrieves.
	 */
	private String contextType;
	
	private GenericSQLService genericSQLService;
	
	private EntityIdentificationService entityIdentificationService;
	
	private SingleEntityDataService singleEntityDataService;
	
	public SemanticConnectionsContextualizer(String contextType, GenericSQLService genericSQLService
			,EntityIdentificationService entityIdentificationService, SingleEntityDataService singleEntityDataService) {
		this.contextType = contextType;
		this.genericSQLService = genericSQLService;
		this.entityIdentificationService = entityIdentificationService;
		this.singleEntityDataService = singleEntityDataService;
	}
	
	@Override
	public String getContextType() {
		return contextType;
	}

	private long linkCount = 0l;
	
	@Override
	public List<Link> retrieve(Dataset parent, Integer offset, Integer limit) {
		List<Link> result = new ArrayList<Link>();
		String parentTableName = parent.getArachneId().getTableName();
		System.out.println("parentTableName: " + parentTableName + " - contextType: " + contextType);
		List<String> fields = new ArrayList<String>();
		List<String> contextIds = genericSQLService.getConnectedEntities("ArachneSemanticConnection", contextType
				, parent.getArachneId().getArachneEntityID(), "ForeignKeyTarget");
		System.out.println("ContextIds: " + contextIds);
		if (!StrUtils.isEmptyOrNull(contextIds)) {
			// get datasets, assemble the links and add them to the result list
			ListIterator<String> contextId = contextIds.listIterator(offset);
			while (contextId.hasNext() && (linkCount < limit || limit == -1)) {
				ArachneLink link = new ArachneLink();
				link.setEntity1(parent);

				ArachneId arachneId = entityIdentificationService.getId(contextType, Long.parseLong(contextId.next()));
				if (arachneId == null) {
					// The magic number zero ("0L") means that the entity is not in the "arachneentityidentificaton" table
					arachneId = new ArachneId(contextType, Long.parseLong(contextId.next()), 0L, false);
				}

				link.setEntity2(singleEntityDataService.getSingleEntityByArachneId(arachneId));

				linkCount += 1;
				System.out.println("Adding Link " + contextType + " number " + linkCount + "/" + limit + " of " + contextIds.size());
				result.add(link);
			}
		}
		return result;
	}

}
