package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;

public class MarbilderbestandortContextualizer extends AbstractContextualizer {
	
	protected static final String SQL1 = "SELECT * FROM `ortsbezug_leftjoin_ort` WHERE `FS_MarbilderbestandID` = ";

	@Override
	public String getContextType() {
		return "ort";
	}
	
	public List<AbstractLink> retrieve(Dataset parent) {
		
		final List<AbstractLink> result = new ArrayList<AbstractLink>();
		List<Map<String, Object>> queryResults = simpleSQLService.getJDBCTemplate().queryForList(SQL1 + parent.getField("marbilderbestand.PS_MarbilderbestandID"));
		
		// queryForList may return a list with only a null value - so we need to check for that
		if (!queryResults.isEmpty() && queryResults.get(0) != null) {
			for (final Map<String,Object> queryResult : queryResults) {
				final ArachneLink link = new ArachneLink();
				link.setEntity1(parent);
				link.setEntity2(createDatasetFromQueryResults(queryResult));
				result.add(link);
			}
		}
		
		return result;
		
	}

	private Dataset createDatasetFromQueryResults(final Map<String,Object> queryResult) {
		final Map<String, String> resultMap = new HashMap<String, String>();
		for (Map.Entry<String,Object> entry : queryResult.entrySet()) {
			Object value = entry.getValue();
			if (value != null && !value.toString().isEmpty()) {
				resultMap.put("ort." + entry.getKey(), entry.getValue().toString());
			}
		}
		System.out.println(resultMap.toString());
		final EntityId entityId = new EntityId("ort", -1L, -1L, false);
		final Dataset result = new Dataset();
		result.setArachneId(entityId);
		result.appendFields(resultMap);
		return result;
	}

}
