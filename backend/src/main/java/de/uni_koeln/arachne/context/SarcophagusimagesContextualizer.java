package de.uni_koeln.arachne.context;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.EntityId;

public class SarcophagusimagesContextualizer extends AbstractContextualizer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SarcophagusimagesContextualizer.class);
	
	private transient final static String CONTEXT_TYPE = "Sarcophagusimages";
	
	/**
	 * The context category we are searching for
	 */
	private transient final static String TARGET_CONTEXT_TYPE = "marbilder";
	
	/**
	 * All object categories whose assigned images are relevant for sarcophagi
	 */
	private transient final static String[] PRIMARY_CONTEXT_TYPES = { "objekt", "gruppen", "relief", "realien" };
	
	/**
	 * The <code>SarcophagusImage</code> entity being processed at the moment.
	 */
	private transient SarcophagusImage image;
	
	/**
	 * All Images belonging to the sarcophagus that triggered contextualization
	 */
	private transient Set<SarcophagusImage> images;
	
	private transient long imageCount = 0L;
	
	public SarcophagusimagesContextualizer() {
		images = new HashSet<SarcophagusImage>();
	}
	
	/**
	 * Nested class to store ImageData
	 */
	private static class SarcophagusImage {
		private String id;
		private String filename;
		private String project;
		private String scene = "-1";
		private void setImageFields(String id, String filename, String project) {
			this.id = id;
			this.filename = filename;
			this.project = project;
		}
		private void setScene(String scene) {this.scene = scene; }
		public String toString() {
			String result = "{";
			result += "id: " + id + ", ";
			result += "filename: " + filename + ", ";
			result += "project: " + project + ", ";
			if (!scene.equals("-1"))
				result += "scene: " + scene + ", ";
			result = result.substring(0, result.length()-2) + "}";
			return result;
		}
	}
	
	/**
	 * Retrieve image data and store it in <code>Dataset</code> parent by iterating over connected entities and 
	 * @return	always null, because instead of actual contexts we only want to add a custom field to <code>Dataset</code> parent 
	 */
	public List<AbstractLink> retrieve(final Dataset parent, final Integer offset, final Integer limit) {
		
		for (String contextType : PRIMARY_CONTEXT_TYPES) {
			
			final List<Map<String, String>> entitiesContextContents = genericSQLService.getConnectedEntities(contextType, parent.getArachneId().getArachneEntityID());
			if (entitiesContextContents != null) {
				for (Map<String, String> entityData : entitiesContextContents) {
					final String internalId = entityData.get(contextType + ".PS_" + contextType.substring(0,1).toUpperCase() + contextType.substring(1).toLowerCase() + "ID"); // e.g. 'relief.PS_ReliefID'
					final EntityId entityId = entityIdentificationService.getId(contextType, Long.parseLong(internalId));
					String sceneNr = null;
					if (contextType == "relief")
						sceneNr = entityData.get("relief.Szenennummer");
					
					final List<Map<String, String>> imagesContextContents = genericSQLService.getConnectedEntities(TARGET_CONTEXT_TYPE, entityId.getArachneEntityID());
					if (imagesContextContents != null) {
						final ListIterator<Map<String, String>> imagesContextMap = imagesContextContents.listIterator(offset);
						while (imagesContextMap.hasNext() && (imageCount < limit || limit == -1)) {
							image = new SarcophagusImage();
							extractImageDataFromQueryResults(imagesContextMap.next());
							imageCount++;
							
							if ((contextType == "relief") && (sceneNr != null))
								image.setScene(sceneNr);
							
							if (image.id != null)
								images.add(image);
						}	
					}
				}
			}
		}

		if (!images.isEmpty()) {
			parent.setFields(CONTEXT_TYPE + "." + "images", images.toString());
		}
		return null;
	}
	
	/**
	 * Get all relevant data belonging to an image, store it in member variable <code>image</code> and add that to <code>images</code>.
	 * @param queryResults	the image data retrieved 
	 */
	private void extractImageDataFromQueryResults (final Map<String, String> queryResults) {
		final String sourceType = queryResults.get("semanticconnection.TypeSource");
		final String targetType = queryResults.get("semanticconnection.TypeTarget");
		final String project, id, filename;
		
		if (targetType.equals("marbilder") && sourceType!=null) {
			project =  sourceType;
			id = queryResults.get("semanticconnection.Source"); 
		} else if (sourceType.equals("marbilder") && targetType!=null) {
			project = targetType;
			id = queryResults.get("semanticconnection.Target"); 
		} else {
			return;
		}
		
		filename = queryResults.get("marbilder.DateinameMarbilder");
		image.setImageFields(id, filename, project);
	}
	
	public String getContextType() {
		return CONTEXT_TYPE;
	}
	
}
