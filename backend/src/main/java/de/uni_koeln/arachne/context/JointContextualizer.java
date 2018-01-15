package de.uni_koeln.arachne.context;

import java.util.*;

import de.uni_koeln.arachne.mapping.hibernate.ArachneEntity;
import de.uni_koeln.arachne.response.AdditionalContent;
import de.uni_koeln.arachne.service.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.crypto.Data;

/**
 * This contextualizer retrieves internal contexts (tables in the arachne database).
 * but NOT based on waht you find in the semConnectionsTable in the db but by Information from the XMLs
 *
 */
public class JointContextualizer extends AbstractContextualizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JointContextualizer.class);

    protected transient String contextType;

    protected transient JointContextDefinition jointContextDefinition;

    public JointContextualizer(JointContextDefinition jointContextDefinition, GenericSQLDao genericSQLDao) {
        this.jointContextDefinition = jointContextDefinition;
        this.genericSQLDao = genericSQLDao;
        this.contextType = jointContextDefinition.getId();
    }

    @Override
    public String getContextType() {
        return contextType;
    }

    @Override
    public List<AbstractLink> retrieve(final Dataset parent) {
        final List<AbstractLink> result = new ArrayList<AbstractLink>();

        String groupName = "surfacetreatmentaction";
        String groupBy = "surfacetreatment.PS_SurfaceTreatmentID";
        HashMap<String, ArrayList<Dataset>> groups = new HashMap<String, ArrayList<Dataset>>();

        final long queryTime = System.currentTimeMillis();
        final List<Map<String, String>> contextContents = genericSQLDao.getConnectedEntitiesJoint(contextType, parent, jointContextDefinition);
        LOGGER.debug("Query time: " + (System.currentTimeMillis() - queryTime) + " ms");

        if (contextContents != null) {
            final ListIterator<Map<String, String>> contextMap = contextContents.listIterator();
            while (contextMap.hasNext()) {
                final Dataset row = createDatasetFromQueryResults(contextMap.next());
                final String groupByKey = row.getField(groupBy);
                if (!groups.containsKey(groupByKey)) {
                    groups.put(groupByKey, new ArrayList<Dataset>());
                }
                groups.get(groupByKey).add(row);
            }
        }

        for (final String key : groups.keySet()) {
            final ArrayList<Dataset> group = groups.get(key);

            final Dataset groupEntity = new Dataset();
            groupEntity.appendFields(prefixFields(group.get(0).getFields()));
            groupEntity.setAdditionalContent(new AdditionalContent());

            final Iterator<Dataset> groupIterator = group.iterator();
            final ArrayList<AbstractLink> slist = new ArrayList<AbstractLink>();
            while (groupIterator.hasNext()) {
                final Dataset subEntity = groupIterator.next();
                subEntity.setAdditionalContent(new AdditionalContent());
                final ArachneLink slink = new ArachneLink();
                slink.setEntity1(groupEntity);
                slink.setEntity2(subEntity);
                slist.add(slink);
            }
            groupEntity.addContext(new Context(groupName, groupEntity, slist));

            final ArachneLink link = new ArachneLink();
            link.setEntity1(parent);
            link.setEntity2(groupEntity);
            result.add(link);
        }

        parent.setAdditionalContent(new AdditionalContent());
        return result;
    }

    private Map<String, String> prefixFields(final Map<String, String> fields) {
        Map<String, String> prefixed = new HashMap<String, String>();
        for (final String key : fields.keySet()) {
            prefixed.put(key, fields.get(key));
        }
        return prefixed;
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

            resultMap.put(key, entry.getValue());

        }

        final EntityId entityId = new EntityId(contextType, foreignKey, eId, false, 0L);
        result.setArachneId(entityId);
        result.appendFields(resultMap);
        return result;
    }

}
