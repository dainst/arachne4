package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.response.ArachneLink;
import de.uni_koeln.arachne.response.Link;
import de.uni_koeln.arachne.service.ArachneContextualizationService;
import de.uni_koeln.arachne.service.ArachneInternalLinkService;
import de.uni_koeln.arachne.util.ArachneId;

@Service("arachneSingleEntityContextService")
public class ArachneSingleEntityContextService {
	
	@Autowired
	ArachneEntityIdentificationService arachneEntityIdentificationService;
	
	@Autowired
	ArachneSingleEntityDataService arachneSingleEntityDataService;
	
	

	//Calls the Right ArachneContextualizationService by name.
	public List<Link> getLinks (ArachneDataset parentDs, String contextName, Integer offset, Integer limit) {
		
		ArachneContextualizationService contextService = getContextByContextName(contextName);
		//contextService.retrive(parentDs, offset, limit);
		
		/*
		 * Testing Code
		 * 
		 */
		List<Link> links = new ArrayList<Link>();
		
		ArachneId arachneId1 = arachneEntityIdentificationService.getByEntityID((long)12628);
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
		links.add(aLink3);
		
		return links;
	}
	
	//gets the ArachneContextualizationService 
	protected ArachneContextualizationService getContextByContextName(String contextName){
		ArachneContextualizationService cService = new ArachneInternalLinkService();
		
		//if(contextName == "bauwerksteil") {
		//	cService =  new ArachneInternalLinkService();
		//}
		
		return cService;
	}
	
}
