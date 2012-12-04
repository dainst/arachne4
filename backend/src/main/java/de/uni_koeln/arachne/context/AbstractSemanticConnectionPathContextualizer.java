package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

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
	
	protected transient ContextPath contextPath;

	protected transient String contextualizerName = null;
	//TODO Advanced typeStepRestrictions with Array that act as white or Blacklists
	//TODO Restrict the the Special Values on the Way Like "Fundort" etc.
	
	
	
	public AbstractSemanticConnectionPathContextualizer() {
		this.contextPath = new ContextPath();
		this.setupContextPath();
		//Infere contextualizername!
		if (contextualizerName == null) {
			
			final String classname = this.getClass().toString();
			
			contextualizerName = classname.substring(classname.lastIndexOf('.')+1, classname.length()-"Contextualizer".length());
			LOGGER.debug("Infered Name: " + contextualizerName);
			
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
	public List<AbstractLink> retrieve(final Dataset parent, final Integer offset, final Integer limit) {
				
		//TODO Implement limit offset restriction
		//TODO IF Preformence Problems Occure : Implement structure to optimize retrival technique by given paramters.
		
		final List<Long> connectedEntities = this.genericSQLService.getPathConnectedEntityIds(parent.getArachneId()
				.getArachneEntityID(),contextPath);
		//Retrival Succsessfull Then Build result
		if (connectedEntities != null) {
			final List<AbstractLink> out = new ArrayList<AbstractLink>(connectedEntities.size());
			for (Long long1 : connectedEntities) {
			
				final ArachneLink link = new ArachneLink();
				link.setEntity1(parent);
				if (long1 == 0) {
					continue;
				}
				
				final EntityId tempIdent = this.entityIdentificationService.getId(long1);
				final Dataset tempDataset = this.singleEntityDataService.getSingleEntityByArachneId(tempIdent);
				tempDataset.renameFieldsPrefix(contextualizerName);
				
				link.setEntity2(tempDataset);
				out.add(link);			
			}		
			return out;
		}
		
		//elsewise return null
		return null;
	}
	
}
