package de.uni_koeln.arachne.responseobjects;

import java.io.IOException;

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
		ServletContextResource xmlDocument = new ServletContextResource(servletContext, filename);
	    try {
	    	SAXBuilder sb = new SAXBuilder();
	    	Document doc = sb.build(xmlDocument.getFile());
	    	
	    	Element display = doc.getRootElement().getChild("display");
	    	
	    	// set title
	    	String titleKey = display.getChild("title").getChild("field").getAttributeValue("name");
	    	response.setTitle(dataset.fields.get(titleKey));
	    	
	    	// TODO implement when the xml definitions are fixed
	    	// set subtitle
	    	/*Element subtitle = display.getChild("subtitle");
	    	if (subtitle.getChild("field") != null) {
	    		
	    	} else {
	    		
	    	}*/
	    	
	    	System.out.println(display.getChild("subtitle"));
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
}