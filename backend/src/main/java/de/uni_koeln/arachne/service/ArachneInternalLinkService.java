package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.response.ArachneLink;
import de.uni_koeln.arachne.response.Link;
import de.uni_koeln.arachne.util.ArachneId;

public class ArachneInternalLinkService extends ArachneContextualizationService {

	@Autowired
	ArachneEntityIdentificationService arachneEntityIdentificationService;
	
	@Autowired
	ArachneSingleEntityDataService arachneSingleEntityDataService;
	
	
	@Override
	public String getContextType() {
		String type = "Arachne Internal Links";
		return type;
	}

	@Override
	public List<Link> retrive(ArachneDataset parentDs,  Integer offset, Integer limit) {
		links = new ArrayList<Link>();
		
		/*
		 * Placeholder-Testing code
		 */
		if(parentDs.getArachneId().getInternalKey() == 2100062) {
			/*ArachneId arachneId1 = arachneEntityIdentificationService.getByEntityID((long)12628);
			ArachneDataset arachneContextDataset = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId1);
			ArachneLink aLink = new ArachneLink();
			aLink.setEntity1(arachneContextDataset);
			links.add(aLink);
			
			ArachneId arachneId2 = arachneEntityIdentificationService.getByEntityID((long)12630);
			ArachneDataset arachneContextDataset2 = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId2);
			ArachneLink aLink2 = new ArachneLink();
			aLink2.setEntity1(arachneContextDataset2);
			links.add(aLink2);
			
			ArachneId arachneId3 = arachneEntityIdentificationService.getByEntityID((long)12631);
			ArachneDataset arachneContextDataset3 = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId3);
			ArachneLink aLink3 = new ArachneLink();
			aLink3.setEntity1(arachneContextDataset3);
			links.add(aLink3);*/
			
			

			
		}
		
		return links;
	}

}
