package de.uni_koeln.arachne.context;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.AdditionalContent;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.SarcophagusImage;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.image.ImageUtils;

/**
 * Contextualizer class to retrieve image data not directly connected to sarcophagi but to objects who in turn
 * are connected to the sarcophagus that triggered contextualization.
 */
public class SarcophagusimagesContextualizer extends AbstractContextualizer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SarcophagusimagesContextualizer.class);
	
	private transient final static String CONTEXT_TYPE = "Sarcophagusimages";
	
	/**
	 * The context category we are searching for.
	 */
	private transient final static String TARGET_CONTEXT_TYPE = "marbilder";
	
	/**
	 * All object categories whose assigned images are relevant for sarcophagi.
	 */
	private transient final static String[] PRIMARY_CONTEXT_TYPES = { "objekt", "gruppen", "relief", "realien" };
	
	/**
	 * The <code>SarcophagusImage</code> entity being processed at the moment.
	 */
	private transient SarcophagusImage image;
	
	/**
	 * All Images belonging to the sarcophagus.
	 */
	private transient final List<SarcophagusImage> images;
	
	public SarcophagusimagesContextualizer() {
		images = new ArrayList<SarcophagusImage>();
	}
	
	/**
	 * Retrieve image data and store it in <code>Dataset</code> parent by iterating over connected entities.
	 * @param parent	the dataset of the sarcophagus, that the images will be added to.
	 * @return	always null, because instead of building actual contexts we only want to add a custom field to 
	 * <code>Dataset</code> parent.
	 */
	public List<AbstractLink> retrieve(final Dataset parent) {
		
		for (final String contextType : PRIMARY_CONTEXT_TYPES) {
			
			final List<Map<String, String>> entitiesContextContents = genericSQLDao.getConnectedEntities(contextType, parent.getArachneId().getArachneEntityID());
			if (entitiesContextContents != null) {
				for (final Map<String, String> entityData : entitiesContextContents) {
					final String internalId = entityData.get(contextType + ".PS_" + contextType.substring(0,1).toUpperCase() + contextType.substring(1).toLowerCase() + "ID"); // e.g. 'relief.PS_ReliefID'
					final EntityId entityId = entityIdentificationService.getId(contextType, Long.parseLong(internalId));
					//get information that might be relevant for the image
					Integer sceneNumber = null;
					String description = null;
					if ("relief".equals(contextType)) {
						sceneNumber = Integer.parseInt(entityData.get("relief.Szenennummer"));
						description = entityData.get("relief.KurzbeschreibungRelief");
					} else if ("realien".equals(contextType)) {
						description = entityData.get("realien.KurzbeschreibungRealien");
					}

					final List<Map<String, String>> imagesContextContents = genericSQLDao.getConnectedEntities(TARGET_CONTEXT_TYPE, entityId.getArachneEntityID());
					if (imagesContextContents != null) {
						addImages(contextType, imagesContextContents, sceneNumber, description);	
					}
				}
			}
		}
		
		if (images != null && !images.isEmpty()) {
			parent.setThumbnailId(ImageUtils.findThumbnailId(images));
			if (parent.getAdditionalContent() == null) {
				final AdditionalContent additionalContent = new AdditionalContent();
				additionalContent.setSarcophagusImages(images);
				parent.setAdditionalContent(additionalContent);
			} else {
				parent.getAdditionalContent().setSarcophagusImages(images);
			}
		}
		
		LOGGER.debug(parent.toString());
		
		return null;
	}
	
	/**
	 * Method to add images to the <code>images</code> set. 
	 * @param contextType A String naming the context.
	 * @param imagesContextContents List of connected image dataset maps.
	 * @param sceneNumber Internal number of a relief scene. Is null if the context is not of the type "relief".
	 * @param offset of the context to retrieve.
	 * @param limit Maximum number of contexts to retireve.
	 */
	private void addImages(final String contextType, final List<Map<String, String>> imagesContextContents, 
						   final Integer sceneNumber, final String description) {
		final ListIterator<Map<String, String>> imagesContextMap = imagesContextContents.listIterator();
		while (imagesContextMap.hasNext()) {
			image = new SarcophagusImage();
			extractImageDataFromQueryResults(imagesContextMap.next());
						
			if (("relief".equals(contextType)) && (sceneNumber != null)) {
				image.setSceneNumber(sceneNumber);
				image.setDescription(description);
			} else if ("realien".equals(contextType)) {
				image.setDescription(description);
			}
			if (image.getImageId() != null) {
				images.add(image);
			}
		}
	}
	
	/**
	 * Get all relevant data belonging to an image, store it in member variable <code>image</code> and add that to <code>images</code>.
	 * @param queryResults	the image data retrieved 
	 */
	private void extractImageDataFromQueryResults (final Map<String, String> queryResults) {
		final String sourceType = queryResults.get("SemanticConnection.TypeSource");
		final String targetType = queryResults.get("SemanticConnection.TypeTarget");
		final String project;
		final long imageId;
		final String filename;
		
		if ("marbilder".equals(targetType) && sourceType!=null) {
			project =  sourceType;
			imageId = Long.parseLong(queryResults.get("SemanticConnection.Target")); 
		} else if ("marbilder".equals(sourceType) && targetType!=null) {
			project = targetType;
			imageId = Long.parseLong(queryResults.get("SemanticConnection.Source")); 
		} else {
			return;
		}
		
		filename = queryResults.get("marbilder.DateinameMarbilder");

		if (filename!=null && project!=null) {
			image.setImageFields(imageId, filename, project);
		}
	}
	
	public String getContextType() {
		return CONTEXT_TYPE;
	}
	
}
