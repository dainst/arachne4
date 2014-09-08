package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;

/**
 * Adds the 'zenon' context of the 'buch' context to the 'object' dataset as nativ context, so that it is available
 * to add the correct title of the book as literature to the object dataset.
 */
public class CustompathobjektzenontitleContextualizer extends
		AbstractContextualizer {

	protected static final String SQL1 = "SELECT `zenon`.`245_a` FROM `SemanticConnection` `e`, `buch` `b` LEFT JOIN `zenon` ON `zenon`.`001` = `b`.`bibid` WHERE 1 AND `e`.`Source` = ";
	protected static final String SQL2 = " AND `e`.`TypeTarget` = \"buch\" AND `b`.`PS_BuchID` = `e`.`ForeignKeyTarget`"; 
	
	public CustompathobjektzenontitleContextualizer() {
		super();
	}

	@Override
	public String getContextType() {
		return "zenon";
	}

	@Override
	public List<AbstractLink> retrieve(Dataset parent) {
		final List<AbstractLink> result = new ArrayList<AbstractLink>();
		final List<String> queryResults = simpleSQLService.getJDBCTemplate().queryForList(
				SQL1 + parent.getArachneId().getArachneEntityID() + SQL2, String.class);
		
		// queryForList may return a list with only a null value - so we need to check for that
		if (!StrUtils.isEmptyOrNull(queryResults) && queryResults.get(0) != null) {
			for (final String queryResult: queryResults) {
				final ArachneLink link = new ArachneLink();
				link.setEntity1(parent);
				link.setEntity2(createDatasetFromQueryResults(queryResult));
				result.add(link);
			}
		}
		return result;
	}

	private Dataset createDatasetFromQueryResults(final String queryResult) {
		final Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("zenon.245_a", queryResult);
		final EntityId entityId = new EntityId("zenon", -1L, -1L, false, -1L);
		final Dataset result = new Dataset();
		result.setArachneId(entityId);
		result.appendFields(resultMap);
		return result;
	}
		
}
