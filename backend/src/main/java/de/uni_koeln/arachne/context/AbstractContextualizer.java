package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.service.EntityIdentificationService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.service.SimpleSQLService;
import de.uni_koeln.arachne.service.SingleEntityDataService;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * Base class for all contextualizers. It implements the service setters needed to correctly work with the automatic 
 * instantiation mechanism of contextualizers via reflection. 
 */
public abstract class AbstractContextualizer implements IContextualizer {

	protected transient EntityIdentificationService entityIdentificationService;
	
	protected transient GenericSQLDao genericSQLDao;
	
	protected transient SingleEntityDataService singleEntityDataService;
	
	protected transient UserRightsService rightsService;
	
	protected transient XmlConfigUtil xmlConfigUtil;
	
	protected transient SimpleSQLService simpleSQLService;
	
	/**
	 * Sets the entity identification service.
	 * @param entityIdentificationService The service instance.
	 */
	public void setEntityIdentificationService(final EntityIdentificationService entityIdentificationService) {
		this.entityIdentificationService = entityIdentificationService;
	}

	/**
	 * Sets the generic sql dao.
	 * @param genericSQLDao The DAO instance.
	 */
	public void setGenericSQLService(final GenericSQLDao genericSQLDao) {
		this.genericSQLDao = genericSQLDao;
	}

	/**
	 * Sets the single entity data service.
	 * @param singleEntityDataService The service instance.
	 */
	public void setSingleEntityDataService(final SingleEntityDataService singleEntityDataService) {
		this.singleEntityDataService = singleEntityDataService;
	}

	/**
	 * Sets the user rights service.
	 * @param rightsService The service instance.
	 */
	public void setRightsService(final UserRightsService rightsService) {
		this.rightsService = rightsService;
	}

	/**
	 * Sets the XML config utility.
	 * @param xmlConfigUtil The class instance.
	 */
	public void setXmlConfigUtil(final XmlConfigUtil xmlConfigUtil) {
		this.xmlConfigUtil = xmlConfigUtil;
	}

	/**
	 * Sets the simple SQL service.
	 * @param simpleSQLService The service instance.
	 */
	public void setSimpleSQLService(final SimpleSQLService simpleSQLService) {
		this.simpleSQLService = simpleSQLService;
	}
	
	@Override
	public abstract String getContextType();

	@Override
	public abstract List<AbstractLink> retrieve(Dataset parent);

}
