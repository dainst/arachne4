package de.uni_koeln.arachne.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.ServletContextResource;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.response.Content;
import de.uni_koeln.arachne.sqlutil.ArachneSQLToolbox;

/**
 * This service retrieves the external fields that are declared inside the corresponding
 * xml configuration file.
 */
@Service("attributeService")
public class AttributeService {

	/**
	 * ServletContext used to read the config xmls.
	 */
	@Autowired
	private ServletContext servletContext;

	/**
	 * Used to fetch values from the database via dynamic SQL.
	 */
	@Autowired
	GenericFieldService genericFieldService;
	
	/**
	 * Adds the external fields by full qualified name and their respective values to the datasets
	 * map of fields.
	 * @param dataset The <code>ArachneDataset</code> to add the fields/values to.
	 */
	public void addExternalFields(ArachneDataset dataset) {
		Map<String, String> externalFieldMap = new HashMap<String, String>();
		List<String> externalFields = getExternalFields(dataset.getArachneId().getTableName());
		String tableName = dataset.getArachneId().getTableName();
		for (String currentField : externalFields) {
			String[] tableAndField = currentField.split("\\.", 2);
			List<String> queryResult = genericFieldService.getStringField(tableAndField[0], tableName
					, dataset.getArachneId().getInternalKey(), tableAndField[1]);
			if (queryResult != null) {
				if (!queryResult.isEmpty()) {
					System.out.println("Field: " + currentField + " Query: " + queryResult.get(0) + "(" + queryResult.isEmpty() + ")");
					// TODO handle multiple values
					externalFieldMap.put(currentField, queryResult.get(0));
				}
			}
		}
		
		dataset.appendFields(externalFieldMap);
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