package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.response.ArachneDataset;

public class LiteraturContextualizer implements IContextualizer {

	@Override
	public String getContextType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Link> retrieve(ArachneDataset parent, Integer offset, Integer limit) {
		// TODO retrieval implementation
		System.out.println("im Contexualizer angekommen");
		return null;
	}

}
