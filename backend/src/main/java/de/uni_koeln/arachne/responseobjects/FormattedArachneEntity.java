package de.uni_koeln.arachne.responseobjects;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Response object class that returns preformatted JSON for the frontend.
 * @author Rasmus all alone
 *
 */
public class FormattedArachneEntity extends BaseArachneEntity {
	
	/**
	 * The Title of the Dataset
	 */
	protected String title;
	
	/**
	 * The Subtitle of the Dataset
	 */
	protected String subtitle;
	
	/**
	 * Hierachical structured information of the dataset.
	 */
	protected Content content;
	
	/**
	 * The date of the last Modification of the Dataset
	 */
	protected Date lastModified;
	
	/**
	 * The Informations of the Dataset that is not in the core Dataset Definitions
	 */
	protected Map<String,String> sections;
	
	/**
	 * The context map Contains the Contexts of the 
	 */
	protected Map<String,List<ArachneDataset>> context;
	
	/**
	 * The Images that are asociated with the dataset
	 */
	protected List<ArachneImage> images;
	
	public FormattedArachneEntity() {
		sections = new HashMap<String,String>();
		title = "";
		subtitle = "";
	}	
	
	/**
	 * This Function sets a Single Section in the Sections Map
	 * @param sectionLabel The Label of the Section Information
	 * @param sectionValue The Value that this Section has
	 * @return returns false if the section value is overwrittten true if the Section is new to the Object
	 */
	
	public boolean setSection(String sectionLabel, String sectionValue) {
		if (this.sections.containsKey(sectionLabel)){
			this.sections.put(sectionLabel, sectionValue);
			return false;
		}
		else {
			this.sections.put(sectionLabel, sectionValue);
			return true;
		}
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	
	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}
	
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Map<String, List<ArachneDataset>> getContext() {
		return context;
	}

	public void setContext(Map<String, List<ArachneDataset>> context) {
		this.context = context;
	}

	public List<ArachneImage> getImages() {
		return images;
	}

	public void setImages(List<ArachneImage> images) {
		this.images = images;
	}

	public Map<String, String> getSections() {
		return sections;
	}

	public void setSections(Map<String, String> sections) {
		this.sections = sections;
	}
	
	// is Methods
}