package de.uni_koeln.arachne.response;

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
	 * The Informations of the Dataset that is not in the core Dataset Definitions
	 */
	protected Map<String,String> fields;
	
	/**
	 * The context map Contains the Contexts of the 
	 */
	protected Map<String,List<ArachneDataset>> context;
	
	/**
	 * The Images that are asociated with the dataset
	 */
	protected List<ArachneImage> images;
	

	
	public ArachneDataset() {
		fields = new HashMap<String,String>();
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
	
	public Map<String, String> getFields() {
		return fields;
	}

	
	
	//set methods
	public void setContext(Map<String, List<ArachneDataset>> context) {
		this.context = context;
	}
	public void setImages(List<ArachneImage> images) {
		this.images = images;
	}

	
	/**
	 * This Function sets a Single Section in the Sections Map
	 * @param fieldsLabel The Label of the Section Information
	 * @param fieldsValues The Value that this Section has
	 * @return returns false if the section value is overwritten true if the Section is new to the Object
	 */
	
	public boolean setFields(String fieldsLabel, String fieldsValues) {
		if (this.fields.containsKey(fieldsLabel)){
			this.fields.put(fieldsLabel, fieldsValues);
			return false;
		}
		else {
			this.fields.put(fieldsLabel, fieldsValues);
			return true;
		}
	}
	
	public void setFields(Map<String, String> sections) {
		this.fields = sections;
	}
	

	public void setArachneId(ArachneId arachneId) {
		this.arachneId = arachneId;
	}

	
	// is Methods
}
