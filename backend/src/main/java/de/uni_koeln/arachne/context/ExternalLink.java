package de.uni_koeln.arachne.context;

import de.uni_koeln.arachne.response.ArachneDataset;

/**
 * This class is a specialized <code>Link</code> to hold external links. This means that the right side of the link
 * is an entity fetched from an external source while the left side is an entity fetched from the database.
 * The left side is represented as a <code>ArachneDataset</code> while the right side is represented as a URI.
 */
public class ExternalLink extends Link {
	
	/**
	 * Left side of the link. In general this is the parent entity.
	 */
	protected ArachneDataset entity;
	
	/**
	 * Right side of the link.
	 */
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
