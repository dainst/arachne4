package de.uni_koeln.arachne.service;

import java.util.List;

import org.springframework.stereotype.Service;
import de.uni_koeln.arachne.response.*;

@Service("arachneContexualizerService")
public abstract class ArachneContextualizationService {
	
	
	/*
	 * The List holding the links.
	 */
	protected List<Link> links;
	
	
	/*
	 * Methods for implementing in special ContextServices 
	 */
	abstract String getContextType();
	abstract List<Link> retrive(ArachneDataset parentDs,  Integer offset, Integer limit);

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	} 
	
	
	
}
