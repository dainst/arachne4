package de.uni_koeln.arachne.response;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used by the <code>SarcophagusimagesContextualizer</code> to store image data 
 * belonging to sarcophagus entities.
 */
@XmlRootElement
public class SarcophagusImage extends Image {
	
	/**
	 * the category of the object the image belongs to. Possible Categories are specified in
	 * <code>SarcophagusimagesContextualizer.PRIMARY_CONTEXT_TYPES</code>
	 */
	private String project = null;
	
	/**
	 * If the image is one connected to an entity of category "relief" this stores the scene number
	 * of that relief.
	 */
	private Integer sceneNumber = null;
	
	/**
	 * If the image is one connected to an entity of category "relief" or "realien" this stores 
	 * a short description of that object.
	 */
	private String description;
	
	public String getProject() {
		return project;
	}
	
	public Integer getSceneNumber() {
		return sceneNumber;
	}
	
	public String getDescription() {
		return description;
	}

	public void setProject(final String project) {
		this.project = project;
	}
	
	public void setSceneNumber(final Integer scene) {
		this.sceneNumber = scene;
	}
	
	public void setDescription(final String description) {
		this.description = description;
	}

	public void setImageFields(final Long imageId, final String subtitle, final String project) {
		this.imageId = imageId;
		this.subtitle = subtitle;
		this.project = project;
	}
}
