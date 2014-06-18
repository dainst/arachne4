package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.dao.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.IUserRightsService;
import de.uni_koeln.arachne.service.SimpleSQLService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * Base class for all contextualizers. It implements the service setters needed to correctly work with the automatic instantiation
 *  mechanism of contextualizers via reflection. 
 */
public abstract class AbstractContextualizer implements IContextualizer {

	protected transient EntityIdentificationService entityIdentificationService;
	
	protected transient GenericSQLDao genericSQLDao;
	
	protected transient SingleEntityDataService singleEntityDataService;
	
	protected transient IUserRightsService rightsService;
	
	protected transient XmlConfigUtil xmlConfigUtil;
	
	protected transient SimpleSQLService simpleSQLService;
	
	public void setEntityIdentificationService(final EntityIdentificationService entityIdentificationService) {
		this.entityIdentificationService = entityIdentificationService;
	}

	public void setGenericSQLService(final GenericSQLDao genericSQLDao) {
		this.genericSQLDao = genericSQLDao;
	}

	public void setSingleEntityDataService(final SingleEntityDataService singleEntityDataService) {
		this.singleEntityDataService = singleEntityDataService;
	}

	public void setRightsService(final IUserRightsService rightsService) {
		this.rightsService = rightsService;
	}

	public void setXmlConfigUtil(final XmlConfigUtil xmlConfigUtil) {
		this.xmlConfigUtil = xmlConfigUtil;
	}

	public void setSimpleSQLSerive(final SimpleSQLService simpleSQLService) {
		this.simpleSQLService = simpleSQLService;
	}
	
	@Override
	public abstract String getContextType();

	@Override
	public abstract List<AbstractLink> retrieve(Dataset parent);

}
