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
 * but NOT based on the SemanticConnections table in the DB but by Information from the XML files.
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

        final long queryTime = System.currentTimeMillis();
		final List<Map<String, String>> contextContents = genericSQLDao.getConnectedEntitiesJoint(contextType, parent,
				jointContextDefinition);
        LOGGER.debug("Query time: " + (System.currentTimeMillis() - queryTime) + " ms");

        if ((jointContextDefinition.isGrouped())) {
            return retrieveGrouped(parent, contextContents);
        } else {
            return retrieveUngrouped(parent, contextContents);
        }
    }

    /**
     * normal way, like semanticConnectionContextualizer
     * @param parent
     * @param contextContents
     * @return
     */
    private List<AbstractLink> retrieveUngrouped(Dataset parent, List<Map<String, String>> contextContents) {
        final List<AbstractLink> result = new ArrayList<AbstractLink>();

        if (contextContents != null) {
            final ListIterator<Map<String, String>> contextMap = contextContents.listIterator();

            while (contextMap.hasNext()) {
                final ArachneLink link = new ArachneLink();
                link.setEntity2(createDatasetFromQueryResults(contextMap.next(), contextType + "." ));
                link.setEntity1(parent);
                link.setFields(link.getEntity2().getFields());
                result.add(link);
            }
        }
        return result;
    }

    /**
     * when in the jointContext-Definition a group item is used we can create context-items wich has context-items itself
     * @param parent
     * @param contextContents
     * @return
     */
    private List<AbstractLink> retrieveGrouped(Dataset parent, List<Map<String, String>> contextContents) {

        final List<AbstractLink> result = new ArrayList<AbstractLink>();

        final String groupName = jointContextDefinition.getGroupName();
        final String groupBy = jointContextDefinition.getGroupBy();
        final HashMap<String, ArrayList<Dataset>> groups = new HashMap<String, ArrayList<Dataset>>();

        if (contextContents != null) {
            final ListIterator<Map<String, String>> contextMap = contextContents.listIterator();
            while (contextMap.hasNext()) {
                final Dataset row = createDatasetFromQueryResults(contextMap.next(), "");
                final String groupByKey = row.getField(groupBy);
                if (!groups.containsKey(groupByKey)) {
                    groups.put(groupByKey, new ArrayList<Dataset>());
                }
                groups.get(groupByKey).add(row);
            }
        }

        final ArrayList<ArachneLink> linkList = new ArrayList<ArachneLink>();

        for (final String key : groups.keySet()) {
            final ArrayList<Dataset> group = groups.get(key);

            final Dataset groupEntity = createGroupEntity(group.get(0), groupName);

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
            linkList.add(link);
        }

        //results were sorted by sql BUT this got lost through grouping, so we have to sort again
        if ((jointContextDefinition.getOrderBy() != null) && !jointContextDefinition.getOrderBy().equals("")) {
            final Comparator<ArachneLink> comparator = jointContextDefinition.getOrderDescending() ?
                    new ArachneLinkComparator(jointContextDefinition.getOrderBy()).reversed() :
                    new ArachneLinkComparator(jointContextDefinition.getOrderBy());
            Collections.sort(linkList, comparator);
        }
        result.addAll(linkList);

        parent.setAdditionalContent(new AdditionalContent());
        return result;
    }

    private Dataset createGroupEntity(final Dataset mergedDataset, String groupName) {
        final Dataset groupEntity = new Dataset();
        groupEntity.setAdditionalContent(new AdditionalContent());

        Map<String, String> prefixed = new HashMap<String, String>();
        for (final String key : mergedDataset.getFields().keySet()) {
            if ((groupName == null) || (!key.startsWith(groupName + "."))) {
                prefixed.put(contextType + "." + key, mergedDataset.getField(key));
            }
        }
        groupEntity.appendFields(prefixed);
        return groupEntity;
    }


    class ArachneLinkComparator implements Comparator<ArachneLink> {

        public String orderBy;

        public ArachneLinkComparator(String orderBy) {
            super();
            this.orderBy = orderBy;
        }

        @Override
        public int compare(ArachneLink n1, ArachneLink n2){
            return n1.getEntity2().getField(orderBy).compareToIgnoreCase(n2.getEntity2().getField(orderBy));
        }

    }


    /**
     * Creates a new dataset which is a context from the results of an SQL query.
     * @param map The SQL query result.
     * @return The newly created dataset.
     */
    private Dataset createDatasetFromQueryResults(final Map<String, String> map, final String prefix) {

        final Dataset result = new Dataset();

        long foreignKey = 0L;
        long eId = 0L;

        final Map<String, String> resultMap = new HashMap<String, String>();
        for (final Map.Entry<String, String> entry: map.entrySet()) {
            final String key = entry.getKey();
            resultMap.put(prefix + key, entry.getValue());
        }

        final EntityId entityId = new EntityId(contextType, foreignKey, eId, false, 0L);
        result.setArachneId(entityId);
        result.appendFields(resultMap);
        return result;
    }

}