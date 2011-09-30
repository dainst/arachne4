package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.response.ArachneDataset;

public class LiteratureContextualizer implements Contextualizer {

	@Override
	public String getContextType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Link> retrive(ArachneDataset parent, Integer offset, Integer limit) {
	
		// TODO retrival implementation
		System.out.println("im Contexualizer angekommen");
		return null;
	}

}
