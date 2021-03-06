package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;

public class PersoncompleteContextualizer extends AbstractContextualizer {

    @Override
    public String getContextType() {
        return "personcomplete";
    }

    @Override
    public List<AbstractLink> retrieve(final Dataset parent) {
        final List<AbstractLink> result = new ArrayList<AbstractLink>();

        // data from the "person" table
        final List<Map<String, String>> perContextContents = genericSQLDao.getPersonsByCollectionId(
        		parent.getArachneId().getInternalKey());

        // check if the books exist in Arachne and construct corresponding link
        if (perContextContents != null) {
            final ListIterator<Map<String, String>> contextMap = perContextContents.listIterator();
            while (contextMap.hasNext()) {
                Map<String, String> context = contextMap.next();
                try {
                    final String entityId = context.get("arachneentityidentification.ArachneEntityID");
                    if (!StrUtils.isEmptyOrNull(entityId)) {
                        context.put(getContextType() + ".arachneLinkStart", "<a href=\"" + Dataset.BASEURI
                                + String.valueOf(entityId) + "\">");
                        context.put(getContextType() + ".arachneLinkEnd", "</a>");
                    }
                } catch (Exception e) {
                    // nothing to do here, just move on
                }
                final ArachneLink link = new ArachneLink();
                link.setEntity1(parent);
                link.setEntity2(createDatasetFromQueryResults(context));
                result.add(link);
            }
        }

        return result;
    }

    /**
     * Creates a new dataset which is a context from the results of an SQL query.
     * @param map The SQL query result.
     * @return The newly created dataset.
     */
    private Dataset createDatasetFromQueryResults(final Map<String, String> map) {

        final Dataset result = new Dataset();

        long foreignKey = 0L;
        long eId = 0L;

        final Map<String, String> resultMap = new HashMap<String, String>();
        for (final Map.Entry<String, String> entry: map.entrySet()) {
            final String key = entry.getKey();
            if (!(key.contains("PS_") && key.contains("ID")) && !(key.contains("Source")) && !(key.contains("Type"))) {
                // get ArachneEntityID from context query result
                if ("SemanticConnection.Target".equals(key)) {
                    eId = Long.parseLong(entry.getValue());
                    continue;
                } else if ("SemanticConnection.ForeignKeyTarget".equals(key)) {
                    foreignKey = Long.parseLong(entry.getValue());
                    continue;
                }
                resultMap.put(getContextType() + '.' + key.split("[.]")[1], entry.getValue());
            }
        }

        final EntityId entityId = new EntityId(getContextType(), foreignKey, eId, false, null, 0L);
        result.setArachneId(entityId);
        result.appendFields(resultMap);
        return result;
    }
}
