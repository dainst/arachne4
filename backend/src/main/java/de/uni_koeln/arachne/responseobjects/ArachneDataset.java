package de.uni_koeln.arachne.responseobjects;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.arachne.util.ArachneId;
/**
 * This class provides a low level Interface to Arachne Datasets.
 * Its Maps the core Infos to the Fields and stores the other Data in the Sections Map
 * It Also contains a List for The Images and annother Map for the Contexts
 * @author Rasmus Krempel
 *
 */
public class ArachneDataset {
	
	/**
	 * Identification of the Dataset
	 */
	
	protected ArachneId arachneId;
	
	/**
	 * The Title of the Dataset
	 */
	protected String title;

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
	
	/**
	 * The Administration Information of the Dataset
	 */
	protected Map<String,String> adminstrationInformations;
	
	public ArachneDataset() {
		sections = new HashMap<String,String>();
		adminstrationInformations = new HashMap<String,String>();
	}	
	
	//get methods
	public ArachneId getArachneId() {
		return arachneId;
	}
	public Map<String, List<ArachneDataset>> getContext() {
		return context;
	}
	public List<ArachneImage> getImages() {
		return images;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public Map<String, String> getSections() {
		return sections;
	}

	public String getTitle() {
		return title;
	}
	public Map<String, String> getAdminstrationInformation() {
		return adminstrationInformations;
	}
	
	//set methods
	public void setContext(Map<String, List<ArachneDataset>> context) {
		this.context = context;
	}
	public void setImages(List<ArachneImage> images) {
		this.images = images;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	/**
	 * This Function sets a Single Section in the Sections Map
	 * @param sectionlabel The Label of the Section Information
	 * @param sectionvalue The Value that this Section has
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
	
	public void setTitle(String title) {
		this.title = title;
	}
	public void setArachneId(ArachneId arachneId) {
		this.arachneId = arachneId;
	}
	public void setAdminstrationInformations(
			Map<String, String> adminstrationInformation) {
		this.adminstrationInformations = adminstrationInformation;
	}
	public void setAdminstrationInformation(String adminstrationInformationLabel, String adminstrationInformationValue) {
		this.adminstrationInformations.put(adminstrationInformationLabel, adminstrationInformationValue);
	}
	// is Methods
	
	
	
}
