package de.uni_koeln.arachne.context;

import java.util.List;
import java.util.stream.Collectors;

import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;

/**
 * This contextualizer retrieves internal contexts (tables in the arachne
 * database).
 */
public class SemanticConnectionsContextualizer implements IContextualizer {

	/**
	 * The type of <code>Context<code> the <code>Contextualizer</code> retrieves.
	 */
	private transient final String contextType;

	private transient final GenericSQLDao genericSQLDao;

	public SemanticConnectionsContextualizer(final String contextType, final GenericSQLDao genericSQLDao) {
		this.contextType = contextType;
		this.genericSQLDao = genericSQLDao;
	}

	@Override
	public String getContextType() {
		return contextType;
	}

	@Override
	public List<AbstractLink> retrieve(final Dataset parent) {
		final List<Dataset> linkedDatasets = SemanticConnectionsUtil.getConnectedEntitiesAsDatasets(genericSQLDao,
				parent.getArachneId().getArachneEntityID(), contextType);

		List<ArachneLink> links = SemanticConnectionsUtil.createArachneLinks(parent, linkedDatasets);

		// Return links cast to the the abstract base class
		return links.stream().map(link -> (AbstractLink) link).collect(Collectors.toList());
	}
}
