package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.GenericSQLService;
import de.uni_koeln.arachne.service.SingleEntityDataService;

/**
 * Base class for all contextualizers. It implements the service setters needed to correctly work with the automatic instantiation
 *  mechanism of contextualizers via reflection. 
 */
public abstract class AbstractContextualizer implements IContextualizer {

	protected transient EntityIdentificationService entityIdentificationService;
	
	protected transient GenericSQLService genericSQLService;
	
	protected transient SingleEntityDataService singleEntityDataService;
	
	public void setEntityIdentificationService(final EntityIdentificationService entityIdentificationService) {
		this.entityIdentificationService = entityIdentificationService;
	}

	public void setGenericSQLService(final GenericSQLService genericSQLService) {
		this.genericSQLService = genericSQLService;
	}

	public void setSingleEntityDataService(final SingleEntityDataService singleEntityDataService) {
		this.singleEntityDataService = singleEntityDataService;
	}

	@Override
	public abstract String getContextType();

	@Override
	public abstract List<AbstractLink> retrieve(Dataset parent, Integer offset,	Integer limit);

}
