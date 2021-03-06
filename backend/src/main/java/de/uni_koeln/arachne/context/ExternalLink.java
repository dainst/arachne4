package de.uni_koeln.arachne.context;

import de.uni_koeln.arachne.response.Dataset;

/**
 * This class is a specialized <code>Link</code> to hold external links. This means that the right side of the link
 * is an entity fetched from an external source while the left side is an entity fetched from the database.
 * The left side is represented as a <code>ArachneDataset</code> while the right side is represented as a URI.
 */
public class ExternalLink extends AbstractLink {
	
	/**
	 * Left side of the link. In general this is the parent entity.
	 */
	protected Dataset entity;
	
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

	public Dataset getEntity() {
		return entity;
	}

	public void setEntity(final Dataset entity) {
		this.entity = entity;
	}

	public void setUri2(final String uri2) {
		this.uri2 = uri2;
	}

	@Override
	public Dataset getEntity1() {
		return getEntity();
	}

	@Override
	public Dataset getEntity2() {
		return null;
	}
}
