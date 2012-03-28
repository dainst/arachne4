package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.GenericSQLService;
import de.uni_koeln.arachne.util.EntityId;

/**
 * This contextualizer retrieves internal contexts (tables in the arachne database). 
 */
public class SemanticConnectionsContextualizer implements IContextualizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticConnectionsContextualizer.class);
	
	/**
	 * The type of <code>Context<code> the <code>Contextualizer</code> retrieves.
	 */
	private transient String contextType;
	
	private transient GenericSQLService genericSQLService;
	
	private transient long linkCount = 0l;
	
	public SemanticConnectionsContextualizer(final String contextType, final GenericSQLService genericSQLService) {
		this.contextType = contextType;
		this.genericSQLService = genericSQLService;
	}
	
	@Override
	public String getContextType() {
		return contextType;
	}
	
	@Override
	public List<AbstractLink> retrieve(Dataset parent, Integer offset, Integer limit) {
		final List<AbstractLink> result = new ArrayList<AbstractLink>();
		
		final long queryTime = System.currentTimeMillis();
		final List<Map<String, String>> contextContents = genericSQLService.getConnectedEntities(contextType
				, parent.getArachneId().getArachneEntityID());
		LOGGER.debug("Query time: " + String.valueOf(System.currentTimeMillis() - queryTime) + " ms");		
		
		if (contextContents != null) {
			final ListIterator<Map<String, String>> contextMap = contextContents.listIterator(offset);
			while (contextMap.hasNext() && (linkCount < limit || limit == -1)) {
				final Map<String, String> map = contextMap.next();
				final ArachneLink link = new ArachneLink();
				final Dataset dataset = new Dataset();
				
				// this is how the contextualizer can set his own names
				// here the ArachneEntityID is extracted from the query result and  
				Long foreignKey = 0L;
				Long eId = 0L;
				boolean isDeleted = false;
				final Map<String, String> resultMap = new HashMap<String, String>();
				for (Map.Entry<String, String> entry: map.entrySet()) {
					final String key = entry.getKey();
					if (!(key.contains("PS_") && key.contains("ID"))) {
						// get ArachneEntityID from  
						if (key.startsWith("arachneentityidentification")) {
							if (key.endsWith("ArachneEntityID")) {
								eId = Long.parseLong(entry.getValue()); 
							} else if (key.endsWith("ForeignKey")) {
								foreignKey = Long.parseLong(entry.getValue());
							} else if (key.endsWith("isDeleted")) {
								isDeleted = Boolean.parseBoolean(entry.getValue());
							} 
						}
						
						final String newKey = contextType + "." + key.split("\\.")[1];
						resultMap.put(newKey, entry.getValue());
					}
				}
				final EntityId entityId = new EntityId(contextType, foreignKey, eId, isDeleted);
				dataset.setArachneId(entityId);
				dataset.appendFields(resultMap);
				link.setEntity1(parent);
				link.setEntity2(dataset);
				result.add(link);
			}
		}
		return result;
	}
}
