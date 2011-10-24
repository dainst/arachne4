package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.service.ArachneEntityIdentificationService;
import de.uni_koeln.arachne.service.GenericSQLService;

import de.uni_koeln.arachne.util.ArachneId;

public class OrtContextualizer implements IContextualizer {

	private ArachneEntityIdentificationService arachneEntityIdentificationService;
	private GenericSQLService genericSQLService;
	/**
	 * Constructor setting the needed services.
	 */
	public OrtContextualizer(ArachneEntityIdentificationService arachneEntityIdentificationService
			, GenericSQLService genericSQLService) {
		this.arachneEntityIdentificationService = arachneEntityIdentificationService;
		this.genericSQLService = genericSQLService;
	}
	
	@Override
	public String getContextType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Long linkCount = 0l;
	
	@Override
	public List<Link> retrieve(ArachneDataset parent, Integer offset, Integer limit) {
		List<Link> result = new ArrayList<Link>();
		String parentTableName = parent.getArachneId().getTableName();
		System.out.println("OrtContextualizer getting contexts...");
		List<Map<String, String>> contextContents = genericSQLService.getEntitiesById("ortsbezug_leftjoin_ort"
				, parentTableName, parent.getArachneId().getInternalKey());
		
		if (contextContents != null) {
			ListIterator<Map<String, String>> contextMap = contextContents.listIterator(offset);
			while (contextMap.hasNext() && linkCount < limit) {
				Map<String, String> map = contextMap.next();
				ArachneLink link = new ArachneLink();
				ArachneDataset dataset = new ArachneDataset();
				String id = map.get("ortsbezug_leftjoin_ort.PS_OrtID");
				ArachneId arachneId = arachneEntityIdentificationService.getId("Ort", Long.parseLong(id));
				dataset.setArachneId(arachneId);
				// rename ortsbezug_leftjoin_ort to ort
				// this is how the contextualizer can set his own names
				Map<String, String> resultMap = new HashMap<String, String>();
				for (Map.Entry<String, String> entry: map.entrySet()) {
					String key = entry.getKey();
					if (!(key.contains("PS_") && key.contains("ID"))) {
						String newKey = "ort." + key.split("\\.")[1];
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