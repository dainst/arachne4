package de.uni_koeln.arachne.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;

import de.uni_koeln.arachne.response.AbstractContent;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Field;
import de.uni_koeln.arachne.response.FieldList;
import de.uni_koeln.arachne.response.LinkField;
import de.uni_koeln.arachne.response.Section;
import de.uni_koeln.arachne.service.IUserRightsService;

/**
 * This class provides functions to find a XML config file by type, extract information based on the XML element from
 * the dataset and grants access to the servlet context.
 * If a class wants to work with the XML config files it should use this class via autowiring or as base class.
 */
@Component("xmlConfigUtil")
public class XmlConfigUtil implements ServletContextAware {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlConfigUtil.class);
	
	/**
	 * The servlet context is needed to load the XML config files. 
	 */
	private transient ServletContext servletContext;
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	private transient SAXBuilder xmlParser = null;
	
	private transient final Map<String, Document> xmlConfigDocuments = new HashMap<String, Document>();
	
	private transient final Map<String, Element> xmlIncludeElements = new HashMap<String, Element>();
	
	public void setServletContext(final ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	/**
	 * This function checks if a config file for the given type exists and returns its filename.
	 * @param type Type of the config to look for.
	 * @return The filename of the XML config file for the given type or <code>"unknown"</code> if no config file is found.
	 */
	private String getDocumentFilenameFromType(final String type) {
		String filename = "/WEB-INF/xml/"+ type + ".xml";
		final ServletContextResource file = new ServletContextResource(servletContext, filename);
		if (!file.exists()) {
			filename = "unknown";
		}
		
		LOGGER.debug("config file: " + filename);
		return filename;
	}
	
	/**
	 * This method returns a XML parser to build the DOM. The parser is only set up once and can be reused.
	 * @return A SAXBuilder object.
	 */
	private SAXBuilder getXMLParser() {
		if (xmlParser == null) {
			xmlParser =	new SAXBuilder(new XMLReaderSAX2Factory(false, "org.apache.xerces.parsers.SAXParser"));
			// explicitly disable validation
			xmlParser.setFeature("http://xml.org/sax/features/validation", false);
		}
		return xmlParser;
	}
	
	/**
	 * This method retrieves a XML document based on the type. It tries to find the corresponding XML file, parses it
	 * and constructs the DOM. Already parsed documents are cached and simply returned. 
	 * @param type The type of the dataset.
	 * @return A XML document or <code>null</code> if none is found.
	 */
	public Document getDocument(final String type) {
		final Document cachedDocument = xmlConfigDocuments.get(type);
		if (cachedDocument == null) {
			return getDocumentFromFile(type);
		} else {
			return cachedDocument;
		}
	}
	
	private Document getDocumentFromFile(final String type) {
		final String filename = getDocumentFilenameFromType(type);
		if ("unknown".equals(filename)) {
			return null;
		}
		
		final ServletContextResource xmlDocument = new ServletContextResource(servletContext, filename);
		final SAXBuilder saxBuilder = getXMLParser();
		
		try {
			final Document document = saxBuilder.build(xmlDocument.getFile());
			replaceIncludesInDocument(document);
			xmlConfigDocuments.put(type, document);
			return document;
		} catch (JDOMException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		
    	return null;
	}
	
	/**
	 * Replaces include elements with the elements from the corresponding XML include files.
	 * @param document
	 */
	private void replaceIncludesInDocument(final Document document) {
		// parse to find include tags
		final Element rootElement = document.getRootElement();
		final Namespace nameSpace = rootElement.getNamespace();
		final Element display = rootElement.getChild("display", nameSpace);
		replaceInclude(display);
		//final Element facets = rootElement.getChild("facets", nameSpace);
		// extract elements from include tags
		// add elements to document
	}
	
	/**
	 * Recursive method to find all include tags in an <code>Element</code> and replace them by their corresponding real <code>Element</code>.
	 * @param element The DOM element to scan for include elements.
	 */
	private void replaceInclude(final Element element) {
		final List<Element> children = element.getChildren();
		
		if (!children.isEmpty()) {
			for (Element e: children) {
				if ("include".equals(e.getName())) {
					element.addContent(getInclude(e));
					element.removeContent(e);
				} else {
					if (!"field".equals(e.getName())) {
						replaceInclude(e);
					}
				}
			}
		}
	}
	
	/**
	 * This method replaces an include element in the DOM by the corresponding real element. The real element is fetched either from the 
	 * cache of include elements or read from file and then added to the cached elements list.
	 * @param include The include element to replace.
	 * @return The real element the include element is replaced with.
	 */
	private Element getInclude(final Element include) {
		final String type = include.getAttributeValue("type");
		
		final Element cachedElement = xmlIncludeElements.get(type);
		if (cachedElement == null) {
			return getElementFromFile(type);
		} else {
			return cachedElement;
		}
	}
	
	/**
	 * Reads an include XML file and returns the contained <code>Element</code>.
	 * @param type The type of the include element.
	 * @return The <code>Element</code> extracted from the DOM of the XML include file.
	 */
	private Element getElementFromFile(final String type) {
		final String filename = getIncludeFilenameFromType(type);
		if ("unknown".equals(filename)) {
			return null;
		}
		
		final ServletContextResource xmlDocument = new ServletContextResource(servletContext, filename);
		final SAXBuilder saxBuilder = getXMLParser();
		
		try {
			final Document document = saxBuilder.build(xmlDocument.getFile());
			final Element rootElement = document.getRootElement();
			final Namespace nameSpace = rootElement.getNamespace();
			// the include element is either a single section or context
			Element element = rootElement.getChild("section", nameSpace);
			if (element == null) {
				element = rootElement.getChild("context", nameSpace);
			}
			if (element != null) {
				element.detach();
				xmlIncludeElements.put(type, element);
			}
			return element;
		} catch (JDOMException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		
    	return null;
	}
	
	/**
	 * This function checks if an include file for the given type exists and returns its filename.
	 * @param type Type of the include to look for.
	 * @return The filename of the XML include file for the given type or <code>"unknown"</code> if no include file is found.
	 */
	private String getIncludeFilenameFromType(final String type) {
		String filename = "/WEB-INF/xml/"+ type + "_inc.xml";
		final ServletContextResource file = new ServletContextResource(servletContext, filename);
		if (!file.exists()) {
			filename = "unknown";
		}
		
		LOGGER.debug("include file: " + filename);
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
		final StringBuilder result = new StringBuilder("");
		final List<Element> children = section.getChildren();
		String separator = "<br/>";
		if (section.getAttributeValue("separator") != null) {
			separator = section.getAttributeValue("separator");
		}

		for (Element e:children) {
			if (e.getName().equals("field")) {
				addFieldStringToResult(e, result, dataset, nameSpace, separator); 
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

	/**
	 * This function adds the content of a field of the dataset as defined in the XML description to the result <code>StringBuilder</code>. 
	 * @param element The XML element describing the field. 
	 * @param result The <code>StringBuilder</code> to add the content to.
	 * @param dataset The current dataset.
	 * @param nameSpace The current namespace.
	 * @param separator The current separator.
	 */
	private void addFieldStringToResult(final Element element, final StringBuilder result, final Dataset dataset, final Namespace nameSpace
			, final String separator) {
		
		final String initialValue = dataset.getField(element.getAttributeValue("datasource"));
		StringBuilder datasetResult = null;
		if (initialValue != null) {
			datasetResult = new StringBuilder(initialValue);
		} 

		final String postfix = element.getAttributeValue("postfix");
		final String prefix = element.getAttributeValue("prefix");

		if (datasetResult == null) {
			datasetResult = getIfEmpty(element, dataset, nameSpace);
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

	/**
	 * Returns the content of a field of the dataset as defined inside an <code>ifEmtpy</code> tag in the XML config file.
	 * It is safe to use even if the passed in <code>Element</code> does not have an <code>ifEmpty-Element</code> as a child.
	 * @param element The XML element describing the parent of the <code>ifEmpty</code> element.
	 * @param dataset The current dataset.
	 * @param nameSpace The current namespace.
	 * @return A <code>StringBuilder</code> containing the formatted value or <code>null</code> if no value could be retrieved or
	 * the passed in <code>Element</code> does not have an <code>ifEmpty-Element</code> as a child.
	 */
	public StringBuilder getIfEmpty(final Element element, final Dataset dataset, final Namespace nameSpace) {
		
		String key;
		StringBuilder result = null;
		final Element ifEmptyElement = element.getChild("ifEmpty", nameSpace);
		if (ifEmptyElement != null) {
			key = ifEmptyElement.getChild("field", nameSpace).getAttributeValue("datasource");
			if (key != null && !key.isEmpty()) {
				final String ifEmptyValue = dataset.getField(key);
				if (ifEmptyValue == null) {
					result = getIfEmpty(ifEmptyElement.getChild("field", nameSpace), dataset, nameSpace); 
				} else {
					result = new StringBuilder(ifEmptyValue);
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
	public AbstractContent getContentFromSections(final Element section, final Dataset dataset) {
		
		if (!hasMinGroupId(section.getAttributeValue("minGroupId"))) {
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
				addFieldToResult(e, result, dataset, separator);
			} else { 
				if (e.getName().equals("linkField")) {
					addLinkFieldToResult(e, result, dataset, separator);
				} else {
					if (e.getName().equals("context")) {
						final Section nextSection = (Section)getContentFromContext(e, dataset);
						if (nextSection != null && !((Section)nextSection).getContent().isEmpty()) { 
							result.add(nextSection);
						}
					} else {
						final Section nextSection = (Section)getContentFromSections(e, dataset);
						if (nextSection != null && !((Section)nextSection).getContent().isEmpty()) { 
							result.add(nextSection);
						}
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
	 */
	private void addFieldToResult(final Element element, final Section result, final Dataset dataset
			, final String separator) {
		
		if (!hasMinGroupId(element.getAttributeValue("minGroupId"))) {
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
	 * Procedure to add a <code>LinkField</code> from the dataset to the result <code>Section</code>.
	 * @param dataset The current dataset.
	 * @param result The <code>Section</code> the field belongs to.
	 * @param separator The currently active separator.
	 * @param element The description of the field as XML element.
	 */
	private void addLinkFieldToResult(final Element element, final Section result, final Dataset dataset
			, final String separator) {

		if (!hasMinGroupId(element.getAttributeValue("minGroupId"))) {
			return;
		}

		final String labelKey = element.getAttributeValue("labelKey");
		if (!StrUtils.isEmptyOrNull(labelKey)) {
			final LinkField linkField = new LinkField(labelKey);
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
				
				linkField.setValue(value.toString());
				linkField.convertValueToLink();
				
				// TODO find better solution as the previous content may be a section
				// If there are more than one field in this section add the value (incl. separator) to the previous field
				if (result.getContent().isEmpty()) {
					result.add(linkField);
				} else {
					final int contentSize = result.getContent().size();
					final Field previousContent = (Field)result.getContent().get(contentSize-1);
					previousContent.setValue(previousContent.getValue() + separator + linkField.getValue());
				}
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
	 * @return A <code>Section</code> object containing the context sections content or <code>null</code> if access is denied.
	 */
	public Section getContentFromContext(final Element context, final Dataset dataset) {
		
		if (!hasMinGroupId(context.getAttributeValue("minGroupId"))) {
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
			addFieldsToFieldList(children, fieldList, i, dataset, contextType, separator);
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
	
	/**
	 * This function adds the values of all fields of a context to the <code>FieldList</code>. It retrieves the values and formats 
	 * the output according to the XML description of the fields.
	 * @param children The XML descriptions of the fields to add.
	 * @param fieldList The values of the fields in this context.
	 * @param index The index of the context (as <code>Contexts</code> are multivalued a single dataset may have several contexts of a given type).
	 * @param dataset The current dataset.
	 * @param contextType The type of the context.
	 * @param separator the currently active separator.
	 */
	private void addFieldsToFieldList(final List<Element> children, final FieldList fieldList, final int index
			, final Dataset dataset, final String contextType,	final String separator) {
		
		for (Element e: children) {
			if (e.getName().equals("field") || e.getName().equals("linkField")) {
				addFieldToFieldList(e, fieldList, index, dataset, contextType, separator);
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
	 */
	private void addFieldToFieldList(final Element element, final FieldList fieldList, final int index
			,final Dataset dataset, final String contextType, final String separator) {
		
		if (!hasMinGroupId(element.getAttributeValue("minGroupId"))) {
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
			
			// handle linkFields
			final String labelKey = element.getAttributeValue("labelKey");
			if (!StrUtils.isEmptyOrNull(labelKey)) {
				value.insert(0, "<a href=\"");
				value.append("\">" + labelKey + "</a>");
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
	 * This function compares the given string with the groupID of the current user and returns <code>true</code> if the string value 
	 * is less than the groupID value or if <code>minGroupIdStr</code> is <code>null</code>. This is used to check which content the 
	 * currently logged in user is allowed to see.   
	 * @param minGroupIdStr A string containing the minimum groupId as integer value.
	 * @return A boolean value indicating if the current user is allowed to see the content.
	 */
	private boolean hasMinGroupId(final String minGroupIdStr) {
		if (!StrUtils.isEmptyOrNull(minGroupIdStr)) {
			final int groupId = userRightsService.getCurrentUser().getGroupID();
			final int minGroupId = Integer.parseInt(minGroupIdStr);
			LOGGER.debug("minGroupId: " + minGroupId + " - user groupId: " + groupId);
			if (groupId < minGroupId) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This method looks up which facets are defined in an XML file describing a category and returns them as a list. 
	 * @param category The name of the category.
	 * @return A <code>List&lt;String></code>
	 */
	public List<String> getFacetsFromXMLFile(final String category) {
		final String filename = getDocumentFilenameFromType(category);
		if ("unknown".equals(filename)) {
			return null;
		}
		
		final List<String> facetList = new ArrayList<String>();
		
		final ServletContextResource xmlDocument = new ServletContextResource(getServletContext(), filename);
	    try {
	    	final SAXBuilder saxBuilder = new SAXBuilder(new XMLReaderSAX2Factory(false, "org.apache.xerces.parsers.SAXParser"));
	    	final Document document = saxBuilder.build(xmlDocument.getFile());
	    	final Namespace nameSpace = document.getRootElement().getNamespace();
	    	
			// Get facets
 			final Element facets = document.getRootElement().getChild("facets", nameSpace);
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
	
	/**
	 * Simple getter to grant access to the <code>ServletContext</code>.
	 * @return The current servlet context.
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}
	
	/**
	 * This method constructs a string list of the cached XML config document names.
	 * @return A list of the cached config document names. 
	 */
	public List<String> getXMLConfigDocumentList() {
		final List<String> result = new ArrayList<String>(); 
		if (!xmlConfigDocuments.keySet().isEmpty()) {
			for (Map.Entry<String, Document> entry: xmlConfigDocuments.entrySet()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	/**
	 * Method to clear the current XML config document cache.
	 */
	public void clearDocumentCache() {
		xmlConfigDocuments.clear();
	}
}
