package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.uni_koeln.arachne.context.ArachneContext;
import de.uni_koeln.arachne.context.ArachneLink;
import de.uni_koeln.arachne.util.ArachneId;
import de.uni_koeln.arachne.util.StrUtils;
/**
 * This class provides a low level Interface to Arachne Datasets.
 * Its Maps the core Infos to the Fields and stores the other Data in the Sections Map
 * It Also contains a List for The Images and annother Map for the Contexts
 *
 */
public class ArachneDataset {
	// TODO change implementation to something more portable
	/**
	 * workaround for implementing getUri;
	 */
	private final String baseUri = "http://localhost:8080/arachnedataservice/entity/";	
	
	/**
	 * Identification of the Dataset.
	 */
	protected ArachneId arachneId;
	
	
	/**
	 * The Informations of the Dataset that is not in the core Dataset Definitions.
	 */
	protected Map<String,String> fields;
	
	/**
	 * The context map contains the contexts of the entity.
	 */
	protected List<ArachneContext> context;
	
	/**
	 * The Images that are asociated with the dataset.
	 */
	//protected List<ArachneImage> images;
	protected List<String> images;
	
	/**
	 * Parameterless constructor.
	 */
	public ArachneDataset() {
		fields = new Hashtable<String,String>();
		context = new ArrayList<ArachneContext>();
	}	
	
	/**
	 * Returns the unique Uri of the dataset.
	 * @return The unique Uri idenifying the dataset
	 */
	public String getUri() {
		if (arachneId.getArachneEntityID() != null) {
			return baseUri + arachneId.getArachneEntityID();
		} else {
			return "Invalid Uri! Ask later!";
		}
	}
	
	//get methods
	public ArachneId getArachneId() {
		return arachneId;
	}
	
	public List<ArachneContext> getContext() {
		return context;
	}
	
	/*public List<ArachneImage> getImages() {
		return images;
	}*/
	public List<String> getImages() {
		return images;
	}
	
	/**
	 * Looks up a field in the </code>fields<code> list	or in the contexts and returns its value. The 
	 * </code>fields<code> list is the preferred search location and only if a field is not found there the contexts are 
	 * searched.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the field or <code>null<code/> if the field is not found.
	 */
	public String getField(String fieldName) {
		String result = getFieldFromFields(fieldName);
		if (StrUtils.isEmptyOrNull(result)) {
			result = getFieldFromContext(fieldName);
		}
		return result;
	}
	
	/**
	 * Looks up a field in the </code>fields<code> list and returns its value.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the field or <code>null<code/> if the field is not found.
	 */
	public String getFieldFromFields(String fieldName) {
		String result = fields.get(fieldName);
		return result;
	}
	
	/**
	 * Looks up a field in the in the contexts and returns its value.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the field or <code>null<code/> if the field is not found.
	 */
	public String getFieldFromContext(String fieldName) {
		for (ArachneContext context: this.context) {
			ArachneLink link = (ArachneLink)context.getFirstContext();
			if (link != null) {
				// we know that Entity1 is 'this'
				return link.getEntity2().getFieldFromFields(fieldName);
			}
		}
		return null;
	}
	
	//set methods
	public void setContext(List<ArachneContext> context) {
		this.context = context;
	}
	
	/*public void setImages(List<ArachneImage> images) {
		this.images = images;
	}*/
	public void setImages(List<String> images) {
		this.images = images;
	}
		
	public void addContext(ArachneContext aContext) {
		this.context.add(aContext);
	}

	
	/**
	 * This Function sets a Single Section in the Sections Map
	 * @param fieldsLabel The Label of the Section Information
	 * @param fieldsValues The Value that this Section has
	 * @return returns false if the section value is overwritten true if the Section is new to the Object
	 */
	
	public boolean setFields(String fieldsLabel, String fieldsValues) {
		if (this.fields.containsKey(fieldsLabel)) {
			this.fields.put(fieldsLabel, fieldsValues);
			return false;
		}
		else {
			this.fields.put(fieldsLabel, fieldsValues);
			return true;
		}
	}
	
	public void appendFields(Map<String, String> sections) {
		this.fields.putAll(sections);
	}

	public void setArachneId(ArachneId arachneId) {
		this.arachneId = arachneId;
	}
	
	
	// is Methods
}
