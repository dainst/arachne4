package de.uni_koeln.arachne.context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;

class SemanticConnectionsUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticConnectionsUtil.class);

	/**
	 * Convenience method to retrive entities via the SemanticConnections table and
	 * hava a debug log of the query time.
	 */
	static List<Map<String, String>> getConnectedEntitiesLogging(final GenericSQLDao dao, final Long sourceEntityId,
			final String targetContext) {
		final long queryTime = System.currentTimeMillis();
		final List<Map<String, String>> result = dao.getConnectedEntities(targetContext, sourceEntityId);
		LOGGER.debug("Query time: " + (System.currentTimeMillis() - queryTime) + " ms");
		return result;
	}

	/**
	 * Lookup entites of type targetContext which are connected to sourceEntityId
	 * and return them as Datasets.
	 *
	 * @param dao            The dao to use for querying.
	 * @param sourceEntityId The entity to look for connections from. An arachne
	 *                       entity id.
	 * @param targetContext  The type of connected entities to look up.
	 * @return A list of Dataset objects connected to the sourceId.
	 */
	static List<Dataset> getConnectedEntitiesAsDatasets(final GenericSQLDao dao, final Long sourceEntityId,
			final String targetContext) {
		final List<Dataset> result = new LinkedList<>();
		final List<Map<String, String>> queryResults = getConnectedEntitiesLogging(dao, sourceEntityId, targetContext);
		if (queryResults != null) {
			for (Map<String, String> map : queryResults) {
				result.add(createDatasetFromQueryResults(map, targetContext));
			}
		}
		return result;
	}

	/**
	 * Creates a new dataset which is a context from the results of an SQL query.
	 *
	 * @param queryResult The SQL query result.
	 * @param tableName   The tableName used for the entity.
	 * @return The newly created dataset.
	 */
	static Dataset createDatasetFromQueryResults(final Map<String, String> queryResult, final String tableName) {

		final Dataset result = new Dataset();

		long foreignKey = 0L;
		long eId = 0L;

		final Map<String, String> resultMap = new HashMap<String, String>();
		for (final Map.Entry<String, String> entry : queryResult.entrySet()) {
			final String key = entry.getKey();

			// get ArachneEntityID from context query result
			// Workaround for shitty case insensitiv table names on OSX
			if ("SemanticConnection.Target".equals(key) || "semanticconnection.Target".equals(key)) {
				eId = Long.parseLong(entry.getValue());
				continue;
			} else if ("SemanticConnection.ForeignKeyTarget".equals(key)
					|| "semanticconnection.ForeignKeyTarget".equals(key)) {
				foreignKey = Long.parseLong(entry.getValue());
				continue;
			}

			resultMap.put(key, entry.getValue());
		}

		final EntityId entityId = new EntityId(tableName, foreignKey, eId, false, null, 0L);
		result.setArachneId(entityId);
		result.appendFields(resultMap);
		return result;
	}

	/**
	 * Create <code>ArachneLink</code> objects that contain the parent Dataset as
	 * the first and the other Datasets as the second entity.
	 *
	 * @param parent The Dataset to use on as the first entity in the lLinks.
	 * @param others The Datasets to use as the second entities in the links.
	 * @return A list of ArachneLinks connecting the parent to the other datasets.
	 */
	static List<ArachneLink> createArachneLinks(final Dataset parent, final List<Dataset> others) {
		final List<ArachneLink> result = new LinkedList<>();
		for (final Dataset other : others) {
			final ArachneLink link = new ArachneLink();
			link.setEntity1(parent);
			link.setEntity2(other);
			result.add(link);
		}
		return result;
	}
}
