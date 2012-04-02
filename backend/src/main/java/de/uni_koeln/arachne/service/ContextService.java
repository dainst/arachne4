package de.uni_koeln.arachne.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.ServletContextResource;

import de.uni_koeln.arachne.context.*;
import de.uni_koeln.arachne.response.Dataset;
import de.uni_koeln.arachne.util.StrUtils;
import de.uni_koeln.arachne.util.XmlConfigUtil;

/**
 * This class handles creation and retrieval of contexts and adds them to datasets.
 * Internally it uses <code>Contextualizers</code> to abstract the data access and allow to fetch contexts not only from
 * the Arachne database but from any other datasource (even external ones).  
 */
@Service("arachneContextService")
public class ContextService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextService.class);
	
	@Autowired
	private EntityIdentificationService arachneEntityIdentificationService; // NOPMD
	
	/**
	 * Service to access the 'Verknuepfungen' table. The information stored in that table is used
	 * to determine which contexts the <code>addContext</code> method adds to a given dataset.
	 */	
	@Autowired
	private ConnectionService arachneConnectionService; // NOPMD
	
	/**
	 * Service to access ids in 'cross tables'.
	 */
	@Autowired
	private GenericSQLService genericSQLService; // NOPMD
	
	/**
	 * Utility class to work with the XML config files.
	 */
	@Autowired
	private XmlConfigUtil xmlConfigUtil; // NOPMD
	
	/**
	 * This methods adds all contexts to the dataset that are found in the XML description.
	 * @param parent The dataset to add the contexts to.
	 */
	public void addMandatoryContexts(final Dataset parent) {
		final List<String> externalFields = getExternalFields(parent.getArachneId().getTableName());
		
		final List<String> mandatoryContextTypes = new ArrayList<String>();

		for (String currentField: externalFields) {
			final String[] contextTypes = currentField.split("\\.");
			if (mandatoryContextTypes.isEmpty() || !mandatoryContextTypes.contains(contextTypes[0])) {
				mandatoryContextTypes.add(contextTypes[0]);
			}
		}
				
		LOGGER.debug("Mandatory Contexts: " + mandatoryContextTypes);
		final Iterator<String> contextType = mandatoryContextTypes.iterator();
		while (contextType.hasNext()) {
			final Context context = new Context(contextType.next(), parent, this);
			context.getallContexts();
			parent.addContext(context);
		}
	}
	
	/**
	 * Method to append all context objects to the given dataset.
	 * Some context objects are universal like 'literatur' or 'ort'. They get included
	 * for every dataset type. Other context objects are looked up based on the 'Verknuepfungen'
	 * table.  
	 * 
	 * @param parent ArachneDataset that will gain the added context
	 */
	public void addContext(final Dataset parent) {
		if (parent.getArachneId().getTableName().equals("bauwerk")) {
			final List<String> connectionList = arachneConnectionService.getConnectionList(parent.getArachneId().getTableName());
			final Iterator<String> iterator = connectionList.iterator();
			while (iterator.hasNext()) {
				final Context context = new Context(iterator.next(), parent, this);
				context.getFirstContext();
				parent.addContext(context);
			}
			
			final Context litContext = new Context("Literatur", parent, this);
			litContext.getFirstContext();
			parent.addContext(litContext);
		}
	}
	
	/**
	 * This function retrieves the contexts according to the given criteria.
	 * It uses a context specific contextualizer to fetch the data.
	 * @param parent Instance of an <code>ArachneDataset</code> that will receive the context
	 * @param contextName String that describes the context-type
	 * @param offset Starting position for context listing
	 * @param limit Quantity of contexts 
	 * @return Returns a list of <code>Links</code> 
	 */ 
	public List<AbstractLink> getLinks(final Dataset parent, final String contextType, final Integer offset, final Integer limit) {
		final IContextualizer contextualizer = getContextualizerByContextType(contextType);
	    return contextualizer.retrieve(parent, offset, limit);
	}
	
	/**
	 * Method creating an appropriate contextualizer. The class name is constructed from the <code>contextType</code>.
	 * Then reflection is used to create the corresponding class instance.
	 * <br>
	 * If no specialized <code>Contextualizer</code> class is found an instance of <code>GenericSQLContextualizer</code> is returned.
	 * @param contextType Type of a context of interest  
	 * @return an appropriate contextualizer serving the specific context indicated by the given <code>contextType</code>
	 */
	@SuppressWarnings("rawtypes")
	private IContextualizer getContextualizerByContextType(final String contextType) {
		// TODO The services should not be hardcoded but somehow specified by either contextType or contextualizer 
		//Initialization of contextualizer needs two params
		Class [] classParam = new Class[2];
		classParam[0] = EntityIdentificationService.class;
		classParam[1] = GenericSQLService.class;
		
		//Initialization of contextualizer needs two params
		Object [] objectParam = new Object[2];
		objectParam[0] = arachneEntityIdentificationService;
		objectParam[1] = genericSQLService;
		
		try {
			final String upperCaseContextType = contextType.substring(0, 1).toUpperCase() + contextType.substring(1).toLowerCase();
			final String className = "de.uni_koeln.arachne.context." + upperCaseContextType + "Contextualizer";
			LOGGER.debug("Initializing class: " + className + "...");
			final Class<?> aClass = Class.forName(className);
			final java.lang.reflect.Constructor classConstructor = aClass.getConstructor(classParam);
			return (IContextualizer)classConstructor.newInstance(objectParam);
		} catch (ClassNotFoundException e) {
			LOGGER.debug("FAILURE - using SemanticConnectionsContextualizer instead");
			return new SemanticConnectionsContextualizer(contextType, genericSQLService);
		}
		catch (Exception e) {
			// TODO: handle exception
			LOGGER.error(e.getMessage());
		}
		return null;
	}
	
	/**
	 * Internally used method to get the external field names from the XML files. The name of the XML file to read
	 * is constructed from the </code>type<code>. If no corresponding XML file is found <code>fallback.xml</code> is
	 * used. The method then opens the XML file, creates the DOM and uses <code>getFields</code> to get the values from
	 *  the dom.
	 * @param type The of the xml file.
	 * @return A list of full qualified external field names.
	 */
	private List<String> getExternalFields(final String type) {	
		
		final String filename = xmlConfigUtil.getFilenameFromType(type);
		
		final ServletContextResource xmlDocument = new ServletContextResource(xmlConfigUtil.getServletContext(), filename);
		try {
			final SAXBuilder saxBuilder = new SAXBuilder();
			final Document doc = saxBuilder.build(xmlDocument.getFile());
			final Element rootElement = doc.getRootElement();
			//TODO Make Nicer XML Parsing is very quick and Dirty solution for my Problems
			final Namespace nameSpace = Namespace.getNamespace("http://arachne.uni-koeln.de/schemas/category");
			final Element display = rootElement.getChild("display", nameSpace);
			final Element facets = rootElement.getChild("facets", nameSpace);
			final List<String> result = new ArrayList<String>();
			result.addAll(getFields(display, type));
			result.addAll(getFields(facets, type));
			return result;		
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(e.getMessage());
		}
		return null;
	}
	
	/**
	 * Recursive function adding all external fields of an <code>Element</code> and its children to the result list.
	 * @param element The XML node to check for external fields.
	 * @param parentType The type of the <code>ArachneDataset</code>.
	 * @return A list of full qualified external field names.
	 */
	private List<String> getFields(final Element element, final String parentType) {
		final List<String> result = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		final List<Element> children = element.getChildren();
		
		if ("context".equals(element.getName()) && !children.isEmpty()) {
			getFieldsFromContext(element, parentType, result, children);
		} else {
			getFieldsFromField(element, parentType, result, children);
		}
		
		return result;
	}

	private void getFieldsFromField(final Element element, final String parentType, final List<String> result,
			final List<Element> children) {
		
		if (!children.isEmpty()) {
			for (Element e:children) {
				result.addAll(getFields(e, parentType));
			}
		}

		final String datasourceValue = element.getAttributeValue("datasource");
		if (!StrUtils.isEmptyOrNull(datasourceValue) && !datasourceValue.startsWith(parentType) 
				&& !datasourceValue.startsWith("Dataset")) {
			result.add(datasourceValue);
		}

		final String ifEmptyValue = element.getAttributeValue("ifEmpty");
		if (!StrUtils.isEmptyOrNull(ifEmptyValue) && !ifEmptyValue.startsWith(parentType) 
				&& !datasourceValue.startsWith("Dataset")) {
			result.add(ifEmptyValue);
		}
	}

	private void getFieldsFromContext(final Element element, final String parentType, final List<String> result,
			final List<Element> children) {
		
		final String context = element.getAttributeValue("type");
		for (Element e:children) {
			String datasourceValue = e.getAttributeValue("datasource");  
			if (!StrUtils.isEmptyOrNull(datasourceValue)) {
				datasourceValue = context + datasourceValue; // NOPMD
				if (!datasourceValue.startsWith(parentType) && !datasourceValue.startsWith("Dataset")) {
					result.add(datasourceValue);
				}
			}
		}
	}
}
