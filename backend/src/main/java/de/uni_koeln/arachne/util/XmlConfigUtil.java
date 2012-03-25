package de.uni_koeln.arachne.util;

import java.util.List;

import javax.servlet.ServletContext;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextResource;

import de.uni_koeln.arachne.response.Content;
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
	
	private static final Logger logger = LoggerFactory.getLogger(XmlConfigUtil.class);
	
	/**
	 * Servlet context to load the XML config files. 
	 */
	@Autowired
	private ServletContext servletContext;
	
	/**
	 * This function checks if a config file for the given type exists and returns its filename.
	 * @param type Type of the config to look for.
	 * @return The filename of the XML config file for the given type or <code>null</code>.
	 */
	public String getFilenameFromType(String type) {
		String filename = "/WEB-INF/xml/"+ type + ".xml";
		ServletContextResource file = new ServletContextResource(servletContext, filename);
		if (!file.exists()) {
			filename = null;
		}
		
		logger.debug("config file: " + filename);
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
	public String getStringFromSections(Element section, Dataset dataset) {
		String result = "";
		// JDOM doesn't handle generics correctly so it issues a type safety warning
		@SuppressWarnings("unchecked")
		List<Element> children = section.getChildren();
		String separator = "<br/>";
		if (section.getAttributeValue("separator") != null) {
			separator = section.getAttributeValue("separator");
		}
		
		for (Element e:children) {
			if (e.getName().equals("field")) {
				String key = e.getAttributeValue("datasource");
				String datasetResult = dataset.getField(key);
				String postfix = e.getAttributeValue("postfix");
				String prefix = e.getAttributeValue("prefix");
				if (StrUtils.isEmptyOrNull(datasetResult)) {
					Element ifEmptyElement = e.getChild("ifEmpty");
					if (ifEmptyElement != null) {
						// TODO discuss if multiple fields inside an ifEmpty tag make sense
						key = ifEmptyElement.getChild("field").getAttributeValue("datasource");
						if (key != null) {
							if (!key.isEmpty()) {
								datasetResult = dataset.getField(key);
							}
						}
					}
				}
				if (!StrUtils.isEmptyOrNull(datasetResult)) {
					if (prefix != null) datasetResult = prefix + datasetResult;
					if (postfix != null) datasetResult += postfix;
					if (!result.isEmpty() && !datasetResult.isEmpty()) {
						result += separator;
					}
					result += datasetResult;
				} 
			} else {
				String datasetResult = getStringFromSections(e, dataset);
				if (!result.isEmpty() && !datasetResult.isEmpty()) {
					result += separator;
				}
				result += datasetResult;
			}
		}
		return result;
	}
	
	/**
	 * This function handles sections in the xml config files. It extracts the content from the dataset following the definitions in the xml files
	 * and returns it as <code>Content</code>.
	 * <br>
	 * The validity of the xml file is not checked!!!
	 * @param parent The xml section <code>Element</code> to parse.
	 * @param dataset The dataset that contains the SQL query results.
	 * @return A <code>Content</code> object containing the sections content.
	 */
	public Content getContentFromSections(Element section, Dataset dataset) {
		Section result = new Section();
		//TODO Get translated label string for value of labelKey-attribute in the section element  
		result.setLabel(section.getAttributeValue("labelKey"));
		// JDOM doesn't handle generics correctly so it issues a type safety warning
		@SuppressWarnings("unchecked")
		List<Element> children = section.getChildren();
		String defaultSeparator = "<br/>";
		String separator = section.getAttributeValue("separator"); 
		if (section.getAttributeValue("separator") == null) {
			separator = defaultSeparator;
		}
		for (Element e:children) {
			if (e.getName().equals("field")) {
				Field field = new Field();
				String value = dataset.getField(e.getAttributeValue("datasource"));
				String postfix = e.getAttributeValue("postfix");
				String prefix = e.getAttributeValue("prefix");
				if (value != null) {
					if (prefix != null) value = prefix + value;
					if (postfix != null) value += postfix; 
					
					// TODO find better solution as the previous content may be a section
					// If there are more than one field in this section add the value (incl. separator) to the previous field
					if (!result.getContent().isEmpty()) {
						int contentSize = result.getContent().size();
						Field previousContent = (Field)result.getContent().get(contentSize-1);
						previousContent.setValue(previousContent.getValue() + separator +value);
					} else {
						field.setValue(value);
						result.add(field);
					}
				}
			} else {
				if (e.getName().equals("context")) {
					Section nextSection = (Section)getContentFromContext(e, dataset);
					if (nextSection != null) {
						if (!((Section)nextSection).getContent().isEmpty()) { 
							result.add(nextSection);
						}
					}
				} else {
					Section nextSection = (Section)getContentFromSections(e, dataset);
					if (nextSection != null) {
						if (!((Section)nextSection).getContent().isEmpty()) { 
							result.add(nextSection);
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * This function handles context elements in the xml config files. It extracts the content from the dataset 
	 * following the definitions in the xml files and returns it as <code>Content</code>.
	 * <br>
	 * The validity of the xml file is not checked!!!
	 * @param context The xml context <code>Element</code> to parse.
	 * @param dataset The dataset that contains the SQL query results.
	 * @return A <code>Content</code> object containing the context sections content.
	 */
	public Section getContentFromContext(Element context, Dataset dataset) {
		Section result = new Section();
		String contextType = context.getAttributeValue("type");
		//TODO Get translated label string for value of labelKey-attribute in the section element  
		result.setLabel(context.getAttributeValue("labelKey"));
		
		String parentSeparator = null;
		if (context.getParentElement().getName().equals("section")) {
			parentSeparator = context.getParentElement().getAttributeValue("separator");
		}
		if (parentSeparator == null) {
			parentSeparator = "<br/>";
		}
		
		// JDOM doesn't handle generics correctly so it issues a type safety warning
		@SuppressWarnings("unchecked")
		List<Element> children = context.getChildren();
		String defaultSeparator = "<br/>";
		String separator = context.getAttributeValue("separator"); 
		if (context.getAttributeValue("separator") == null) {
			separator = defaultSeparator;
		}
				
		FieldList fieldList = new FieldList();
		for (int i = 0; i < dataset.getContextSize(contextType); i++) {
			for (Element e: children) {
				if (e.getName().equals("field")) {
					String value = dataset.getFieldFromContext(contextType + e.getAttributeValue("datasource"), i);
					String postfix = e.getAttributeValue("postfix");
					String prefix = e.getAttributeValue("prefix");
					if (value != null) {
						if (prefix != null) value = prefix + value;
						if (postfix != null) value += postfix; 
						String currentListValue = null;
						if (!fieldList.getValue().isEmpty() && i < fieldList.size()) {
							currentListValue = fieldList.get(i);
						}
						if (currentListValue != null) {
							fieldList.modify(i, currentListValue + separator + value);
						} else {
							fieldList.add(value);
						}
					}
				}
			}
		}
		if (fieldList.size() > 1) {
			result.add(fieldList);
		} else {
			if(fieldList.size() == 1 ){
				Field field = new Field();
				field.setValue(fieldList.get(0));
				result.add(field);
			}
		}
		if (result.getContent().isEmpty()) {
			return null;
		}
		return result;
	}
	
	public ServletContext getServletContext() {
		return servletContext;
	}
}
