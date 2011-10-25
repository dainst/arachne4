package de.uni_koeln.arachne.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.ServletContextResource;

import de.uni_koeln.arachne.context.*;
import de.uni_koeln.arachne.response.ArachneDataset;

/**
 * This class handles creation and retrieval of contexts and adds them to datasets.
 * Internally it uses <code>Contextualizers</code> to abstract the data access and allow to fetch contexts not only from
 * the Arachne database but from any other datasource (even external ones).  
 */
@Service("arachneContextService")
public class ArachneContextService {
	
	@Autowired
	private ArachneEntityIdentificationService arachneEntityIdentificationService;
	
	/**
	 * Service to access the 'Verknuepfungen' table. The information stored in that table is used
	 * to determine which contexts the <code>addContext</code> method adds to a given dataset.
	 */	
	@Autowired
	private ArachneConnectionService arachneConnectionService;
	
	/**
	 * Service to access ids in 'cross tables'.
	 */
	@Autowired
	private GenericSQLService genericFieldService;
	
	@Autowired
	private ArachneSingleEntityDataService arachneSingleEntityDataService;
	
	@Autowired
	private ServletContext servletContext; 
	
	/**
	 * This methods adds all contexts to the dataset that are found in the XML description.
	 * @param parent The dataset to add the contexts to.
	 */
	public void addMandatoryContexts(ArachneDataset parent) {
		List<String> externalFields = getExternalFields(parent.getArachneId().getTableName());
		List<String> mandatoryContextTypes = new ArrayList<String>();
		for (String currentField: externalFields) {
			String[] contextTypes = currentField.split("\\.");
			if (mandatoryContextTypes.isEmpty() || !mandatoryContextTypes.contains(contextTypes[0])) {
				mandatoryContextTypes.add(contextTypes[0]);
			}
		}
		System.out.println("Mandatory Contexts: " + mandatoryContextTypes);
		for (String contextType: mandatoryContextTypes) {
			ArachneContext context = new ArachneContext(contextType, parent, this);
			context.getFirstContext();
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
	public void addContext(ArachneDataset parent) {
		if (parent.getArachneId().getTableName().equals("bauwerk")) {
			List<String> connectionList = arachneConnectionService.getConnectionList(parent.getArachneId().getTableName());
			Iterator<String> i = connectionList.iterator();
			while (i.hasNext()) {
				ArachneContext context = new ArachneContext(i.next(), parent, this);
				context.getLimitContext(10);
				parent.addContext(context);
			}
			
			ArachneContext litContext = new ArachneContext("Literatur", parent, this);
			litContext.getLimitContext(10);
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
	public List<Link> getLinks(ArachneDataset parent, String contextType, Integer offset, Integer limit) {
	    IContextualizer contextualizer = getContextualizerByContextType(contextType);
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
	private IContextualizer getContextualizerByContextType(String contextType) {
		// TODO The services should not be hardcoded but somehow specified by either contextType or contextualizer 
		//Initialization of contextualizer needs two params
		Class [] classParam = new Class[2];
		classParam[0] = ArachneEntityIdentificationService.class;
		classParam[1] = GenericSQLService.class;
		
		//Initialization of contextualizer needs two params
		Object [] objectParam = new Object[2];
		objectParam[0] = arachneEntityIdentificationService;
		objectParam[1] = genericFieldService;
		
		try {
			String upperCaseContextType = contextType.substring(0, 1).toUpperCase() + contextType.substring(1).toLowerCase();
			String className = "de.uni_koeln.arachne.context." + upperCaseContextType + "Contextualizer";
			System.out.println("Trying to initialize class: " + className);
			Class aClass = Class.forName(className);
			java.lang.reflect.Constructor classConstructor = aClass.getConstructor(classParam);
			return (IContextualizer)classConstructor.newInstance(objectParam);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			//return new GenericSQLContextualizer(contextType, arachneConnectionService, genericFieldService
			//		, arachneEntityIdentificationService, arachneSingleEntityDataService);
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Internally used method to get the external field names from the XML files.
	 * It opens the XML file, creates the DOM and uses <code>getFields</code> to get the values from the dom.
	 * @param type The of the xml file.
	 * @return A list of full qualified external field names.
	 */
	private List<String> getExternalFields(String type) {	
		String filename = "/WEB-INF/xml/"+ type + ".xml";

		ServletContextResource xmlDocument = new ServletContextResource(servletContext, filename);
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(xmlDocument.getFile());

			Element display = doc.getRootElement().getChild("display");

			List<String> result = new ArrayList<String>();
			result.addAll(getFields(display, type));
			return result;		
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Recursive function adding all external fields of an <code>Element</code> and its children to the result list.
	 * @param element The XML node to check for external fields.
	 * @param parentType The type of the <code>ArachneDataset</code>.
	 * @return A list of full qualified external field names.
	 */
	private List<String> getFields(Element element, String parentType) {
		List<String> result = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		List<Element> children = element.getChildren();
		for (Element e:children) {
			result.addAll(getFields(e, parentType));
		}
		
		String nameValue = element.getAttributeValue("name"); 
		if (nameValue != null) {
			if (!nameValue.startsWith(parentType)) {
				result.add(nameValue);
			}
		}
		
		String ifEmptyValue = element.getAttributeValue("ifEmpty");
		if (ifEmptyValue != null) {
			if (!ifEmptyValue.startsWith(parentType)) {
				result.add(ifEmptyValue);
			}
		}
		
		return result;
	}
}
