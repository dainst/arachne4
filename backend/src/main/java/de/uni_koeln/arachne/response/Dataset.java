package de.uni_koeln.arachne.response;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.uni_koeln.arachne.context.AbstractLink;
import de.uni_koeln.arachne.context.Context;
import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;
/**
 * This class provides a low level Interface to Arachne Datasets.
 * Its Maps the core Infos to the Fields and stores the other Data in the Sections Map
 * It Also contains a List for The Images and annother Map for the Contexts
 *
 */
@XmlRootElement
public class Dataset {

	// TODO change implementation to something more portable
	/**
	 * workaround for implementing getUri;
	 */
	private transient static final String BASEURI = "http://crazyhorse.archaeologie.uni-koeln.de/arachnedataservice/entity/";
	
	/**
	 * Identification of the Dataset.
	 */
	protected EntityId arachneId;
	
	/**
	 * The image to show as preview.
	 */
	protected Long thumbnailId;
	
	/**
	 * The Informations of the Dataset that is not in the core Dataset Definitions.
	 */
	protected Map<String,String> fields;
	
	/**
	 * The context map contains the contexts of the entity.
	 */
	protected List<Context> contexts;
	
	/**
	 * The Images that are asociated with the dataset.
	 */
	//protected List<ArachneImage> images;
	protected List<Image> images;
	
	/**
	 * Generic field for additional content
	 */
	protected AdditionalContent additionalContent; 
	
	/**
	 * The number of connections the entity represented by the dataset has
	 */
	protected double degree = 1;
	
	/**
	 * Parameterless constructor.
	 */
	public Dataset() {
		fields = new Hashtable<String,String>();
		contexts = new ArrayList<Context>();
	}
	
	/**
	 * changes the Prefix of the Internal key. It CHANGES ALL PREFIXES in the fields list
	 * @param newPrefix The PRefix that replaces the old Prefix
	 */
	public void renameFieldsPrefix(final String newPrefix){
		final Set<String> oldkeys = fields.keySet();
		final Map<String,String> newfields = new Hashtable<String,String>(fields.size());
		
		for (final String oldkey : oldkeys) {	
			final String newkey = newPrefix + oldkey.substring(oldkey.lastIndexOf('.'),oldkey.length());
			newfields.put(newkey, fields.get(oldkey));
		}
		
		fields = newfields;		
	}
	
	/**
	 * changes the Prefix of the Internal key.
	 * @param oldPrefix the old Prefix
	 * @param newPrefix The new prefix that replaces the old one
	 */
	public void renameFieldsPrefix(final String oldPrefix, final String newPrefix){
		
		final Set<String> oldkeys = fields.keySet();
		final Map<String,String> newfields = new Hashtable<String,String>(fields.size());
		
		for (final String oldkey: oldkeys) {
			if (!oldkey.startsWith(oldPrefix)) {
				continue;
			}
			final String newkey = newPrefix+oldkey.substring(oldkey.lastIndexOf('.'), oldkey.length());
			newfields.put(newkey, fields.get(oldkey));
		}
		
		fields = newfields;
		
		
	}
	/**
	 * Returns the unique Uri of the dataset.
	 * @return The unique Uri idenifying the dataset
	 */
	public String getUri() {
		if (arachneId.getArachneEntityID() == null) {
			return "Invalid Uri! Ask later!";
		} else {
			return BASEURI + arachneId.getArachneEntityID();
		}
	}
	
	//get methods
	public EntityId getArachneId() {
		return arachneId;
	}
	
	public List<Context> getContexts() {
		return contexts;
	}
	
	public Context getContext(final String type) {
		for (Context context : contexts) {
			if (context.getContextType().equals(type)) {
				return context;
			}
		}
		return null;
	}
	
	public List<Image> getImages() {
		return images;
	}
	
	public Long getThumbnailId() {
		return thumbnailId;
	}
	
	@XmlElement
	public Map<String, String> getFields() {
		return fields;
	}
	
	public AdditionalContent getAdditionalContent() {
		return additionalContent;
	}
	
	public double getDegree() {
		return degree;
	}
	
	/**
	 * This method returns the number of contexts of a given type. 
	 * <br>
	 * Side effect: If not all contexts are retrieved they will be retrieved now.
	 * @param contextType The type of the context of interest
	 * @return The number of context entities in this context
	 */
	public int getContextSize(final String contextType) {
		for (final Context context: this.contexts) {
			if (context.getContextType().equals(contextType)) {
				return context.getSize();				
			}
		}
		return 0;
	}
	
	/**
	 * Looks up a field in the </code>fields<code> list	or in the contexts and returns its value. The 
	 * </code>fields<code> list is the preferred search location and only if a field is not found there the contexts are 
	 * searched.
	 * <br>
	 * "Dataset" is a special contextualizer name that is used to reference data which is in every dataset (basically the <code>ArachneEntityId</code> object).
	 * This function returns these values, too, as it is faster than doing the look up again via the contextualizer mechanism.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the field or <code>null</code> if the field is not found.
	 */
	public String getField(final String fieldName) {
		String result = null;
		if (fieldName.startsWith("Dataset")) {
			// the magic number is the "dataset." char count
			final String unqualifiedFieldName = fieldName.substring(8);
			if ("Id".equals(unqualifiedFieldName)) {
				result = String.valueOf(arachneId.getArachneEntityID());
			} else {
				if ("internalId".equals(unqualifiedFieldName)) {
					result = String.valueOf(arachneId.getInternalKey());
				} else {
					if ("TableName".equals(unqualifiedFieldName)) {
						result = arachneId.getTableName();
					}
				}
			}
		} else {
			result = getFieldFromFields(fieldName);
			if (StrUtils.isEmptyOrNullOrZero(result)) {
				result = getFieldFromContext(fieldName);
			}
		}
		return result;
	}
	
	/**
	 * Looks up a field in the <code>fields</code> list and returns its value.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the field or <code>null</code> if the field is not found.
	 */
	public String getFieldFromFields(final String fieldName) {
		return fields.get(fieldName);
	}
	
	/**
	 * Looks up a field in the contexts and returns its value.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the field or <code>null<code/> if the field is not found.
	 */
	public String getFieldFromContext(final String fieldName) {
		String result = null;
		for (final Context context: this.contexts) {
			final AbstractLink link = context.getFirstContext();
			if (link != null) {
				// we know that Entity1 is 'this'
				result = link.getFieldFromFields(fieldName);
				if (!StrUtils.isEmptyOrNullOrZero(result)) {
					return result;
				}
			}
		}
		return null;
	}
	
	/**
	 * Looks up a field in the contexts and returns its value.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the field or <code>null</code> if the field is not found.
	 */
	public String getFieldFromContext(final String fieldName, final int index) {
		String result = null;
		for (final Context context: this.contexts) {
			if (fieldName.startsWith(context.getContextType() + '.')) {
				if (index < context.getSize()) {
					final AbstractLink link = context.getContext(index);
					if (link != null) {
						// we know that Entity1 is 'this'
						if (fieldName.endsWith(".contextUri")) {
							return link.getUri2();
						}
						result = link.getFieldFromFields(fieldName);
						if (!StrUtils.isEmptyOrNullOrZero(result)) {
							return result;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Looks up a field in all contexts and returns their values as list.
	 * Currently only internal links are supported.
	 * @param fieldName The full qualified fieldName to look up.
	 * @return The value of the fields or <code>null</code> if the field is not found.
	 */
	public List<String> getFieldsFromContexts(final String fieldName) {
		final List<String> result = new ArrayList<String>();
		for (final Context context: this.contexts) {
			final List<AbstractLink> links = context.getAllContexts();
			if (!links.isEmpty()) {
				for (final AbstractLink link: links) {
					String tmpResult = null;
					// we know that Entity1 is 'this'
					tmpResult = link.getFieldFromFields(fieldName);
					if (!StrUtils.isEmptyOrNullOrZero(tmpResult)) {
						result.add(tmpResult);
					}
				}
			}
		}
		if (result.isEmpty()) {
			return null;
		} else {
			return result;
		}
	}
	
	// set methods
	public void setContexts(final List<Context> contexts) {
		this.contexts = contexts;
	}
	
	public void setImages(final List<Image> images) {
		this.images = images;
	}
	
	public void setThumbnailId(final Long thumbnailId) {
		this.thumbnailId = thumbnailId;
	}
		
	public void setAdditionalContent(final AdditionalContent additionalContent) {
		this.additionalContent = additionalContent;
	}

	public void setDegree(final double degree) {
		this.degree = degree;
	}
	
	public void addContext(final Context context) {
		this.contexts.add(context);
	}

	
	/**
	 * This Function sets a Single Section in the Sections Map
	 * @param fieldsLabel The Label of the Section Information
	 * @param fieldsValues The Value that this Section has
	 * @return returns false if the section value is overwritten true if the Section is new to the Object
	 */
	
	public boolean setFields(final String fieldsLabel, final String fieldsValues) {
		if (this.fields.containsKey(fieldsLabel)) {
			this.fields.put(fieldsLabel, fieldsValues);
			return false;
		} else {
			this.fields.put(fieldsLabel, fieldsValues);
			return true;
		}
	}
	
	public void appendFields(final Map<String, String> sections) {
		this.fields.putAll(sections);
	}

	public void setArachneId(final EntityId arachneId) {
		this.arachneId = arachneId;
	}
	
	public void addImage(final Image image) {
		if (image == null) {
			return;
		}
		if (images == null) {
			images = new ArrayList<Image>();
		}
		images.add(image);
	}
	
	public void addImages(final List<Image> additionalImages) {
		if(this.images == null) {
			this.images = additionalImages;
		} else {
			this.images.addAll(additionalImages);
		}
	}
	
	@Override
	public String toString() {
		return fields + ", " + contexts;
	}
}
