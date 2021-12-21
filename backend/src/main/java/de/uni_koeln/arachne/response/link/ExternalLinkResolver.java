package de.uni_koeln.arachne.response.link;

import de.uni_koeln.arachne.response.Dataset;

/**
 * Link resolvers create <code>ExternalLink</code>s based on a given <code>Dataset</code>.
 * @author Sebastian Cuy.
 * @author Reimar Grabowski
 */
public interface ExternalLinkResolver {
	
	/**
	 * Create an <code>ExternalLink</code>.
	 * @param dataset dataset representing an entity
	 * @return The external link.
	 */
	public ExternalLink resolve(Dataset dataset);

}
