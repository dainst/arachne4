package de.uni_koeln.arachne.response;

// TODO add documentation
public class ExternalLink extends Link {
	protected ArachneDataset entity;
	
	protected String uri2;
	
	@Override
	String getUri1() {
		return entity.getUri();
	}

	@Override
	String getUri2() {
		return uri2;
	}
}
