package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.uni_koeln.arachne.context.Context;
import de.uni_koeln.arachne.context.ArachneLink;
import de.uni_koeln.arachne.context.Link;
import de.uni_koeln.arachne.util.ArachneId;
import de.uni_koeln.arachne.util.StrUtils;
/**
 * This class provides a low level Interface to Arachne Datasets.
 * Its Maps the core Infos to the Fields and stores the other Data in the Sections Map
 * It Also contains a List for The Images and annother Map for the Contexts
 *
 */
public class Dataset {
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
	protected List<Context> context;
	
	/**
	 * The Images that are asociated with the dataset.
	 */
	//protected List<ArachneImage> images;
	protected List<Image> images;
	
	/**
	 * Parameterless constructor.
	 */
	public Dataset() {
		fields = new Hashtable<String,String>();
		context = new ArrayList<Context>();
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
	
	public List<Context> getContext() {
		return context;
	}
	
	/*public List<ArachneImage> getImages() {
		return images;
	}*/
	public List<Image> getImages() {
		return images;
	}
	
	/**
	 * This method returns the number of contexts of a given type. 
	 * <br>
	 * Side effect: If not all contexts a retrieved they will be retrieved now.
	 * @param contextType The type of the context of interest
	 * @return The number of context entities in this context
	 */
	public int getContextSize(String contextType) {
		for (Context context: this.context) {
			if (context.getContextType().equals(contextType)) {
				return context.getContextSize();				
			}
		}
		return 0;
	}
	
	/**
	 * Looks up a field in the </code>fields<code> list	or in the contexts and returns its value. The 
	 * </code>fields<code> list is the preferred search location and only if a field is not found there the contexts are 
	 * searched.
	 * <br>
	 * "dataset" is a special contextualizer name that is used to reference data which is in every dataset like the internalId.
	 * This functions returns such values, too.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the field or <code>null<code/> if the field is not found.
	 */
	public String getField(String fieldName) {
		String result = null;
		if (fieldName.startsWith("dataset")) {
			// the magic number is the dataset char count
			String unqualifiedFieldName = fieldName.substring(8);
			if (unqualifiedFieldName.equals("TableName")) {
				result = arachneId.getTableName();
			}
		} else {
			result = getFieldFromFields(fieldName);
			if (StrUtils.isEmptyOrNull(result)) {
				result = getFieldFromContext(fieldName);
			}
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
	 * Looks up a field in the contexts and returns its value.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the field or <code>null<code/> if the field is not found.
	 */
	public String getFieldFromContext(String fieldName) {
		String result = null;
		for (Context context: this.context) {
			ArachneLink link = (ArachneLink)context.getFirstContext();
			if (link != null) {
				// we know that Entity1 is 'this'
				result = link.getEntity2().getFieldFromFields(fieldName);
				if (!StrUtils.isEmptyOrNull(result)) {
					return result;
				}
			}
		}
		return null;
	}
	
	/**
	 * Looks up a field in the contexts and returns its value.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the field or <code>null<code/> if the field is not found.
	 */
	public String getFieldFromContext(String fieldName, int index) {
		String result = null;
		for (Context context: this.context) {
			ArachneLink link = (ArachneLink)context.getContext(index);
			if (link != null) {
				// we know that Entity1 is 'this'
				result = link.getEntity2().getFieldFromFields(fieldName);
				if (!StrUtils.isEmptyOrNull(result)) {
					return result;
				}
			}
		}
		return null;
	}
	
	/**
	 * Looks up a field in all contexts and returns their values as list.
	 * Currently only internal links are supported.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the fields or <code>null<code/> if the field is not found.
	 */
	public List<String> getFieldsFromContexts(String fieldName) {
		List<String> result = new ArrayList<String>();
		for (Context context: this.context) {
			List<Link> links = context.getallContexts();
			if (!links.isEmpty()) {
				for (Link link: links) {
					String tmpResult = null;
					// TODO add support for external links
					// we know that Entity1 is 'this'
					if (link instanceof ArachneLink) {
						ArachneLink internalLink = (ArachneLink)link;
						tmpResult = internalLink.getEntity2().getFieldFromFields(fieldName);
					}
					if (!StrUtils.isEmptyOrNull(tmpResult)) {
						result.add(tmpResult);
					}
				}
			}
		}
		if (result.size() > 0) {
			return result;
		} else {
			return null;
		}
	}
	
	// set methods
	public void setContext(List<Context> context) {
		this.context = context;
	}
	
	/*public void setImages(List<ArachneImage> images) {
		this.images = images;
	}*/
	public void setImages(List<Image> images) {
		this.images = images;
	}
		
	public void addContext(Context aContext) {
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
	
	@Override
	public String toString() {
		return fields + ", " + context;
	}
}
