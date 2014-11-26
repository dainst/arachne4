package de.uni_koeln.arachne.response.link;

/**
 * Link resolvers create <code>ExternalLink</code>s based on a given <code>Dataset</code>.
 */
import de.uni_koeln.arachne.response.Dataset;

public interface ExternalLinkResolver {
	
	/**
	 * Create an <code>ExternalLink</code>.
	 * @param dataset dataset representing an entity
	 */
	public ExternalLink resolve(Dataset dataset);

}
