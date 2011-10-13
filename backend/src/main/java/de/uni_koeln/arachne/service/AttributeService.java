package de.uni_koeln.arachne.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

@Service("attributeService")
public class AttributeService {

	/**
	 * ServletContext used to read the config xmls.
	 */
	@Autowired
	private ServletContext servletContext;

	public List<String> getExternalFields(ArachneDataset dataset) {	
		String filename = "/WEB-INF/xml/"+ dataset.getArachneId().getTableName() + ".xml";

		ServletContextResource xmlDocument = new ServletContextResource(servletContext, filename);
		try {
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(xmlDocument.getFile());

			Element display = doc.getRootElement().getChild("display");

			List<String> result = new ArrayList<String>();
			result.addAll(getFields(display, dataset.getArachneId().getTableName()));
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