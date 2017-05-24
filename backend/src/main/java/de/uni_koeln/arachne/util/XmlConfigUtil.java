package de.uni_koeln.arachne.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.beans.factory.annotation.Autowired;

import de.uni_koeln.arachne.context.ContextImageDescriptor;
import de.uni_koeln.arachne.response.AbstractContent;
import de.uni_koeln.arachne.response.ContextEntity;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.response.Field;
import de.uni_koeln.arachne.response.FieldList;
import de.uni_koeln.arachne.response.LinkField;
import de.uni_koeln.arachne.response.Section;
import de.uni_koeln.arachne.util.sql.TableConnectionDescription;
import de.uni_koeln.arachne.service.Transl8Service;
import de.uni_koeln.arachne.service.Transl8Service.Transl8Exception;

/**
 * This class provides functions to find a XML config file by type, extract information based on the XML element from
 * the dataset and grants access to the servlet context.
 * If a class wants to work with the XML config files it should use this class via autowiring or as base class.
 */
@Component("xmlConfigUtil")
public class XmlConfigUtil implements ServletContextAware {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlConfigUtil.class);
	
	private static final String LINK_PREFIX = "$link$";
	
	private static final String DEFAULT_SECTION_SEPARATOR = "<hr>";
		
	/**
	 * The servlet context is needed to load the XML config files. 
	 */
	private transient ServletContext servletContext;
	
	private transient SAXBuilder xmlParser = null;
	
	private transient final Map<String, Document> xmlConfigDocuments = new HashMap<String, Document>();
	
	private transient final Map<String, List<Element>> xmlIncludeElements = new HashMap<>();
	
	private transient final Map<String, List<String>> mandatoryContextNames = new HashMap<>();
	
	private transient final Map<String, List<String>> explicitContextualizers = new HashMap<>();
	
	private transient final Map<String, List<ContextImageDescriptor>> contextImageDescriptors = new HashMap<>();
	
	private transient final Map<String, List<TableConnectionDescription>> subCategories = new HashMap<>();
	
	private transient final Map<String, Set<String>> facets = new HashMap<>();

	@Autowired
    private transient Transl8Service ts;
	
	/**
	 * Convenience method to clear the current XML config document, element, mandatory contexts and context image 
	 * descriptors cache.
	 */
	public void clearCache() {
		xmlConfigDocuments.clear();
		xmlIncludeElements.clear();
		mandatoryContextNames.clear();
		contextImageDescriptors.clear();
		subCategories.clear();
		facets.clear();
	}
	
	/**
	 * This function handles context elements in the xml config files. It extracts the content from the dataset 
	 * following the definitions in the xml files and returns it as <code>Content</code>.
	 * <br>
	 * The validity of the xml file is not checked!!!
	 * @param context The xml context <code>Element</code> to parse.
	 * @param namespace The namespace of the document.
	 * @param dataset The dataset that contains the SQL query results.
	 * @return A <code>Section</code> object containing the context sections content or <code>null</code> if access is denied.
	 */
	public Section getContentFromContext(final Element context, final Namespace namespace, final Dataset dataset, final String lang) {
		
		final Section result = new Section();
		final String contextType = context.getAttributeValue("type");

        try {
            result.setLabel(ts.transl8(context.getAttributeValue("labelKey"), lang));
        }
        catch (Transl8Exception e) {
            LOGGER.error("Failed to contact transl8. Cause: ", e);
        }

		String parentSeparator = null;
		if (context.getParentElement().getName().equals("section")) {
			parentSeparator = context.getParentElement().getAttributeValue("separator");
		}
		if (parentSeparator == null) {
			parentSeparator = DEFAULT_SECTION_SEPARATOR;
		}
		
		// Are there any contextSection-Tags within the current context? 
		final List<Element> contextSections = context.getChildren("contextSection", namespace);		
		if (contextSections == null || contextSections.isEmpty()) {
			final List<Element> children = context.getChildren();
			
			final String defaultSeparator = DEFAULT_SECTION_SEPARATOR;
			String separator = context.getAttributeValue("separator"); 
			if (context.getAttributeValue("separator") == null) {
				separator = defaultSeparator;
			}
			result.setSeparator(separator);
			
			// Get link attribute and validate usage
			String link = context.getAttributeValue("link");
			if (!StrUtils.isEmptyOrNull(link)) {
				Element firstField = context.getChild("field", namespace);
				if (firstField != null && !LINK_PREFIX.equals(firstField.getAttributeValue("prefix"))) {
					LOGGER.error("Invalid use of context attribute 'link' in context type '" + contextType 
							+ "'. The 'prefix' for the first field must be '$link$'.");
					// Disable link creation
					link = null;
				}
			}
			
			FieldList fieldList = new FieldList();
			for (int i = 0; i < dataset.getContextSize(contextType); i++) {
				addFieldsToFieldList(children, context.getNamespace(), fieldList, i, dataset, contextType, separator, lang);
			}
			
			if (link != null) {
				final FieldList tempFieldList = new FieldList();
				for (int index = 0; index < fieldList.size(); index++) {
					final String field = fieldList.get(index);
					int separatorIndex = field.indexOf(separator);
					if (field.startsWith(LINK_PREFIX) && separatorIndex > -1) {
						StringBuilder newValue = new StringBuilder(32).append("<a href=\"");
						newValue.append(link);
						newValue.append(field.substring(LINK_PREFIX.length(), separatorIndex));
						newValue.append("\" target=\"_blank\">");
						newValue.append(field.substring(separatorIndex + separator.length()));
						newValue.append("</a>");
						tempFieldList.add(newValue.toString());
					} else {
						tempFieldList.add(fieldList.get(index));						
					}					
				}
				fieldList = tempFieldList;
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
		} else {
			// Iterate over all contexts of the current type 			
			for (int i = 0; i < dataset.getContextSize(contextType); i++) {

				final ContextEntity curSectionContent = new ContextEntity();

				// Iterate over all contextSections within the current processed context
				for (final Element curSection: contextSections) {
					final FieldList fieldList = new FieldList();
					final Section localContext = new Section();
					
					final String defaultSeparator = DEFAULT_SECTION_SEPARATOR;
					String separator = curSection.getAttributeValue("separator"); 
					if (curSection.getAttributeValue("separator") == null) {
						separator = defaultSeparator;
					}
					localContext.setSeparator(separator);

					// store the section label of the current context
					final List<Element> childFields = curSection.getChildren();

                    try {
                        localContext.setLabel(ts.transl8(curSection.getAttributeValue("labelKey"), lang));
                    }
                    catch (Transl8Exception e) {
                        LOGGER.error("Failed to contact transl8. Cause: ", e);
                    }

					// add all child-fields of the current contextSection and retrieve their values
					for (final Element childField: childFields) {
						addContextFieldToFieldList(childField, context.getNamespace(), fieldList, i, dataset, contextType, separator, lang);
					}
					
					// only add to list if fields contain content
					if (fieldList.size() != 0) {
						localContext.add(fieldList);
						curSectionContent.add(localContext);
					}
				}
				result.add(curSectionContent);
			}
			if (result.getContent().isEmpty()) {
				return null;
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
	 * @param namespace The namespace of the document.
	 * @param dataset The dataset that contains the SQL query results.
	 * @return A <code>Content</code> object containing the sections content.
	 */
	public AbstractContent getContentFromSections(final Element section, final Namespace namespace, final Dataset dataset, final String lang) {
	    try {
            final Section result = new Section();
            result.setLabel(ts.transl8(section.getAttributeValue("labelKey"), lang));

            final List<Element> children = section.getChildren();

            final String defaultSeparator = DEFAULT_SECTION_SEPARATOR;
            String separator = section.getAttributeValue("separator");
            if (section.getAttributeValue("separator") == null) {
                separator = defaultSeparator;
            }
            result.setSeparator(separator);

            for (final Element element:children) {
                switch (element.getName()) {
                case "field":
                    addFieldToResult(element, namespace, result, dataset, separator);
                    break;

                case "linkField":
                    addLinkFieldToResult(element, result, dataset, separator, lang);
                    break;

                case "context":
                    final Section nextContext = (Section)getContentFromContext(element, namespace, dataset, lang);
                    if (nextContext != null && !((Section)nextContext).getContent().isEmpty()) {
                        result.add(nextContext);
                    }
                    break;

                default:
                    final Section nextSection = (Section)getContentFromSections(element, namespace, dataset, lang);
                    if (nextSection != null && !((Section)nextSection).getContent().isEmpty()) {
                        result.add(nextSection);
                    }
                    break;
                }
            }
                return result;
        }
        catch (Transl8Exception e) {
            LOGGER.error("Failed to contact transl8. Cause: ", e);
            return null;
        }
	}
	
	/**
	 * Methode retrieves ContextImageDescriptor-instances. If these instances have beeen requested before, the method 
	 * returns cached instances, else it parses the instances from the XML-document
	 * @param type The type of the dataset.
	 * @return A list of context image desciptors.
	 */
	public List<ContextImageDescriptor> getContextImagesNames(final String type) {
		final List<ContextImageDescriptor> cachedImageContextDescriptors = contextImageDescriptors.get(type);
		if(cachedImageContextDescriptors == null) {
			final List<ContextImageDescriptor> descriptors = getImageContextNames(type);
			contextImageDescriptors.put(type, descriptors);
			return descriptors;
		} else {
			return cachedImageContextDescriptors;
		}
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
	
	/**
	 * Returns a list of contextualizers found in the <code>explicitContextualizers</code> tag of the xml file. If there 
	 * is a cached version this is returned else the list is created, cached and then returned.
	 * @param type The category of the parent dataset.
	 * @return A list containing the names of the explicit contextualizers (may be empty).
	 */
	public List<String> getExplicitContextualizers(final String type) {
		final List<String> cachedContextualizers = explicitContextualizers.get(type);
		if (cachedContextualizers == null) {
			final Document document = getDocument(type);
			if (document == null) {
				return new ArrayList<String>();
			}
			
			final Element rootElement = document.getRootElement();
			final Namespace nameSpace = rootElement.getNamespace();
			final Element contextualizers = rootElement.getChild("explicitContextualizers", nameSpace);
			if (contextualizers != null) {
				final List<String> explicitContextualizerTypeList = 
						StrUtils.getCommaSeperatedStringAsList(contextualizers.getTextNormalize());

				if (!StrUtils.isEmptyOrNull(explicitContextualizerTypeList)) {
					explicitContextualizers.put(type, explicitContextualizerTypeList);
					return explicitContextualizerTypeList;
				}
			}
			return new ArrayList<String>();
		} else {
			return cachedContextualizers;
		}
	}
	
	/**
	 * This method looks up which facets are defined in an XML file describing a category. 
	 * @param type The name of the category.
	 * @return A <code>Set&lt;String></code> of the category specific facets.
	 */
	public Set<String> getFacetsFromXMLFile(final String type) {
		Set<String> cachedFacets = facets.get(type);
		if (cachedFacets == null) {
			final Set<String> facetList = new HashSet<>();

			final Document document = getDocument(type);
			if (document != null) {
				final Namespace namespace = document.getRootElement().getNamespace();

				final Element facets = document.getRootElement().getChild("facets", namespace);

				for (final Element element: facets.getChildren()) {
					facetList.add(element.getAttributeValue("name")); 				 				
				}
			}
			return facetList;
		} else {
			return cachedFacets;
		}
	}
	
	/**
	 * Returns the content of a field of the dataset as defined inside an <code>ifEmtpy</code> tag in the XML config file.
	 * It is safe to use even if the passed in <code>Element</code> does not have an <code>ifEmpty-Element</code> as a child.
	 * @param field The XML element describing the parent of the <code>ifEmpty</code> element.
	 * @param dataset The current dataset.
	 * @param namespace The current namespace.
	 * @return A <code>StringBuilder</code> containing the formatted value or <code>null</code> if no value could be retrieved or
	 * the passed in <code>Element</code> does not have an <code>ifEmpty-Element</code> as a child.
	 */
	public StringBuilder getIfEmptyFromField(final Element field, final Namespace namespace, final Dataset dataset) {
		String key;
		StringBuilder result = null;
		final Element ifEmptyElement = field.getChild("ifEmpty", namespace);
		if (ifEmptyElement != null) {
			key = ifEmptyElement.getChild("field", namespace).getAttributeValue("datasource");
			if (key != null && !key.isEmpty()) {
				final String ifEmptyValue = dataset.getField(key);
				if (ifEmptyValue == null) {
					result = getIfEmptyFromField(ifEmptyElement.getChild("field", namespace), namespace , dataset); 
				} else {
					result = new StringBuilder(16).append(ifEmptyValue);
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the content of a field of the dataset as defined inside an <code>ifEmtpy</code> tag in a 
	 * <code>Context</code> tag in the XML config file.
	 * It is safe to use even if the passed in <code>Element</code> does not have an <code>ifEmpty</code>-Element as a 
	 * child.
	 * @param element The XML element describing the parent of the <code>ifEmpty</code> element.
	 * @param namespace The current namespace.
	 * @param dataset The current dataset.
	 * @param contextType The type of the context.
	 * @param index The index of the context.
	 * @return A <code>StringBuilder</code> containing the formatted value or <code>null</code> if no value could be 
	 * retrieved or the passed in <code>Element</code> does not have an <code>ifEmpty</code>-Element as a child.
	 */
	public StringBuilder getIfEmptyContext(final Element element, final Namespace namespace, final Dataset dataset
			, final String contextType, final int index) {
		String key;
		StringBuilder result = null;
		final Element ifEmptyElement = element.getChild("ifEmpty", namespace);

		if (ifEmptyElement != null) {
			key = ifEmptyElement.getChild("field", namespace).getAttributeValue("datasource");
			if (key != null && !key.isEmpty()) {
				if (key.charAt(0) == '.') {
					key = contextType + key; // NOPMD
				}
				final String ifEmptyValue = dataset.getFieldFromContext(key, index);
				if (ifEmptyValue == null) {
					result = getIfEmptyContext(ifEmptyElement.getChild("field", namespace), namespace , dataset, contextType, index); 
				} else {
					result = new StringBuilder(16).append(ifEmptyValue);
				}
			}
		}
		return result;
	}
	
	/**
	 * This method returns the list of unique context names that are needed to retrieve all data for a given category. The list is only 
	 * created if it is not cached.
	 * @param type The category of the parent dataset.
	 * @return A list containing the names of the mandatory contexts or may be empty if the type does not need external contexts. 
	 */
	public List<String> getMandatoryContextNames(final String type) {
		final List<String> cachedContextList = mandatoryContextNames.get(type);
		if (cachedContextList == null) {
			final List<String> externalFields = getExternalFields(type);
			final List<String> mandatoryContextTypes = new ArrayList<String>();
			
			if (externalFields != null) { 
				for (final String currentField: externalFields) {
					final String[] contextTypes = currentField.split("\\.");
					if (mandatoryContextTypes.isEmpty() || !mandatoryContextTypes.contains(contextTypes[0])) {
						mandatoryContextTypes.add(contextTypes[0]);
					}
				}
				mandatoryContextNames.put(type, mandatoryContextTypes);
				return mandatoryContextTypes;
			}
			return new ArrayList<String>();
		} else {
			return cachedContextList;
		}
	}
	
	/**
	 * Simple getter to grant access to the <code>ServletContext</code>.
	 * @return The current servlet context.
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}
	
	/**
	 * Returns a list of subCategories that may contain additional information needed for the dataset (like 'objektkeramik' 
	 * for 'objekt'). So this methods handles the subCategories tag in the xml file and caches the information.
	 * @param type The category type.
	 * @return A list of <code>TableConnectionDescriptions</code>. The list may be empty.
	 */
	public List<TableConnectionDescription> getSubCategories(final String type) {
		// TODO replace corresponding query builder and handle 'PrimaryKey' here
		List<TableConnectionDescription> result = subCategories.get(type);
		if (result != null) {
			return result;
		} else {
			result = new ArrayList<TableConnectionDescription>();

			final Document document = getDocument(type);
			if (document == null) {
				return result;
			}

			final Element rootElement = document.getRootElement();
			final Namespace nameSpace = rootElement.getNamespace();
			final Element subtables = rootElement.getChild("subtables", nameSpace);
			if (subtables != null) {
				for (Element subtable : subtables.getChildren()) {
					result.add(new TableConnectionDescription(type, subtable.getAttributeValue("connectFieldParent")
							, subtable.getTextNormalize(), subtable.getAttributeValue("connectFieldSubTable")));	
				}
			}
			
			return result;
		}
	}
	
	/**
	 * This method constructs a string list of the cached XML config document names.
	 * @return A list of the cached config document names. 
	 */
	public List<String> getXMLConfigDocumentList() {
		final List<String> result = new ArrayList<String>(); 
		if (!xmlConfigDocuments.keySet().isEmpty()) {
			for (final Map.Entry<String, Document> entry: xmlConfigDocuments.entrySet()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}
		
	/**
	 * This method constructs a string list of the cached XML include element names.
	 * @return A list of the cached include element names. 
	 */
	public List<String> getXMLIncludeElementList() {
		final List<String> result = new ArrayList<String>(); 
		if (!xmlIncludeElements.keySet().isEmpty()) {
			for (final Map.Entry<String, List<Element>> entry: xmlIncludeElements.entrySet()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}
	
	/**
	 * This method returns a XML parser to build the DOM. The parser is only set up once and can be reused.
	 * @return A SAXBuilder object.
	 */
	public SAXBuilder getXMLParser() {
		if (xmlParser == null) {
			xmlParser =	new SAXBuilder(new XMLReaderSAX2Factory(false, "org.apache.xerces.parsers.SAXParser"));
			// explicitly disable validation
			xmlParser.setFeature("http://xml.org/sax/features/validation", false);
		}
		return xmlParser;
	}
		
	public void setServletContext(final ServletContext servletContext) {
		this.servletContext = servletContext;
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
	 * @return the separator that must be used next.
	 */
	private String addContextFieldToFieldList(final Element element, final Namespace namespace, final FieldList fieldList, final int index
			,final Dataset dataset, final String contextType, String separator, final String lang) {
		
		final String initialValue = dataset.getFieldFromContext(contextType + element.getAttributeValue("datasource"), index);
		
		StringBuilder value = null;
		if (initialValue == null) {
			value = getIfEmptyContext(element, namespace, dataset, contextType, index);
		} else {
			value = new StringBuilder(16).append(initialValue);
		}
		
		value = processValueEdits(element, value);	
	
		final String postfix = element.getAttributeValue("postfix");
		final String prefix = element.getAttributeValue("prefix");
		final String overrideSeparator = element.getAttributeValue("overrideSeparator");
		separator = (overrideSeparator == null) ? separator : overrideSeparator ;
		
		if (value != null) {
			if (prefix != null) {
				value.insert(0, prefix);
			}
			if (postfix != null) {
				value.append(postfix); 
			}
			
			// handle linkFields
			if (element.getName().contentEquals("linkField")){
				try {
                    final String labelKey = ts.transl8(element.getAttributeValue("labelKey"), lang);
                    if (StrUtils.isEmptyOrNullOrZero(labelKey)) {
                        value = new StringBuilder(32).append("<a href=\"")
                                .append(value.toString())
                                .append("\"  target=\"_blank\">")
                                .append(value.toString())
                                .append("</a>");
                    } else {
                        value.insert(0, "<a href=\"");
                        value.append("\" target=\"_blank\">" + labelKey + "</a>");
                    }
				}
                catch (Transl8Exception e) {
                    LOGGER.error("Failed to contact transl8. Cause: ", e);
				}
			}
			
			String nextSeparator = element.getAttributeValue("separator");
			String currentListValue = null;
			if (!fieldList.getValue().isEmpty() && index < fieldList.size()) {
				currentListValue = fieldList.get(index);
			}
			if (currentListValue == null) {
				fieldList.add(value.toString());
				nextSeparator = (nextSeparator != null) ? nextSeparator : separator;
				return nextSeparator;
			} else {
				fieldList.modify(index, currentListValue + separator + value);
				return nextSeparator;
			}
		}
		return separator;
	}
	
	/**
	 * Internal function adds the passed field to the passed result
	 * @param field Field, whose datasource is added
	 * @param parentType The category of the dataset
	 * @param result The result list containing the full qualified field names.
	 * @param context Context which is used to qualify the field names
	 */
	private void addContextFieldToList(final Element field,
			final String parentType, final List<String> result,
			final String context) {
		String datasourceValue = field.getAttributeValue("datasource");
		if (!StrUtils.isEmptyOrNullOrZero(datasourceValue)) {
			datasourceValue = context + datasourceValue; // NOPMD
			// Exception for objekt-Subgroups e.g. objektkeramik, these are handled specially
			// Add Exception for Fabric / Fabricdescription, Surfacetreatment / SurfacetreatmentAction
			// as they shouldnt be handled as Object-/Sub-Object => the problem is, that the child-type
			// starts with the name of the parent
			if ((!datasourceValue.startsWith(parentType)
					&& !datasourceValue.startsWith("Dataset")) || "surfacetreatment".equals(parentType) || "fabric".equals(parentType)) {
				result.add(datasourceValue);
			}
		}
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
	private void addFieldsToFieldList(final List<Element> children, final Namespace namespace, final FieldList fieldList, final int index
			, final Dataset dataset, final String contextType, final String separator, final String lang) {
		
		String nextSeparator = separator;
		for (final Element element: children) {
			if (element.getName().equals("field") || element.getName().equals("linkField")) {
				nextSeparator = addContextFieldToFieldList(element, namespace, fieldList, index, dataset, contextType
						, nextSeparator, lang);
				nextSeparator = (nextSeparator != null) ? nextSeparator : separator; 
			}
		}
	}
	
	/**
	 * Procedure to add a <code>Field</code> from the dataset to the result <code>Section</code>.
	 * @param dataset The current dataset.
	 * @param result The <code>Section</code> the field belongs to.
	 * @param separator The currently active separator.
	 * @param element The description of the field as XML element.
	 */
	private void addFieldToResult(final Element element, final Namespace namespace, final Section result, final Dataset dataset
			, String separator) {
		
		final Field field = new Field();
		StringBuilder value = null;
		final String initialValue = dataset.getField(element.getAttributeValue("datasource"));
		if (initialValue == null) {
			value = getIfEmptyFromField(element, namespace, dataset);
		} else {
			value = new StringBuilder(16).append(initialValue);
		}
		
		value = processValueEdits(element, value);
		
		final String postfix = element.getAttributeValue("postfix");
		final String prefix = element.getAttributeValue("prefix");
		final String overrideSeparator = element.getAttributeValue("overrideSeparator");
		separator = (overrideSeparator == null) ? separator : overrideSeparator;
				
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
			, String separator, final String lang) {

        try {
            final String labelKey = ts.transl8(element.getAttributeValue("labelKey"), lang);
            if (!StrUtils.isEmptyOrNullOrZero(labelKey) || element.getChild("field") != null) {
                final LinkField linkField = new LinkField(labelKey);
                StringBuilder value = null;
                final String initialValue = dataset.getField(element.getAttributeValue("datasource"));
                if (initialValue != null) {
                    value = new StringBuilder(16).append(initialValue);
                }

                value = processValueEdits(element, value);

                final String postfix = element.getAttributeValue("postfix");
                final String prefix = element.getAttributeValue("prefix");
                final String overrideSeparator = element.getAttributeValue("overrideSeparator");
                separator = (overrideSeparator == null) ? separator : overrideSeparator;

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
        catch (Transl8Exception e) {
            LOGGER.error("Failed to contact transl8. Cause: ", e);
        }
	}
	
	/**
	 * Retrieves the context names of the contexts that are referenced in contextImage tags of the XML documents. 
	 * @param type The parents type.
	 * @return A list of unique context names.
	 */
	private List<String> getContextImageContextNames(final String type) {
		final List<ContextImageDescriptor> contextImageDescriptors = getContextImagesNames(type);
		List<String> result = new ArrayList<String>();
		if (contextImageDescriptors != null) {
			for (final ContextImageDescriptor contextImageDescriptor : contextImageDescriptors) {
				final String name = contextImageDescriptor.getContextName();
				if (!result.contains(name)) {
					result.add(name);
				}
			}
		}
		
		return result;
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
	 * Function to load a xml category file from disk, build a document and put the resulting document into the cache.  
	 * @param type A <code>String</code> specifying the category type.
	 * @return A fully assembled document.
	 */
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
	 * Reads an include XML file and returns the contained <code>Elements</code>.
	 * @param type The type of the include elements.
	 * @return The <code>Elements</code> extracted from the DOM of the XML include file.
	 */
	private List<Element> getElementsFromFile(final String type) {
		final String filename = getIncludeFilenameFromType(type);
		if ("unknown".equals(filename)) {
			return null;
		}
		
		final ServletContextResource xmlDocument = new ServletContextResource(servletContext, filename);
		final SAXBuilder saxBuilder = getXMLParser();
		
		try {
			final Document document = saxBuilder.build(xmlDocument.getFile());
			final Element rootElement = document.getRootElement();
			final Namespace namespace = rootElement.getNamespace();
			// the include element may be either a list of section, of context or of facet elements
			List<Element> elements = rootElement.getChildren("section", namespace);
			if (elements.isEmpty()) {
				elements = rootElement.getChildren("context", namespace);
				if (elements.isEmpty()) {
					elements = rootElement.getChildren("facet", namespace);
				}
			}
			if (!elements.isEmpty()) {
				List<Element> clonedElements = new ArrayList<>();
				List<Element> detachedElements = new ArrayList<>();
				while (!elements.isEmpty()) {
					Element element = elements.get(elements.size()-1).detach();
					detachedElements.add(element);
					clonedElements.add(element.clone());
				}
				xmlIncludeElements.put(type, clonedElements);
				return detachedElements;
			}
			return elements;
		} catch (JDOMException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		
    	return null;
	}

	/**
	 * Internally used method to get the external field names from the XML documents. If no corresponding document is found 
	 * <code>null</code> is returned. The method uses <code>getFields</code> to get the values from the document.
	 * @param type The type of the document.
	 * @return A list of full qualified external field names.
	 */
	private List<String> getExternalFields(final String type) {	
		final Document document = getDocument(type);
		if (document == null) {
			return null;
		}
		
		final Element rootElement = document.getRootElement();
		final Namespace nameSpace = rootElement.getNamespace();
		final Element display = rootElement.getChild("display", nameSpace);
		final Element facets = rootElement.getChild("facets", nameSpace);
		final List<String> result = new ArrayList<String>();
		result.addAll(getFieldNames(display, type));
		result.addAll(getFieldNames(facets, type));
		result.addAll(getContextImageContextNames(type));
		return result;		
	}
	
	/**
	 * Recursive function adding all external field names of an <code>Element</code> and its children to the result list.
	 * @param element The XML node to check for external fields.
	 * @param parentType The type of the <code>ArachneDataset</code>.
	 * @return A list of full qualified external field names.
	 */
	private List<String> getFieldNames(final Element element, final String parentType) {
		final List<String> result = new ArrayList<String>();
		final List<Element> children = element.getChildren();
		if ("context".equals(element.getName()) && !children.isEmpty()) {
			getFieldNamesFromContext(element, parentType, result, children);
		} else {
			getFieldNamesFromField(element, parentType, result, children);
		}
		return result;
	}
		
	/**
	 * Internal function adding field names from contexts to the <code>result</code>.
	 * @param element The element to process.
	 * @param parentType The category of the dataset.
	 * @param result The result list containing the full qualified field names.
	 * @param children The children of <code>element</code>.
	 */
	private void getFieldNamesFromContext(final Element element,
			final String parentType, final List<String> result,
			final List<Element> children) {

		final String context = element.getAttributeValue("type");

		for (final Element childElement : children) {
			if (childElement.getName().equals("contextSection")) {
				final List<Element> contextSectionChildren = childElement.getChildren();
				for (final Element sectionChild : contextSectionChildren) {
					addContextFieldToList(sectionChild, parentType, result,
							context);
				}
			} else {
				addContextFieldToList(childElement, parentType, result, context);
			}
		}
	}
	
	/**
	 * Internal function adding field names from fields to the <code>result</code>. Only names that don't start with the parent type 
	 * name are considered. For example 'objektkeramik' is not listed in the results for parentType 'objekt'. The reason is that 
	 * such fields are intrinsic to the dataset so no contextualizer shall be used on them.
	 * @param element The element to process.
	 * @param parentType The category of the dataset.
	 * @param result The result list containing the full qualified field names.
	 * @param children The children of <code>element</code>.
	 */
	private void getFieldNamesFromField(final Element element, final String parentType, final List<String> result,
			final List<Element> children) {
		
		if (!children.isEmpty()) {
			for (final Element childElement: children) {
				result.addAll(getFieldNames(childElement, parentType));
			}
		}

		final String datasourceValue = element.getAttributeValue("datasource");
		if (!StrUtils.isEmptyOrNullOrZero(datasourceValue) && !datasourceValue.startsWith(parentType) 
				&& !datasourceValue.startsWith("Dataset")) {
			result.add(datasourceValue);
		}

		final String ifEmptyValue = element.getAttributeValue("ifEmpty");
		if (!StrUtils.isEmptyOrNullOrZero(ifEmptyValue) && !ifEmptyValue.startsWith(parentType) 
				&& !datasourceValue.startsWith("Dataset")) {
			result.add(ifEmptyValue);
		}
	}

	/**
	 * Methode retrieves context-image-descriptor instances from xml-descriptions
	 * @param type Name of the category / XML-Document from which the descriptor-instance are created
	 * @return List containing an ContextImageDescriptor-instance for every contextImage-Element within the XML-document
	 */
	private List<ContextImageDescriptor> getImageContextNames(final String type) {
		final Document document = getDocument(type);
		if (document == null) {
			return new ArrayList<ContextImageDescriptor>();
		}
		
		final Element rootElement = document.getRootElement();
		final Namespace namespace = rootElement.getNamespace();
		final Element display = rootElement.getChild("display", namespace);
		
		final Element contextImages = display.getChild("contextImages", namespace);
		if (contextImages == null) {
			return new ArrayList<ContextImageDescriptor>();
		}
		
		final List<Element> contextImagesList = contextImages.getChildren("contextImage", namespace);
		if (contextImagesList == null || contextImagesList.isEmpty()) {
			return new ArrayList<ContextImageDescriptor>();
		}
		
		final List<ContextImageDescriptor> result = new ArrayList<ContextImageDescriptor>(contextImagesList.size());
		for (final Element currentContext: contextImagesList) {
			final String context = currentContext.getValue();
			final String usage = currentContext.getAttributeValue("show");
			result.add(new ContextImageDescriptor(context, usage));
		}
 		return result;
	}

	/**
	 * This method replaces an include element in the DOM by the corresponding real element. The real element is fetched either from the 
	 * cache of include elements or read from file and then added to the cached elements list.
	 * @param include The include element to replace.
	 * @return The real element the include element is replaced with.
	 */
	private List<Element> getInclude(final Element include) {
		final String type = include.getAttributeValue("type");
		
		final List<Element> cachedElements = xmlIncludeElements.get(type);
		if (cachedElements == null) {
			return getElementsFromFile(type);
		} else {
			List<Element> clonedElements = new ArrayList<>();
			for ( Element element : cachedElements) clonedElements.add(element.clone());
			return clonedElements;
		}
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
	 * Method implementing the text edit attributes of the XML elements.
	 * @param element Element containing the value.
	 * @param value Stringbuilder which contains the current element-content.
	 * @return The edited value.
	 */
	private StringBuilder processValueEdits(final Element element, final StringBuilder value) {
		if (value == null || value.length() < 1) {
			return value;
		}
		
		final String search = element.getAttributeValue("search");
		final String replace = element.getAttributeValue("replace");
		final String trimEnd = element.getAttributeValue("trimEnd");
		
		if (search != null && replace != null) {
			Matcher matcher = Pattern.compile(search).matcher(value);
			while (matcher.find()) {
				value.replace(matcher.start(), matcher.end(), replace);
				matcher.region(matcher.start() + replace.length(), value.length());
			}
		}
		
		if (trimEnd != null) {
			int endIndex = value.lastIndexOf(trimEnd);
			if (endIndex > 0) {
				value.delete(endIndex, value.length());
			}
		}
		
		return value;
	}
	
	/**
	 * Recursive method to find all include tags in an <code>Element</code> and replace them by their corresponding real 
	 * <code>Element</code>.
	 * @param element The DOM element to scan for include elements.
	 */
	private void replaceInclude(final Element element) {
		if(element != null) {
			final List<Element> children = element.getChildren();

			if (!children.isEmpty()) {
				final List<Element> staticChildren = new ArrayList<Element>(children);
				for (final Element currentElement : staticChildren) {
					if ("include".equals(currentElement.getName())) {
						element.setContent(element.indexOf(currentElement), getInclude(currentElement));
					} else {
						if (!"field".equals(currentElement.getName())) {
							replaceInclude(currentElement);
						}
					}
				}
			}
		}
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
		final Element facets = rootElement.getChild("facets", nameSpace);
		replaceInclude(facets);
	}
}
