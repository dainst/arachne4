package de.uni_koeln.arachne.response;

/**
 * Base class for content added to <code>FormattedArachneEntitiy</code>.
 * This class is used both as an entity and as a container for entities.
 */
public abstract class Content {
	/**
	 * The labelKey used by the frontend.
	 */
	protected String label;

	public String getLabelKey() {
		return label;
	}

	public void setLabelKey(String labelKey) {
		this.label = labelKey;
	}
}
