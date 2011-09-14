package de.uni_koeln.arachne.responseobjects;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextResource;

import de.uni_koeln.arachne.util.ArachneId;

/**
 * Factory class to create the different kinds of responses from a dataset.
 * The createX methods my access xml config files to create the response objects. These config files are found in the WEB-INF/xml/ directory.
 * This class can be autowired.
 */
@Component
public class ResponseFactory {
	/**
	 * ServletContext used to read the config xmls.
	 */
	@Autowired
	private ServletContext servletContext;
	
	/**
	 * Creates a formatted response object as used by the front-end.
	 * @param dataset The dataset which encapsulates the SQL query results.
	 * @return A <code>FormattedArachneEntity</code> instance which can be jsonized.
	 */
	public FormattedArachneEntity createFormattedArachneEntity(ArachneDataset dataset) {
		FormattedArachneEntity response = new FormattedArachneEntity();
		
		// set id content
		ArachneId arachneId = dataset.getArachneId(); 
		response.setId(arachneId.getArachneEntityID());
		response.setType(arachneId.getTableName());
		response.setInternalId(arachneId.getInternalKey());
				
		String filename = "/WEB-INF/xml/"+ response.getType() + ".xml";
		
		System.out.println("Reading: " + "/WEB-INF/xml/"+ response.getType() + ".xml");
		
		ServletContextResource xmlDocument = new ServletContextResource(servletContext, filename);
	    try {
	    	SAXBuilder sb = new SAXBuilder();
	    	Document doc = sb.build(xmlDocument.getFile());
	    	
	    	Element display = doc.getRootElement().getChild("display");
	    	
	    	// set title
	    	String titleStr = "";
	    	if (display.getChild("title").getChild("field") != null) {
	    		titleStr = dataset.fields.get(display.getChild("title").getChild("field").getAttributeValue("name"));
	    	} else {
	    		titleStr = getStringFromSections(display.getChild("title").getChild("section"), dataset);
	    	}
	    		    	
	    	response.setTitle(titleStr);
	    	
	    	// set subtitle
	    	String subtitleStr = "";
	    	Element subtitle = display.getChild("subtitle");
	    	if (subtitle.getChild("field") != null) {
	    		subtitleStr = dataset.fields.get(subtitle.getChild("field").getAttributeValue("name"));
	    	} else {
	    		subtitleStr = getStringFromSections(subtitle.getChild("section"), dataset);
	    	}
	    	response.setSubtitle(subtitleStr);
	    	
	    	// set sections
	    	/*List<Element> sections = display.getChild("sections").getChildren();
	    	Iterator<Element> i = sections.iterator(); 
	    	while (i.hasNext()) {
	    		
	    	}*/
	    	
	    	
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * Function to extract content from the dataset depending on its xml definition file.
	 * @param section The xml section Element to parse.
	 * @param dataset The dataset that contains the SQL query results.
	 * @return A concatenated string containing the sections content.
	 */
	private String getStringFromSections(Element section, ArachneDataset dataset) {
		String result = "";
		// TODO remove warning
		List<Element> children = section.getChildren();
		String separator = "\n";
		if (section.getAttributeValue("separator") != null) {
			separator = section.getAttributeValue("separator");
		}
		for (Element e:children) {
			if (e.getName().equals("field")) {
				String key = e.getAttributeValue("name");
				String datasetResult = dataset.fields.get(key);
				if (datasetResult.isEmpty()) {
					key = e.getAttributeValue("ifEmpty");
					if ((key != null) || (!key.isEmpty())) {
						datasetResult = dataset.fields.get(key);
					}
				}
				if (!result.isEmpty() && !datasetResult.isEmpty()) {
					result += separator;
				}
				result += datasetResult;
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
}