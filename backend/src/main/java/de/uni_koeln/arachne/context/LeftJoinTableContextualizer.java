package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.GenericSQLService;
import de.uni_koeln.arachne.util.ArachneId;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * This is the baseclass for all contextualizers that get their contexts from
 * 'leftjoin tables', most likely only the <code>Ort-</code> and <code>LiteraturContextualizers</code>.
 * It can also be used to extract data from 'normal' tables by not setting <code>joinTableName</code> or setting it 
 * to </code>null<code> in the constructor of the derived class.
 */
public abstract class LeftJoinTableContextualizer implements IContextualizer {

	private EntityIdentificationService arachneEntityIdentificationService;
	private GenericSQLService genericSQLService;
	
	protected String tableName;
	protected String joinTableName;
	
	
	/**
	 * Constructor setting the needed services.
	 */
	public LeftJoinTableContextualizer(EntityIdentificationService arachneEntityIdentificationService
			, GenericSQLService genericSQLService) {
		this.arachneEntityIdentificationService = arachneEntityIdentificationService;
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
		List<Map<String, String>> contextContents = genericSQLService.getEntitiesById(joinTableName
				, parentTableName, parent.getArachneId().getInternalKey());
		
		// TODO for many context objects this is REALLY expensive - find a way to improve the performance
		if (contextContents != null) {
			ListIterator<Map<String, String>> contextMap = contextContents.listIterator(offset);
			while (contextMap.hasNext() && linkCount < limit) {
				Map<String, String> map = contextMap.next();
				ArachneLink link = new ArachneLink();
				Dataset dataset = new Dataset();
				String id = map.get(joinTableName + ".PS_" + Character.toUpperCase(tableName.charAt(0)) + tableName.substring(1) + "ID");
				ArachneId arachneId = arachneEntityIdentificationService.getId(tableName, Long.parseLong(id));
				if (arachneId == null) {
					// The magic number zero ("0L") means that the entity is not in the "arachneentityidentificaton" table
					arachneId = new ArachneId(joinTableName, Long.parseLong(id), 0L, false);
				}
				dataset.setArachneId(arachneId);
				// TODO remove debug
				System.out.println("LeftJoinTableContextualizer ID: " + arachneId.getArachneEntityID());
				// this is how the contextualizer can set his own names
				Map<String, String> resultMap = new HashMap<String, String>();
				for (Map.Entry<String, String> entry: map.entrySet()) {
					String key = entry.getKey();
					if (!(key.contains("PS_") && key.contains("ID"))) {
						String newKey = tableName + "." + key.split("\\.")[1];
						resultMap.put(newKey, entry.getValue());
					}
				}
				dataset.appendFields(resultMap);
				link.setEntity1(parent);
				link.setEntity2(dataset);
				result.add(link);
			}
		}
		return result;
	}
}
