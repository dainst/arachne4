package de.uni_koeln.arachne.context;

import de.uni_koeln.arachne.response.Dataset;

/**
 * This class is a specialized <code>Link</code> to hold internal links. This means that both sides of the link
 * are entities fetched from the database. As such they a represented as <code>ArachneDatasets</code>. 
 */
public class ArachneLink extends AbstractLink {

	/**
	 * Left side of the link. In general this is the parent entity.
	 */
	private Dataset entity1; 
	
	/**
	 * Right side of the link.
	 */
	private Dataset entity2;
	
	@Override
	public String getUri1() {
		return entity1.getUri();
	}

	@Override
	public String getUri2() {
		return entity2.getUri();
	}

	public Dataset getEntity1() {
		return entity1;
	}

	public void setEntity1(Dataset entity1) {
		this.entity1 = entity1;
	}

	public Dataset getEntity2() {
		return entity2;
	}

	public void setEntity2(Dataset entity2) {
		this.entity2 = entity2;
	}
}