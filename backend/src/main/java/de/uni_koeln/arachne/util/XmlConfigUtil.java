package de.uni_koeln.arachne.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextResource;

import de.uni_koeln.arachne.response.AbstractContent;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Field;
import de.uni_koeln.arachne.response.FieldList;
import de.uni_koeln.arachne.response.Section;

/**
 * This class provides functions to find a XML config file by type, extract information based on the XML element from
 * the dataset and grants access to the servlet context.
 * If some class wants to work with the XML config files it should use this class via autowiring or as base class.
 */
@Component("xmlConfigUtil")
public class XmlConfigUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlConfigUtil.class);
	
	/**
	 * Servlet context to load the XML config files. 
	 */
	@Autowired
	private transient ServletContext servletContext;
	
	/**
	 * This function checks if a config file for the given type exists and returns its filename.
	 * @param type Type of the config to look for.
	 * @return The filename of the XML config file for the given type or <code>null</code>.
	 */
	public String getFilenameFromType(final String type) {
		String filename = "/WEB-INF/xml/"+ type + ".xml";
		final ServletContextResource file = new ServletContextResource(servletContext, filename);
		if (!file.exists()) {
			filename = "unknown";
		}
		
		LOGGER.debug("config file: " + filename);
		return filename;
	}
	
	/**
	 * This function handles sections in the xml config files. It extracts the content from the dataset following the definitions in the xml files
	 * and returns it as a <code>String</code>.
	 * <br>
	 * The validity of the xml file is not checked!!!
	 * @param section The xml section Element to parse.
	 * @param dataset The dataset that contains the SQL query results.
	 * @return A concatenated string containing the sections content.
	 */
	public String getStringFromSections(final Dataset dataset, final Namespace nameSpace, final Element section) {
		final StringBuffer result = new StringBuffer("");
		final List<Element> children = section.getChildren();
		String separator = "<br/>";
		if (section.getAttributeValue("separator") != null) {
			separator = section.getAttributeValue("separator");
		}

		for (Element e:children) {
			if (e.getName().equals("field")) {
				getFieldString(dataset, nameSpace, result, separator, e); 
			} else {
				final String datasetResult = getStringFromSections(dataset, nameSpace, e);
				if (!(result.length() < 1) && !datasetResult.isEmpty()) {
					result.append(separator);
				}
				result.append(datasetResult);
			}
		}
		return result.toString();
	}

	private void getFieldString(final Dataset dataset, final Namespace nameSpace, final StringBuffer result, final String separator
			, final Element element) {
		
		final String initialValue = dataset.getField(element.getAttributeValue("datasource"));
		StringBuffer datasetResult = null;
		if (initialValue != null) {
			datasetResult = new StringBuffer(initialValue);
		} 

		final String postfix = element.getAttributeValue("postfix");
		final String prefix = element.getAttributeValue("prefix");

		if (datasetResult == null) {
			datasetResult = getIfEmpty(dataset, nameSpace, element);
		}

		if (!StrUtils.isEmptyOrNull(datasetResult)) {
			if (prefix != null) {
				datasetResult.insert(0, prefix);
			}
			if (postfix != null) { 
				datasetResult.append(postfix);
			}
			if (!(result.length() < 1) && !(datasetResult.length() < 1)) {
				result.append(separator);
			}
			result.append(datasetResult);
		}
	}

	private StringBuffer getIfEmpty(final Dataset dataset, final Namespace nameSpace, final Element element) {
		
		String key;
		StringBuffer result = null;
		final Element ifEmptyElement = element.getChild("ifEmpty", nameSpace);
		if (ifEmptyElement != null) {
			// TODO discuss if multiple fields inside an ifEmpty tag make sense
			key = ifEmptyElement.getChild("field", nameSpace).getAttributeValue("datasource");
			if (key != null && !key.isEmpty()) {
				final String ifEmptyValue = dataset.getField(key);
				if (ifEmptyValue != null) {
					result = new StringBuffer(ifEmptyValue); 
				}
			}
		}
		return result;
	}
	
	/**
	 * This function handles sections in the xml config files. It extracts the content from the dataset following the 
	 * definitions in the xml files and returns it as <code>Content</code>.
	 * <br>
	 * The validity of the xml file is not checked!!!
	 * @param section The xml section <code>Element</code> to parse.
	 * @param dataset The dataset that contains the SQL query results.
	 * @return A <code>Content</code> object containing the sections content.
	 */
	public AbstractContent getContentFromSections(final Element section, final Dataset dataset, final int groupId) {
		
		if (!hasMinGroupId(section.getAttributeValue("minGroupId"), groupId)) {
			return null;
		}
		
		final Section result = new Section();
		//TODO Get translated label string for value of labelKey-attribute in the section element  
		result.setLabel(section.getAttributeValue("labelKey"));
		
		final List<Element> children = section.getChildren();
		
		final String defaultSeparator = "<br/>";
		String separator = section.getAttributeValue("separator"); 
		if (section.getAttributeValue("separator") == null) {
			separator = defaultSeparator;
		}
								
		for (Element e:children) {
			if (e.getName().equals("field")) {
				addFieldToResult(e, result, dataset, separator, groupId);
			} else {
				if (e.getName().equals("context")) {
					final Section nextSection = (Section)getContentFromContext(e, dataset, groupId);
					if (nextSection != null && !((Section)nextSection).getContent().isEmpty()) { 
						result.add(nextSection);
					}
				} else {
					final Section nextSection = (Section)getContentFromSections(e, dataset, groupId);
					if (nextSection != null && !((Section)nextSection).getContent().isEmpty()) { 
						result.add(nextSection);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Procedure to add a <code>Field</code> from the dataset to the result <code>Section</code>.
	 * @param dataset The current dataset.
	 * @param result The <code>Section</code> the field belongs to.
	 * @param separator The currently active separator.
	 * @param element The description of the field as XML element.
	 * @param groupId The current users groupId.
	 */
	private void addFieldToResult(final Element element, final Section result, final Dataset dataset
			, final String separator, final int groupId) {
		
		if (!hasMinGroupId(element.getAttributeValue("minGroupId"), groupId)) {
			return;
		}
		
		final Field field = new Field();
		StringBuffer value = null;
		final String initialValue = dataset.getField(element.getAttributeValue("datasource"));
		if (initialValue != null) {
			value = new StringBuffer(initialValue);
		}
		final String postfix = element.getAttributeValue("postfix");
		final String prefix = element.getAttributeValue("prefix");
		if (value != null) {
			if (prefix != null) {
				value.insert(0, prefix);
			}
			if (postfix != null) {
				value.append(postfix); 
			}

			// TODO find better solution as the previous content may be a section
			// If there are more than one field in this section add the value (incl. separator) to the previous field
			if (result.getContent().isEmpty()) {
				field.setValue(value.toString());
				result.add(field);
			} else {
				final int contentSize = result.getContent().size();
				final Field previousContent = (Field)result.getContent().get(contentSize-1);
				previousContent.setValue(previousContent.getValue() + separator + value);
			}
		}
	}
	
	/**
	 * This function handles context elements in the xml config files. It extracts the content from the dataset 
	 * following the definitions in the xml files and returns it as <code>Content</code>.
	 * <br>
	 * The validity of the xml file is not checked!!!
	 * @param context The xml context <code>Element</code> to parse.
	 * @param dataset The dataset that contains the SQL query results.
	 * @param grouId The groupId of the current user.
	 * @return A <code>Section</code> object containing the context sections content or <code>null</code> if access is denied.
	 */
	public Section getContentFromContext(final Element context, final Dataset dataset, final int groupId) {
		
		if (!hasMinGroupId(context.getAttributeValue("minGroupId"), groupId)) {
			return null;
		}
		
		final Section result = new Section();
		final String contextType = context.getAttributeValue("type");
		//TODO Get translated label string for value of labelKey-attribute in the section element  
		result.setLabel(context.getAttributeValue("labelKey"));
		
		String parentSeparator = null;
		if (context.getParentElement().getName().equals("section")) {
			parentSeparator = context.getParentElement().getAttributeValue("separator");
		}
		if (parentSeparator == null) {
			parentSeparator = "<br/>";
		}
		
		final List<Element> children = context.getChildren();
		final String defaultSeparator = "<br/>";
		String separator = context.getAttributeValue("separator"); 
		if (context.getAttributeValue("separator") == null) {
			separator = defaultSeparator;
		}
				
		final FieldList fieldList = new FieldList();
		for (int i = 0; i < dataset.getContextSize(contextType); i++) {
			addFieldsToFieldList(children, fieldList, i, dataset, contextType, separator, groupId);
		}
		
		if (fieldList.size() > 1) {
			result.add(fieldList);
		} else {
			if (fieldList.size() == 1 ) {
				final Field field = new Field();
				field.setValue(fieldList.get(0));
				result.add(field);
			}
		}
		
		if (result.getContent().isEmpty()) {
			return null;
		}
		
		return result;
	}
	
	public Section getContentFromContext(final Element context, final Dataset dataset) {
		return getContentFromContext(context, dataset, -1);
	}

	/**
	 * This function adds the values of all fields of a context to the <code>FieldList</code>. It retrieves the values and formats 
	 * the output according to the XML description of the fields.
	 * @param children The XML descriptions of the fields to add.
	 * @param fieldList The values of the fields in this context.
	 * @param index The index of the context (as <code>Contexts</code> are multivalued a single dataset may have several contexts of a given type).
	 * @param dataset The current dataset.
	 * @param contextType The type of the context.
	 * @param separator the currently active separator.
	 * @param grouId The groupId of the current user.
	 */
	private void addFieldsToFieldList(final List<Element> children, final FieldList fieldList, final int index
			, final Dataset dataset, final String contextType,	final String separator, final int groupId) {
		
		for (Element e: children) {
			if (e.getName().equals("field")) {
				addFieldToFieldList(e, fieldList, index, dataset, contextType, separator, groupId);
			}
		}
	}

	/**
	 * This function adds the value of a field of a context to the <code>FieldList</code>. It retrieves the value and formats 
	 * the output according to the XML description of this field.
	 * @param element The XML description of the field to add.
	 * @param fieldList The values of the fields in this context.
	 * @param index The index of the context (as <code>Contexts</code> are multivalued a single dataset may have several contexts of a given type).
	 * @param dataset The current dataset.
	 * @param contextType The type of the context.
	 * @param separator the currently active separator.
	 * @param grouId The groupId of the current user.
	 */
	private void addFieldToFieldList(final Element element, final FieldList fieldList, final int index
			,final Dataset dataset, final String contextType, final String separator, final int groupId) {
		
		if (!hasMinGroupId(element.getAttributeValue("minGroupId"), groupId)) {
			return;
		}
		
		final String initialValue = dataset.getFieldFromContext(contextType + element.getAttributeValue("datasource"), index);
		StringBuffer value = null;
		if (initialValue != null) {
			value = new StringBuffer(initialValue);
		}
		final String postfix = element.getAttributeValue("postfix");
		final String prefix = element.getAttributeValue("prefix");
		if (value != null) {
			if (prefix != null) {
				value.insert(0, prefix);
			}
			if (postfix != null) {
				value.append(postfix); 
			}
			String currentListValue = null;
			if (!fieldList.getValue().isEmpty() && index < fieldList.size()) {
				currentListValue = fieldList.get(index);
			}
			if (currentListValue == null) {
				fieldList.add(value.toString());
			} else {
				fieldList.modify(index, currentListValue + separator + value);
			}
		}
	}
	
	/**
	 * This function compares the given string with the given integer value and returns <code>true</code> if the string value is less than
	 * the integer value or if <code>minGroupIdStr</code> is <code>null</code>. This is used to check which content the currently logged in user is allowed to see.   
	 * @param minGroupIdStr A string containing the minimum groupId as integer value.
	 * @param groupId The groupId of the currently logged in user as <code>int</code>.
	 * @return A boolean value indicating if the current user is allowed to see the content.
	 */
	private boolean hasMinGroupId(final String minGroupIdStr, final int groupId) {
		if (!StrUtils.isEmptyOrNull(minGroupIdStr)) {
			final int minGroupId = Integer.parseInt(minGroupIdStr);
			LOGGER.debug("minGroupId: " + minGroupId + " - user groupId: " + groupId);
			if (groupId < minGroupId) {
				return false;
			}
		}
		return true;
	}
	
	public List<String> getFacetsFromXMLFile(final String category) {
		final String filename = getFilenameFromType(category);
		if ("unknown".equals(filename)) {
			return null;
		}
		
		final List<String> facetList = new ArrayList<String>();
		
		final ServletContextResource xmlDocument = new ServletContextResource(getServletContext(), filename);
	    try {
	    	final SAXBuilder saxBuilder = new SAXBuilder();
	    	final Document doc = saxBuilder.build(xmlDocument.getFile());
	    	//TODO Make Nicer XML Parsing is very quick and Dirty solution for my Problems 
	    	final Namespace nameSpace = Namespace.getNamespace("http://arachne.uni-koeln.de/schemas/category");
	    	
			// Get facets
 			final Element facets = doc.getRootElement().getChild("facets", nameSpace);
 			for (Element e: facets.getChildren()) {
 				facetList.add(e.getAttributeValue("name")); 				
 			}
 			return facetList;
		} catch (JDOMException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}
	
	public ServletContext getServletContext() {
		return servletContext;
	}
}
