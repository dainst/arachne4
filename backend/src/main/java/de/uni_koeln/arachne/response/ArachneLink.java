package de.uni_koeln.arachne.response;

// TODO add documentation
public class ArachneLink extends Link {

	private ArachneDataset entity1; 
	
	private ArachneDataset entity2;
	
	@Override
	public String getUri1() {
		return entity1.getUri();
	}

	@Override
	public String getUri2() {
		return entity2.getUri();
	}

	public ArachneDataset getEntity1() {
		return entity1;
	}

	public void setEntity1(ArachneDataset entity1) {
		this.entity1 = entity1;
	}

	public ArachneDataset getEntity2() {
		return entity2;
	}

	public void setEntity2(ArachneDataset entity2) {
		this.entity2 = entity2;
	}
}
