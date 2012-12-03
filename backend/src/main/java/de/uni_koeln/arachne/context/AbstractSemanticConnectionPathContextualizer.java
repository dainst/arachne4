package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.ContextService;
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
	
	protected ContextPath contextPath;

	protected String contextualizerName =null;
	//TODO Advanced typeStepRestrictions with Array that act as white or Blacklists
	//TODO Restrict the the Special Values on the Way Like "Fundort" etc.
	
	
	
	public AbstractSemanticConnectionPathContextualizer() {
		this.contextPath = new ContextPath();
		this.setupContextPath();
		//Infere contextualizername!
		if(contextualizerName == null){
			
			String classname = this.getClass().toString();
			
			contextualizerName = classname.substring(classname.lastIndexOf(".")+1, classname.length()-"Contextualizer".length());
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
	public List<AbstractLink> retrieve(Dataset parent, Integer offset,
			Integer limit) {
		
		
		
		//TODO Implement limit offset restriction
		//TODO IF Preformence Problems Occure : Implement structure to optimize retrival technique by given paramters.
		
		List<Long> connectedEntities = this.genericSQLService.getPathConnectedEntityIds(parent.getArachneId().getArachneEntityID(),contextPath);
		//Retrival Succsessfull Then Build result
		if(connectedEntities != null){
			List<AbstractLink> out = new ArrayList<AbstractLink>(connectedEntities.size());
			for (Long long1 : connectedEntities) {
			
				final ArachneLink link = new ArachneLink();
				link.setEntity1(parent);
				if(long1 == 0)
					continue;
				
				EntityId tempident = this.entityIdentificationService.getId(long1);
				Dataset tempdataset 	= this.singleEntityDataService.getSingleEntityByArachneId(tempident);
				tempdataset.renameFieldsPrefix(contextualizerName);
				
				link.setEntity2(tempdataset);
				out.add(link);
			
			
			}
		
			return out;
		}
		
		//elsewise return null
		return null;
	}
	
}
