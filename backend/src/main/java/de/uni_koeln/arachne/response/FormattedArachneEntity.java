package de.uni_koeln.arachne.response;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response object class that returns preformatted output to the frontend.
 * This class is serialized to JSON using <codeJackson</code>.
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
	protected Content sections;
	
	/**
	 * The date of the last Modification of the Dataset
	 */
	protected Date lastModified;
	
	/**
	 * The context map Contains the Contexts of the 
	 */
	protected Map<String,List<ArachneDataset>> context;
	
	/**
	 * The Images that are asociated with the dataset
	 */
	protected List<ArachneImage> images;
	
	/**
	 * Parameterless constructor initializing sections, title and subtitle.
	 */
	public FormattedArachneEntity() {
		title = "";
		subtitle = "";
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
	
	public Content getSections() {
		return sections;
	}

	public void setSections(Content content) {
		sections = content;
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
}