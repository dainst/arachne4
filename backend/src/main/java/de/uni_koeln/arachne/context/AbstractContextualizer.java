package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.GenericSQLService;
import de.uni_koeln.arachne.service.SingleEntityDataService;

/**
 * Base class for all contextualizers. It implements the default constructor needed to correctly work with the automatic instantiation
 *  mechanism of contextualizers via reflection. 
 */
public abstract class AbstractContextualizer implements IContextualizer {

	protected transient final EntityIdentificationService entityIdentificationService;
	
	protected transient final GenericSQLService genericSQLService;
	
	protected transient final SingleEntityDataService singleEntityDataService;
	
	public AbstractContextualizer(final EntityIdentificationService entityIdentificationService
			, final GenericSQLService genericSQLService, final SingleEntityDataService singleEntityDataService) {
		this.entityIdentificationService = entityIdentificationService;
		this.genericSQLService = genericSQLService;
		this.singleEntityDataService = singleEntityDataService;
	}
	
	@Override
	public abstract String getContextType();

	@Override
	public abstract List<AbstractLink> retrieve(Dataset parent, Integer offset,	Integer limit);

}
