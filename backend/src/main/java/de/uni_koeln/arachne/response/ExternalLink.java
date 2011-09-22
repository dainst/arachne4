package de.uni_koeln.arachne.response;

// TODO add documentation
public class ExternalLink extends Link {
	protected ArachneDataset entity;
	
	protected String uri2;
	
	@Override
	public String getUri1() {
		return entity.getUri();
	}

	@Override
	public String getUri2() {
		return uri2;
	}

	public ArachneDataset getEntity() {
		return entity;
	}

	public void setEntity(ArachneDataset entity) {
		this.entity = entity;
	}

	public void setUri2(String uri2) {
		this.uri2 = uri2;
	}
}
