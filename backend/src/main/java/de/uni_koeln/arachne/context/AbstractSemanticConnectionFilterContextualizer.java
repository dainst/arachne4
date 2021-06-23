package de.uni_koeln.arachne.context;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * Implementing classes can use this to retrive connected entities but filter
 * them depending on their own criteria. Three hooks are provided to this end:
 *
 * 1. getTargetName() -> From which table to retrieve datasets.
 *
 * 2. getContextType() -> How this context is named in the xmls.
 *
 * 3. filter() -> Which datasets to include in the result.
 */
abstract class AbstractSemanticConnectionFilterContextualizer extends AbstractContextualizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSemanticConnectionFilterContextualizer.class);

	// This class allows fieldnames to be properly translated to table names
	// when using context types that do not correspond to table names.
	private class ContextTypeTranslatingLink extends ArachneLink {

		ContextTypeTranslatingLink(ArachneLink al) {
			this.setEntity1(al.getEntity1());
			this.setEntity2(al.getEntity2());
		}

		@Override
		public String getFieldFromFields(String fieldName) {
			if (!StrUtils.isEmptyOrNull(fieldName) && fieldName.startsWith(getContextType())) {
				fieldName = fieldName.replaceFirst(getContextType(), getTargetName());
			}
			return super.getFieldFromFields(fieldName);
		}
	}

	/**
	 * Controls which table to retrieve linked datasets from.
	 *
	 * @return A string value from SemanticConnection.TypeTarget.
	 */
	public abstract String getTargetName();

	/**
	 * Controls which datasets will be included in the result.
	 *
	 * @param parent The primary dataset that is linked from.
	 * @param other  One of the secondary datasets that are linked to.
	 * @return Whether the <code>other</code> should be included in the result.
	 */
	public abstract boolean filter(Dataset parent, Dataset other);

	@Override
	public abstract String getContextType();

	@Override
	public List<AbstractLink> retrieve(Dataset parent) {
		final List<Dataset> datasets = SemanticConnectionsUtil.getConnectedEntitiesAsDatasets(genericSQLDao,
				parent.getArachneId().getArachneEntityID(), getTargetName());

		final List<Dataset> filtered = datasets.stream().filter(ds -> filter(parent, ds)).collect(Collectors.toList());

		LOGGER.debug(String.format("Filtered datasets of context %s: %d/%d", getContextType(), filtered.size(),
				datasets.size()));

		List<ArachneLink> result = SemanticConnectionsUtil.createArachneLinks(parent, filtered);

		// Ensure that fields can be retrieved even if the context type is not the table
		// name.
		if (getTargetName() != getContextType()) {
			result = result.stream().map(link -> new ContextTypeTranslatingLink(link)).collect(Collectors.toList());
		}

		return result.stream().map(link -> (AbstractLink) link).collect(Collectors.toList());
	}

}
