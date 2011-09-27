package de.uni_koeln.arachne.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.uni_koeln.arachne.dao.ArachneEntityDao;
import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.response.Link;
import de.uni_koeln.arachne.util.ArachneId;
import de.uni_koeln.arachne.response.*;


@Service("arachneSingleEntityContextService")
public class ArachneSingleEntityContextService {
	
	@Autowired
	ArachneEntityIdentificationService arachneEntityIdentificationService;
	
	@Autowired
	ArachneSingleEntityDataService arachneSingleEntityDataService;
	
	@Autowired
	private ArachneEntityDao arachneEntityDao;
	
	public void addContext(ArachneDataset arachneDataset) {
		/*
		 * 
		 *	Following code is just a test! needs to be replaced  
		 * 
		 */
		
		if(arachneDataset.getArachneId().getInternalKey() == 2100062) {
			List<Link> links = new ArrayList<Link>();
			
			ArachneId arachneId = arachneEntityIdentificationService.getByEntityID((long)12628);
			ArachneDataset arachneContextDataset = arachneSingleEntityDataService.getSingleEntityByArachneId(arachneId);
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
			
			arachneDataset.setContext(links);
		}
	}
}
