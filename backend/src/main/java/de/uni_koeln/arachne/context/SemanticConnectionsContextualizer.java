package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.GenericSQLService;
import de.uni_koeln.arachne.util.ArachneId;

public class SemanticConnectionsContextualizer implements IContextualizer {

	/**
	 * The type of <code>Context<code> the <code>Contextualizer</code> retrieves.
	 */
	private String contextType;
	
	private GenericSQLService genericSQLService;
	
	public SemanticConnectionsContextualizer(String contextType, GenericSQLService genericSQLService) {
		this.contextType = contextType;
		this.genericSQLService = genericSQLService;
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
		List<Map<String, String>> contextContents = genericSQLService.getConnectedEntities(contextType
				, parent.getArachneId().getArachneEntityID());
				
		if (contextContents != null) {
			ListIterator<Map<String, String>> contextMap = contextContents.listIterator(offset);
			while (contextMap.hasNext() && (linkCount < limit || limit == -1)) {
				Map<String, String> map = contextMap.next();
				ArachneLink link = new ArachneLink();
				Dataset dataset = new Dataset();
				
				// this is how the contextualizer can set his own names
				Long foreignKey = 0L;
				Long entityId = 0L;
				boolean isDeleted = false;
				Map<String, String> resultMap = new HashMap<String, String>();
				for (Map.Entry<String, String> entry: map.entrySet()) {
					String key = entry.getKey();
					if (!(key.contains("PS_") && key.contains("ID"))) {
						if (key.startsWith("arachneentityidentification")) {
							if (key.endsWith("ArachneEntityID")) {
								entityId = Long.parseLong(entry.getValue()); 
							} else if (key.endsWith("ForeignKey")) {
								foreignKey = Long.parseLong(entry.getValue());
							} else if (key.endsWith("isDeleted")) {
								isDeleted = Boolean.parseBoolean(entry.getValue());
							} 
														
						}
						String newKey = contextType + "." + key.split("\\.")[1];
						resultMap.put(newKey, entry.getValue());
					}
				}
				ArachneId arachneId = new ArachneId(contextType, foreignKey, entityId, isDeleted);
				dataset.setArachneId(arachneId);
				dataset.appendFields(resultMap);
				link.setEntity1(parent);
				link.setEntity2(dataset);
				result.add(link);
			}
		}
		return result;
	}

}
