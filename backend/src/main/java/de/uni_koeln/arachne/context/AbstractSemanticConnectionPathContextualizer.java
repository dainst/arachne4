package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;
/**
 * This abstract Class Provides a simple prototype to Control the Retrival of Contexts that Use more than one Category Step. 
 *	The First Step is to Provide a Simple Category path Like "Parent ->Ort->Buch". Every Book That Linkt over an Place to the core.
 * Everything that uses this Contextualisation Method has to implement the setupContextPath(); Function. 
 * There should be a Defintition of how "What" Should be LinkedHow. 
 */
public abstract class AbstractSemanticConnectionPathContextualizer extends AbstractContextualizer {
	
	
	//This Restricts the Types that Lie on the Path Now Its Only fixed Types AND "ALL" For Every Type
	protected List<String> typeStepRestrictions = null;

	//TODO Advanced typeStepRestrictions with Array that act as white or Blacklists
	//TODO Restrict the the Special Values on the Way Like "Fundort" etc.
	
	
	
	public AbstractSemanticConnectionPathContextualizer() {

		this.setupContextPath();
	}
	
	@Override
	public abstract String getContextType();
	/**
	 * This method has to be declared in the Initialised Contextualizer. It setups the path that has to be followed to geht the Desired Context 
	 */
	protected abstract void setupContextPath();
	
	@Override
	public List<AbstractLink> retrieve(Dataset parent, Integer offset,
			Integer limit) {
		
		//TODO Implement limit offset restriction
		//TODO IF Preformence Problems Occure : Implement structure to optimize retrival technique by given paramters.
		
		List<Long> connectedEntities = this.genericSQLService.getPathConnectedEntityIds(parent.getArachneId().getArachneEntityID(),typeStepRestrictions);
		//Retrival Succsessfull Then Build result
		if(connectedEntities != null){
			List<AbstractLink> out = new ArrayList<AbstractLink>(connectedEntities.size());
			for (Long long1 : connectedEntities) {
			
				final ArachneLink link = new ArachneLink();
				link.setEntity1(parent);
				if(long1 == 0)
					continue;
				
				EntityId tempident = this.entityIdentificationService.getId(long1);
				Dataset tempentity = this.singleEntityDataService.getSingleEntityByArachneId(tempident);
				link.setEntity2(tempentity);
				out.add(link);
			
			
			}
		
			return out;
		}
		
		//elsewise return null
		return null;
	}
	
}
