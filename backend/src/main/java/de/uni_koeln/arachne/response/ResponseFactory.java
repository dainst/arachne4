package de.uni_koeln.arachne.response;

import java.io.IOException;
import java.util.ArrayList;
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
 * The <code>createX</code> methods may access xml config files to create the response objects. These config files are found in the <code>WEB-INF/xml/</code> directory.
 * Currently only the <code>createFormattedArachneEntity</code> method uses these files so that the naming scheme <code>$(TYPE).xml</code> is sufficient. If other methods
 * want to use different xml config files a new naming scheme is needed.
 * <br>
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
	 * Creates a formatted response object as used by the front-end. The structure of this object is defined in the xml config files.
	 * First the type of the object will be determined from the dataset (e.g. bauwerk). Based on the type the corresponding xml file <code>$(TYPE).xml</code> is read.
	 * The response is then created, according to the xml file, from the dataset.
	 * <br>
	 * The validity of the xml file is not checked!!!
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
	    	Element sections = display.getChild("sections");
	    	List<Content> contentList = new ArrayList<Content>();
	    	// JDOM doesn't handle generics correctly so it issues a type safety warning
			@SuppressWarnings("unchecked")
			List<Element> children = sections.getChildren();
			for (Element e:children) {
	    		contentList.add(getContentFromSections(e, dataset));
	    	}
			
			if (!contentList.isEmpty()) {
				if (contentList.size() == 1) {
					response.setSections(contentList.get(0));
				} else {
					Section sectionContent = new Section();
					sectionContent.setLabel("ContainerSection");
					for (Content c:contentList) {
						sectionContent.add(c);
					}
					response.setSections(sectionContent);
				}
			}		
	    	
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
	 * This function handles sections in the xml config files. It extracts the content from the dataset following the definitions in the xml files
	 * and returns it as a <code>String</code>.
	 * <br>
	 * The validity of the xml file is not checked!!!
	 * @param section The xml section Element to parse.
	 * @param dataset The dataset that contains the SQL query results.
	 * @return A concatenated string containing the sections content.
	 */
	private String getStringFromSections(Element section, ArachneDataset dataset) {
		String result = "";
		// JDOM doesn't handle generics correctly so it issues a type safety warning
		@SuppressWarnings("unchecked")
		List<Element> children = section.getChildren();
		String separator = "\n";
		if (section.getAttributeValue("separator") != null) {
			separator = section.getAttributeValue("separator");
		}
		
		for (Element e:children) {
			if (e.getName().equals("field")) {
				String key = e.getAttributeValue("name");
				String datasetResult = dataset.fields.get(key);
				if (datasetResult != null) {
					if (datasetResult.isEmpty()) {
						key = e.getAttributeValue("ifEmpty");
						if (key != null) {
							if (!key.isEmpty()) {
								datasetResult = dataset.fields.get(key);
							}
						}
					}
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
	 * @return A concatenated string containing the sections content.
	 */
	private Content getContentFromSections(Element section, ArachneDataset dataset) {
		Section result = new Section();
		//TODO Get translated label string for value of labelKey-attribute in the section element  
		result.setLabel(section.getAttributeValue("labelKey"));
		// JDOM doesn't handle generics correctly so it issues a type safety warning
		@SuppressWarnings("unchecked")
		List<Element> children = section.getChildren();
		for (Element e:children) {
			if (e.getName().equals("field")) {
				Field field = new Field();
				field.setValue(dataset.fields.get(e.getAttributeValue("name")));
				if (!field.getValue().isEmpty()) {
					result.add(field);
				}
			} else {
				Section nextSection = (Section)getContentFromSections(e, dataset);
				if (!((Section)nextSection).getContent().isEmpty()) { 
					result.add(nextSection);
				}
			}
		}
		return result;
	}
}