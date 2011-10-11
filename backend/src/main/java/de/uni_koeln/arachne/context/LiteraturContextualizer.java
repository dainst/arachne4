package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.service.ArachneEntityIdentificationService;
import de.uni_koeln.arachne.service.GenericFieldService;
import de.uni_koeln.arachne.util.ArachneId;

public class LiteraturContextualizer implements IContextualizer {
	
	private ArachneEntityIdentificationService arachneEntityIdentificationService;
	private GenericFieldService genericFieldService;
	/**
	 * constructor
	 */
	public LiteraturContextualizer(ArachneEntityIdentificationService arachneEntityIdentificationService, GenericFieldService genericFieldService) {
		this.arachneEntityIdentificationService = arachneEntityIdentificationService;
		this.genericFieldService = genericFieldService;
		
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
		
		List<Long> contextIds = genericFieldService.getIdByFieldId("literaturzitat_leftjoin_literatur", parentTableName, parent.getArachneId().getInternalKey(), "literatur");
		
		if (contextIds != null) {
			ListIterator<Long> contextId = contextIds.listIterator(offset);
			while (contextId.hasNext() && linkCount < limit) {
				ArachneLink link = new ArachneLink();
					//Performance for testing
					ArachneDataset aDs = new ArachneDataset();
				ArachneId id = arachneEntityIdentificationService.getId("Literatur", contextId.next());
				link.setEntity1(parent);
				//for performance changed to empty dataset ONLY with arachneId
				//link.setEntity2(arachneSingleEntityDataService.getSingleEntityByArachneId(id));
					//performance testing
					aDs.setArachneId(id);
					link.setEntity2(aDs);
				linkCount += 1;
				System.out.println("Adding Link Literatur" + " number " + linkCount + "/" + limit + " of " + contextIds.size());
				result.add(link);
			}
		}
		return result;
	}
	
	
	/*
	 * SETTER
	 */
	
	public void setArachneEntityIdentificationService(
			ArachneEntityIdentificationService arachneEntityIdentificationService) {
		this.arachneEntityIdentificationService = arachneEntityIdentificationService;
	}



	public void setGenericFieldService(GenericFieldService genericFieldService) {
		this.genericFieldService = genericFieldService;
	}

}
