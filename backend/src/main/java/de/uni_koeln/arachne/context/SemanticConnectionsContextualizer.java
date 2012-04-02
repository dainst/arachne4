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
	private transient final String contextType;
	
	private transient final GenericSQLService genericSQLService;
	
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
	public List<AbstractLink> retrieve(final Dataset parent, final Integer offset, final Integer limit) {
		final List<AbstractLink> result = new ArrayList<AbstractLink>();
		
		final long queryTime = System.currentTimeMillis();
		final List<Map<String, String>> contextContents = genericSQLService.getConnectedEntities(contextType
				, parent.getArachneId().getArachneEntityID());
		LOGGER.debug("Query time: " + (System.currentTimeMillis() - queryTime) + " ms");		
		
		if (contextContents != null) {
			final ListIterator<Map<String, String>> contextMap = contextContents.listIterator(offset);
			while (contextMap.hasNext() && (linkCount < limit || limit == -1)) {
				final ArachneLink link = new ArachneLink();
				link.setEntity1(parent);
				link.setEntity2(createDatasetFromQueryResults(contextMap.next()));
				result.add(link);
				linkCount++;
			}
		}
		return result;
	}

	private Dataset createDatasetFromQueryResults(final Map<String, String> map) {

		final Dataset result = new Dataset();
		// this is how the contextualizer can set his own names
		Long foreignKey = 0L;
		Long eId = 0L;
		boolean isDeleted = false;
		final Map<String, String> resultMap = new HashMap<String, String>();
		for (Map.Entry<String, String> entry: map.entrySet()) {
			final String key = entry.getKey();
			if (!(key.contains("PS_") && key.contains("ID"))) {
				// get ArachneEntityID from context query result  
				if ("arachneentityidentification.ArachneEntityID".equals(key)) {
					eId = Long.parseLong(entry.getValue()); 
				} else if ("arachneentityidentification.ForeignKey".equals(key)) {
					foreignKey = Long.parseLong(entry.getValue());
				} else if ("arachneentityidentification.isDeleted".equals(key)) {
					isDeleted = Boolean.parseBoolean(entry.getValue());
				} 

				final String newKey = contextType + "." + key.split("\\.")[1];
				resultMap.put(newKey, entry.getValue());
			}
		}
		final EntityId entityId = new EntityId(contextType, foreignKey, eId, isDeleted);
		result.setArachneId(entityId);
		result.appendFields(resultMap);
		return result;
	}
}
