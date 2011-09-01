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
public class FormattedArachneEntity {
	/**
	 * Identification of the Dataset
	 */
	protected Long id;
	
	/**
	 * The Title of the Dataset
	 */
	protected String title;

	/**
	 * The tablename field of the ArachneEntity table
	 */
	protected String category;
	
	/**
	 * The foreignKey field of the ArachneEntity table
	 */
	protected Long categoryId;
	
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
	}	
	
	
	
	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String getCategory() {
		return category;
	}



	public void setCategory(String category) {
		this.category = category;
	}



	public Long getCategoryId() {
		return categoryId;
	}



	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
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
	
	public void setSections(Map<String, String> sections) {
		this.sections = sections;
	}
	
	// is Methods
}