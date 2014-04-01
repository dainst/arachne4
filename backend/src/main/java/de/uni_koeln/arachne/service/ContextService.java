package de.uni_koeln.arachne.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.context.*;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Image;
import de.uni_koeln.arachne.util.ImageUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * This class handles creation and retrieval of contexts and adds them to datasets.
 * Internally it uses <code>Contextualizers</code> to abstract the data access and allow to fetch contexts not only from
 * the Arachne database but from any other datasource (even external ones).  
 */
@Service("arachneContextService")
public class ContextService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextService.class);
	
	@Autowired
	private transient EntityIdentificationService entityIdentificationService; 
	
	/**
	 * Service to access the 'Verknuepfungen' table. The information stored in that table is used
	 * to determine which contexts the <code>addContext</code> method adds to a given dataset.
	 */	
	@Autowired
	private transient ConnectionService arachneConnectionService; 
	
	/**
	 * Service to access ids in 'cross tables'.
	 */
	@Autowired
	private transient GenericSQLService genericSQLService; 
	
	@Autowired
	private transient SingleEntityDataService singleEntityDataService;
	
	/**
	 * Utility class to work with the XML config files.
	 */
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	/**
	 * Service to access the current user.
	 */
	@Autowired
	private transient IUserRightsService rightsService;
	
	private transient Map<String, IContextualizer> contextualizers = new HashMap<String, IContextualizer>();
	
	/**
	 * This methods adds all contexts to the dataset that are found in the XML description. It also runs all contextualizers 
	 * that are marked as explicit in the corresponding xml config file. 
	 * @param parent The dataset to add the contexts to.
	 */
	public void addMandatoryContexts(final Dataset parent) {
		// explicit contextualizers
		final List<String> explicitContextualizersList = xmlConfigUtil.getExplicitContextualizers(parent.getArachneId().getTableName());
		for (String contextualizerName: explicitContextualizersList) {
			final IContextualizer contextualizer = getContextualizerByContextType(contextualizerName);
			final Context context = new Context(contextualizer.getContextType(), parent, contextualizer.retrieve(parent));
			if (context.getSize() > 0) {
				parent.addContext(context);
			}
		}
		// implicit contextualizers
		final List<String> mandatoryContextTypes = xmlConfigUtil.getMandatoryContextNames(parent.getArachneId().getTableName());
		LOGGER.debug("Mandatory Contexts: " + mandatoryContextTypes);
		for (final String contextType: mandatoryContextTypes) {
			final Context context = new Context(contextType, parent, getLinks(parent, contextType));
			if (context.getSize() > 0) {
				parent.addContext(context);
			}
		}
	}
	
	
	/**
	 * This methods adds all contexts to the dataset that are found in the XML description.
	 * @param parent The dataset to add the contexts to.
	 * @param imageService Instance of ImageService used for image-retrieval
	 */
	public void addContextImages(final Dataset parent, final ImageService imageService) {
		
		// get Context-Images from Context-XML
		final List<ContextImageDescriptor> contextImages = xmlConfigUtil.getContextImagesNames(parent.getArachneId().getTableName());
			
		if (contextImages == null) {
			LOGGER.debug("No Context-Image-Declarations found.");
			return;
		}
		
		final List<Image> resultContextImages = new ArrayList<Image>();
		
		// check if the source-record contains any images
		boolean containsImages = false;
		if(parent.getImages() != null && !parent.getImages().isEmpty()) {
			containsImages = true;
		}
		
		for (final ContextImageDescriptor cur : contextImages) {
			
			// check contextImage-Preconditions from config
			if (cur.getContextImageUsage().equals("ifempty") && containsImages) {
				continue;
			}
			
			// create image-context
			final Context context = new ContextImage(cur.getContextName(), cur.getContextImageUsage(), parent);
			
			// retrieve full context-data
			final List<AbstractLink> connectedEntities = context.getAllContexts();
			if (connectedEntities == null) {
				continue;
			}
			
			// Retrieve images from context-entities using ImageService
			for (final AbstractLink link : connectedEntities) {
				if (link instanceof ArachneLink) {
					final ArachneLink arachneLink = (ArachneLink) link;
					
					// Fetch conntected entity
					final Dataset contextImageDataset = arachneLink.getEntity2();
					
					// Fetch images for connected Entity
					imageService.addImages(contextImageDataset);
					
					final List<Image> contextImagesList = contextImageDataset.getImages();
					if(contextImagesList == null || contextImagesList.isEmpty()) {
						continue;
					}
					
					// Use table-name for context-description
					final String contextTableName = contextImageDataset.getArachneId().getTableName().substring(0,1).toUpperCase() + contextImageDataset.getArachneId().getTableName().substring(1);
					
					// Iterate over all loaded context-images and add images to parent-dataset
					final Iterator<Image> contextImageIterator = contextImagesList.iterator();
					
					while(contextImageIterator.hasNext()) {
						
						final Image curImage = contextImageIterator.next();
						
						// Entity-ID of connected Record
						curImage.setSourceRecordId(contextImageDataset.getArachneId().getArachneEntityID());
						
						// Context-Type of connected Record
						curImage.setSourceContext(contextTableName);
						
						// add image to result-list and remove from context to save ressources
						resultContextImages.add(curImage);
						contextImageIterator.remove();
					}
				}
			}	
		}
		LOGGER.debug("Adding " + resultContextImages.size() + " additional images from dataset-contexts...");
		parent.addImages(resultContextImages);
		
		// if no thumbnail has been set yet, use one from context
		if(!resultContextImages.isEmpty() && parent.getThumbnailId() == null) {
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
	 * is returned.
	 * @param contextType Type of a context of interest  
	 * @return an appropriate contextualizer serving the specific context indicated by the given <code>contextType</code>
	 */
	@SuppressWarnings("rawtypes")
	private IContextualizer getContextualizerByContextType(final String contextType) {
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
				contextualizer.setGenericSQLService(genericSQLService);
				contextualizer.setSingleEntityDataService(singleEntityDataService);
				contextualizer.setRightsService(rightsService);
				contextualizer.setXmlConfigUtil(xmlConfigUtil);
				contextualizers.put(contextType, contextualizer);
				result = contextualizer;
			} catch (ClassNotFoundException e) {
				LOGGER.debug("FAILURE - using SemanticConnectionsContextualizer instead");
				result = new SemanticConnectionsContextualizer(contextType, genericSQLService);
			} catch (SecurityException e) {
				LOGGER.debug("Getting constructor failed for class " + className + ": ", e);
			} catch (NoSuchMethodException e) {
				LOGGER.debug("Getting constructor failed for class " + className + ": ", e);
			} catch (IllegalArgumentException e) {
				LOGGER.debug("Creating instance of class " + className + "failed. Cause: ", e);
			} catch (InstantiationException e) {
				LOGGER.debug("Creating instance of class " + className + "failed. Cause: ", e);
			} catch (IllegalAccessException e) {
				LOGGER.debug("Creating instance of class " + className + "failed. Cause: ", e);
			} catch (InvocationTargetException e) {
				LOGGER.debug("Creating instance of class " + className + "failed. Cause: ", e);
			}
		}
		return result;		
	}


	public void clearCache() {
		contextualizers = new HashMap<String, IContextualizer>();
	} 
	
}