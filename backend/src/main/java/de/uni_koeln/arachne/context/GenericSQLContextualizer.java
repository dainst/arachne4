package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.GenericSQLService;
import de.uni_koeln.arachne.util.ArachneId;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * This is the baseclass for contextualizers that get their data from Arachne via SQL.
 * For 'leftjoin tables' <code>joinTableName</code> must be set in the derived class. 
 */
public abstract class GenericSQLContextualizer implements IContextualizer {

	private GenericSQLService genericSQLService;
	
	protected String tableName = null;
	protected String joinTableName = null;
		
	/**
	 * Constructor setting the needed services.
	 */
	public GenericSQLContextualizer(GenericSQLService genericSQLService) {
		this.genericSQLService = genericSQLService;
	}
	
	// TODO check if this method is needed/makes sense
	@Override
	public String getContextType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Long linkCount = 0l;
	
	@Override
	public List<Link> retrieve(Dataset parent, Integer offset, Integer limit) {
		if (StrUtils.isEmptyOrNull(joinTableName)) {
			joinTableName = tableName;
		}
		List<Link> result = new ArrayList<Link>();
		String parentTableName = parent.getArachneId().getTableName();
		
		List<Map<String, String>> contextContents = genericSQLService.getEntitiesEntityIdJoinedById(joinTableName
				, parentTableName, parent.getArachneId().getInternalKey());
				
		// TODO for many context objects this is REALLY expensive - find a way to improve the performance
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
						String newKey = tableName + "." + key.split("\\.")[1];
						resultMap.put(newKey, entry.getValue());
					}
				}
				ArachneId arachneId = new ArachneId(tableName, foreignKey, entityId, isDeleted);
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