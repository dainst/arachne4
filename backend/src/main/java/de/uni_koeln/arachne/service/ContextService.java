package de.uni_koeln.arachne.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.arachne.context.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.dao.jdbc.GenericSQLDao;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.service.Transl8Service.Transl8Exception;
import de.uni_koeln.arachne.util.XmlConfigUtil;
import de.uni_koeln.arachne.util.image.ImageUtils;

/**
 * This class handles creation and retrieval of contexts and adds them to datasets.
 * Internally it uses <code>Contextualizers</code> to abstract the data access and allow to fetch contexts not only from
 * the Arachne database but from any other datasource (even external ones).  
 */
@Service("arachneContextService")
public class ContextService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextService.class);
	
	@Autowired
	private transient DataIntegrityLogService dataIntegrityLogService;
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService; 
	
	/**
	 * Service to access ids in 'cross tables'.
	 */
	@Autowired
	private transient GenericSQLDao genericSQLDao; 
	
	@Autowired
	private transient SingleEntityDataService singleEntityDataService;
	
	@Autowired
	private transient SimpleSQLService simpleSQLService;
	
	/**
	 * Utility class to work with the XML config files.
	 */
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	/**
	 * Service to access the current user.
	 */
	@Autowired
	private transient UserRightsService rightsService;
	
	@Autowired
	private transient Transl8Service ts;
	
	private transient Map<String, IContextualizer> contextualizers = new HashMap<String, IContextualizer>();
	
	/**
	 * This methods adds all contexts to the dataset that are found in the XML description. It also runs all contextualizers 
	 * that are marked as explicit in the corresponding xml config file and adds contextImages if neccessary.
	 * @param parent The dataset to add the contexts to.
	 * @param lang The language.
	 * @throws Transl8Exception if transl8 cannot be reached. 
	 */
	public void addMandatoryContexts(final Dataset parent, final String lang) throws Transl8Exception {
		// explicit contextualizers
		final List<String> explicitContextualizersList = xmlConfigUtil.getExplicitContextualizers(parent.getArachneId().getTableName());
		for (String contextualizerName: explicitContextualizersList) {
			final IContextualizer contextualizer = getContextualizerByContextType(contextualizerName);
			final Context context = new Context(contextualizer.getContextType(), parent, contextualizer.retrieve(parent));
			if (context.getSize() > 0) {
				parent.addContext(context);
			}
		}

		// explicit defined db contextualizers
		final List<JointContextDefinition> xmlDefinedContextualizersList = xmlConfigUtil.getJointContextualizers(parent.getArachneId().getTableName());
		final List<String> jointContextNames = new ArrayList<String>();
		for (JointContextDefinition jointContextDefinition: xmlDefinedContextualizersList) {
			final JointContextualizer contextualizer = new JointContextualizer(jointContextDefinition, genericSQLDao);
			final Context context = new Context(contextualizer.getContextType(), parent, contextualizer.retrieve(parent));
			jointContextNames.add(contextualizer.getContextType());
			if (context.getSize() > 0) {
				parent.addContext(context);
			}
		}

		// implicit contextualizers
		final List<String> mandatoryContextTypes = xmlConfigUtil.getMandatoryContextNames(parent.getArachneId().getTableName());
		LOGGER.debug("Mandatory Contexts: " + mandatoryContextTypes);
		for (final String contextType: mandatoryContextTypes) {
			if (jointContextNames.contains(contextType)) {
				continue;
			}
			final Context context = new Context(contextType, parent, getLinks(parent, contextType));
			if (context.getSize() > 0) {
				parent.addContext(context);
			}
		}

		// context images
		addContextImages(parent, lang);
	}
	
	/**
	 * Adds all context images to the dataset that are marked in the XML-Description if required. Retrieves
	 * additional contexts only if needed, uses the contexts to retrieve images.
	 * Does NOT add the additionally retrieved contexts to the parent dataset or to the retrievedContexts.
	 * @param parent The dataset to add the images to.
	 * @throws Transl8Exception if transl8 cannot be reached.
	 */
	private void addContextImages(final Dataset parent, final String lang) throws Transl8Exception {
		// add book cover image
		if ("buch".equals(parent.getArachneId().getTableName())) {
			Long coverPage = null;
			try {
				coverPage = Long.parseLong(parent.getField("buch.Cover"));
			} catch (NumberFormatException nfe) {
				coverPage = genericSQLDao.getBookCoverPage(parent.getArachneId().getInternalKey());
			}
			if (coverPage != null) {
				List<Image> imageList = (List<Image>)genericSQLDao.getImageList("buchseite", coverPage);
				if (imageList != null && !imageList.isEmpty()) {
					Image image = imageList.get(0);
					parent.addImage(image);
					parent.setThumbnailId(image.getImageId());
					return;
				} else {
					dataIntegrityLogService.logWarning(coverPage, "PS_BuchseiteID", "Book page 0 without image.");
				}
			}
			dataIntegrityLogService.logWarning(parent.getArachneId().getInternalKey(), "PS_BuchID", "No cover found.");
			return;
		}
		
		final List<ContextImageDescriptor> contextImages = xmlConfigUtil.getContextImagesNames(parent.getArachneId().getTableName());
		
		if (contextImages == null) {
			LOGGER.debug("No Context-Image-Declarations found.");
			return;
		}

		final List<Image> resultContextImages = new ArrayList<Image>();
		
		// check if the source-record contains any images
		boolean containsImages = false;
		if (parent.getImages() != null && !parent.getImages().isEmpty()) {
			containsImages = true;
		}

		for (final ContextImageDescriptor cur : contextImages) {

			// check contextImage-Preconditions from config
			if (cur.getContextImageUsage().equals("ifempty") && containsImages) {
				continue;
			}

			final String contextName = cur.getContextName();
			
			ContextPath contextPath;
			
			final IContextualizer contextualizer = getContextualizerByContextType(contextName);
			if (contextualizer instanceof AbstractSemanticConnectionPathContextualizer) {
				contextPath = ((AbstractSemanticConnectionPathContextualizer)contextualizer).getContextPath();
			} else {
				contextPath = new ContextPath();
				contextPath.addTypeStepRestriction(contextName);
			}
			contextPath.addTypeStepRestriction("marbilder");
			final List<Map<String, String>> contextContents = genericSQLDao.getPathConnectedEntities(
					parent.getArachneId().getArachneEntityID(), contextPath);
			
			if (contextContents != null) {
				for (final Map<String, String> currentContext : contextContents) {
					final Image image = new Image();
					try {
						LOGGER.debug("currentContext {}", currentContext);
						final long imageId = Long.parseLong(currentContext.get("SemanticConnection.EntityID"));
						image.setImageId(imageId);
						image.setImageSubtitle(currentContext.get("marbilder.DateinameMarbilder"));
						image.setEntityOrder(currentContext.get("marbilder.EntityOrder"));
						image.setSourceContext(ts.transl8(contextName, lang));
						final long sourceRecordId = Long.parseLong(currentContext.get("SemanticConnection.ForeignKeyTarget"));
						image.setSourceRecordId(sourceRecordId);
						resultContextImages.add(image);
					} catch (NumberFormatException nfe) {
						LOGGER.error("Failed to get connected image information [" + parent.getArachneId()
								.getArachneEntityID() + "]. Got 'SemanticConnection.EntityID' = " + currentContext
								.get("SemanticConnection.EntityID")	+ " - 'SemanticConnection.ForeignKeyTarget' = " 
								+ currentContext.get("SemanticConnection.ForeignKeyTarget"));
					}
				}
			}
		}
		LOGGER.debug("Adding " + resultContextImages.size() + " additional images from dataset-contexts...");
		parent.addImages(resultContextImages);

		// if no thumbnail has been set yet, use one from context
		if (!resultContextImages.isEmpty() && parent.getThumbnailId() == null) {
			parent.setThumbnailId(ImageUtils.findThumbnailId(resultContextImages));
		}
	}
		
	/**
	 * This function retrieves the contexts according to the given criteria.
	 * It uses a context specific contextualizer to fetch the data.
	 * @param parent Instance of an <code>ArachneDataset</code> that will receive the context
	 * @param contextType String that describes the context-type
	 * @return Returns a list of <code>Links</code> 
	 */ 
	public List<AbstractLink> getLinks(final Dataset parent, final String contextType) {
		final IContextualizer contextualizer = getContextualizerByContextType(contextType);
	    return contextualizer.retrieve(parent);
	}
	
	/**
	 * Method creating an appropriate contextualizer. The class name is constructed from the <code>contextType</code>.
	 * Then reflection is used to create the corresponding class instance.
	 * <br>
	 * If no specialized <code>Contextualizer</code> class is found an instance of <code>SemanticConnectionsContextualizer</code> 
	 * is returned.<br>
	 * Contextualizers are cached, so only unique instances are created and maintained.
	 * @param contextType Type of a context of interest  
	 * @return an appropriate contextualizer serving the specific context indicated by the given <code>contextType</code>
	 */
	@SuppressWarnings("rawtypes")
	private IContextualizer getContextualizerByContextType(String contextType) {
		IContextualizer result = contextualizers.get(contextType); 
		if (result == null) {
			final String upperCaseContextType = contextType.substring(0, 1).toUpperCase() + contextType.substring(1).toLowerCase();
			final String className = "de.uni_koeln.arachne.context." + upperCaseContextType + "Contextualizer";
			try {
				LOGGER.debug("Initializing class: " + className + "...");
				final Class<?> aClass = Class.forName(className);
				final java.lang.reflect.Constructor classConstructor = aClass.getConstructor();
				final AbstractContextualizer contextualizer = (AbstractContextualizer)classConstructor.newInstance();
				// set services
				contextualizer.setEntityIdentificationService(entityIdentificationService);
				contextualizer.setGenericSQLService(genericSQLDao);
				contextualizer.setSingleEntityDataService(singleEntityDataService);
				contextualizer.setRightsService(rightsService);
				contextualizer.setXmlConfigUtil(xmlConfigUtil);
				contextualizer.setSimpleSQLService(simpleSQLService);
				contextualizers.put(contextType, contextualizer);
				result = contextualizer;
			} catch (ClassNotFoundException e) {
				LOGGER.debug("FAILURE - using SemanticConnectionsContextualizer instead");
				SemanticConnectionsContextualizer contextualizer = new SemanticConnectionsContextualizer(contextType
						, genericSQLDao);
				contextualizers.put(contextType, contextualizer);
				result = contextualizer;
			} catch (SecurityException e) {
				LOGGER.error("Getting constructor failed for class " + className + ": ", e);
			} catch (NoSuchMethodException e) {
				LOGGER.error("Getting constructor failed for class " + className + ": ", e);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Creating instance of class " + className + "failed. Cause: ", e);
			} catch (InstantiationException e) {
				LOGGER.error("Creating instance of class " + className + "failed. Cause: ", e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Creating instance of class " + className + "failed. Cause: ", e);
			} catch (InvocationTargetException e) {
				LOGGER.error("Creating instance of class " + className + "failed. Cause: ", e.getCause());
			}
		}
		return result;		
	}

	public void clearCache() {
		contextualizers = new HashMap<String, IContextualizer>();
	} 
	
}