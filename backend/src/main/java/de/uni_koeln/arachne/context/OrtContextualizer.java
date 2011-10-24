package de.uni_koeln.arachne.context;

import java.util.ArrayList;
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
	
	// TODO remove this
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
				System.out.println("arachneId: " + arachneId.getArachneEntityID());
				dataset.setArachneId(arachneId);
				map.remove(id);
				dataset.appendFields(map);
				link.setEntity1(parent);
				link.setEntity2(dataset);
				linkCount += 1;
				System.out.println("Adding Link Ort " + " number " + linkCount + "  - limit = " + limit);
				result.add(link);
			}
		}
		return result;
	}
}