package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;
/**
 * This abstract Class Provides a simple prototype to Control the Retrival of Contexts that Use more than one Category Step. 
 *	The First Step is to Provide a Simple Category path Like "Parent ->Ort->Buch". Every Book That Linkt over an Place to the core.
 * Everything that uses this Contextualisation Method has to implement the setupContextPath(); Function. 
 * There should be a Defintition of how "What" Should be LinkedHow. 
 */
public abstract class AbstractSemanticConnectionPathContextualizer extends AbstractContextualizer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSemanticConnectionPathContextualizer.class);
	//This Restricts the Types that Lie on the Path Now Its Only fixed Types AND "ALL" For Every Type
	
	transient protected ContextPath contextPath;

	transient protected String contextualizerName =null;
	//TODO Advanced typeStepRestrictions with Array that act as white or Blacklists
	//TODO Restrict the the Special Values on the Way Like "Fundort" etc.
	
	private transient long linkCount = 0l;
	
	public AbstractSemanticConnectionPathContextualizer() {
		this.contextPath = new ContextPath();
		this.setupContextPath();
		//Infere contextualizername!
		if(contextualizerName == null){
			
			final String classname = this.getClass().toString();
			
			contextualizerName = classname.substring(classname.lastIndexOf('.')+1, classname.length()-"Contextualizer".length());
			LOGGER.debug("Infered Name "+contextualizerName);
			
		}
	}
	
	@Override
	public String getContextType(){
		return this.contextPath.getTargetType();
	}
	/**
	 * This method has to be declared in the Initialised Contextualizer. It setups the path that has to be followed to geht the Desired Context 
	 */
	protected abstract void setupContextPath();
	
	@Override
	public List<AbstractLink> retrieve(final Dataset parent, final Integer offset,
			final Integer limit) {
		
		
		
		//TODO Implement limit offset restriction
		//TODO IF Preformence Problems Occure : Implement structure to optimize retrival technique by given paramters.
		
		final List<AbstractLink> result = new ArrayList<AbstractLink>();
		
		final List<Map<String, String>> contextContents = this.genericSQLService.getPathConnectedEntities(parent.getArachneId().getArachneEntityID(),contextPath);
		//Retrival Succsessfull Then Build result
		 if (contextContents != null) {
				final ListIterator<Map<String, String>> contextMap = contextContents.listIterator(offset);
				while (contextMap.hasNext() && (linkCount < limit || limit == -1)) {
					final ArachneLink link = new ArachneLink();
					link.setEntity1(parent);
					
					// write entity-id of context into its dataset
					final Dataset contextDataset = createDatasetFromQueryResults(contextMap.next());
					String contextualizerClassName = this.getClass().getSimpleName();
					contextualizerClassName = contextualizerClassName.replace("Contextualizer", "");
					final Map<String, String> contextIdMap = new HashMap<String, String>(1);
					contextIdMap.put(contextualizerClassName + "." + "EntityID", String.valueOf(contextDataset.getArachneId().getArachneEntityID()));
					contextDataset.appendFields(contextIdMap);
				
					link.setEntity2(contextDataset);
					result.add(link);
					linkCount++;
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
		
		Long foreignKey = 0L;
		Long eId = 0L;

		final Map<String, String> resultMap = new HashMap<String, String>();
		for (final Map.Entry<String, String> entry: map.entrySet()) {
			final String key = entry.getKey();
			if (!(key.contains("PS_") && key.contains("ID")) && !(key.contains("Source")) && !(key.contains("Type"))) {
				// get ArachneEntityID from context query result  
				if (key.endsWith("EntityID")) {
					eId = Long.parseLong(entry.getValue()); 
					continue;
				} else if (key.endsWith("ForeignKeyTarget")) {
					foreignKey = Long.parseLong(entry.getValue());
					continue;
				} 
				final String newkey = contextualizerName+key.substring( key.lastIndexOf('.'),key.length());
				resultMap.put(newkey, entry.getValue());
			}
		}

		final EntityId entityId = new EntityId(contextPath.getTargetType(), foreignKey, eId, false);
		result.setArachneId(entityId);
		result.appendFields(resultMap);
		return result;
	}
}
