package de.uni_koeln.arachne.response;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SarcophagusImage {

	private String entityId;
	private String filename;
	private String project;
	private String scene = "-1";
	
	public String getEntityId() {
		return entityId;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getProject() {
		return project;
	}
	
	public String getScene() {
		return scene;
	}
	
	public void setImageFields(final String entityId, final String filename, final String project) {
		this.entityId = entityId;
		this.filename = filename;
		this.project = project;
	}
	
	public void setEntityId(final String entityId) {
		this.entityId = entityId;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public void setProject(final String project) {
		this.project = project;
	}
	
	public void setScene(final String scene) {
		this.scene = scene;
	}

}
