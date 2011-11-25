package de.uni_koeln.arachne.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.util.ArachneId;

@Service("ArachneImageService")
public class ArachneImageService {
	@Autowired
	GenericSQLService genericSQLService;
	
	public void addImages(ArachneDataset dataset) {
		ArachneId arachneId = dataset.getArachneId();
		List<String> images = genericSQLService.getStringField("marbilder", arachneId.getTableName(), arachneId.getInternalKey(), "PS_MARBilderID");
		System.out.println("ImageList: " + images);
	}
}
