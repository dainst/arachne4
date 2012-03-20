package de.uni_koeln.arachne.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextResource;

import de.uni_koeln.arachne.util.ArachneId;
import de.uni_koeln.arachne.util.XmlConfigUtil;

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
	@Autowired
	private XmlConfigUtil xmlConfigUtil;
	
	/**
	 * Creates a formatted response object as used by the front-end. The structure of this object is defined in the xml config files.
	 * First the type of the object will be determined from the dataset (e.g. bauwerk). Based on the type the corresponding xml file <code>$(TYPE).xml</code> is read.
	 * The response is then created, according to the xml file, from the dataset.
	 * <br>
	 * The validity of the xml file is not checked!!!
	 * @param dataset The dataset which encapsulates the SQL query results.
	 * @return A <code>FormattedArachneEntity</code> instance which can be jsonized.
	 */
	@SuppressWarnings("unchecked")
	public FormattedArachneEntity createFormattedArachneEntity(Dataset dataset) {
		// TODO remove debug
		System.out.println("Constructing formatted response object...");
		System.out.println("dataset: " + dataset);
		
		FormattedArachneEntity response = new FormattedArachneEntity();
		
		// set id content
		ArachneId arachneId = dataset.getArachneId(); 
		String tableName = arachneId.getTableName();
		response.setId(arachneId.getArachneEntityID());
		response.setType(tableName);
		response.setInternalId(arachneId.getInternalKey());
		
		// set dataset group
		String datasetGroupFieldName = tableName+".DatensatzGruppe"+tableName.substring(0,1).toUpperCase()+tableName.substring(1);
		response.setDatasetGroup(dataset.getFieldFromFields(datasetGroupFieldName));		
		
		String filename = xmlConfigUtil.getFilenameFromType(response.getType());
		
		if (filename == null) {
			return null;
		}
		
		ServletContextResource xmlDocument = new ServletContextResource(xmlConfigUtil.getServletContext(), filename);
	    try {
	    	SAXBuilder sb = new SAXBuilder();
	    	Document doc = sb.build(xmlDocument.getFile());
	    	//TODO Make Nicer XML Parsing is very quick and Dirty solution for my Problems 
	    	Namespace ns = Namespace.getNamespace("http://arachne.uni-koeln.de/schemas/category");
	    	Element display = doc.getRootElement().getChild("display",ns);
	    	
	    	// set title
	    	Element title = display.getChild("title", ns);
	    	String titleStr = "";
	    	if (title.getChild("field") != null) {
	    		titleStr = dataset.getField(title.getChild("field", ns).getAttributeValue("datasource"));
	    	} else {
	    		titleStr = xmlConfigUtil.getStringFromSections(title.getChild("section", ns), dataset);
	    	}
	    	response.setTitle(titleStr);
	    	
	    	// set subtitle
	    	String subtitleStr = "";
	    	Element subtitle = display.getChild("subtitle", ns);
	    	if (subtitle.getChild("field", ns) != null) {
	    		subtitleStr = dataset.fields.get(subtitle.getChild("field", ns).getAttributeValue("datasource", ns));
	    	} else {
	    		subtitleStr = xmlConfigUtil.getStringFromSections(subtitle.getChild("section", ns), dataset);
	    	}
	    	response.setSubtitle(subtitleStr);
	    	
	    	// set sections
	    	Element sections = display.getChild("datasections", ns);
	    	List<Content> contentList = new ArrayList<Content>();
	    	// JDOM doesn't handle generics correctly so it issues a type safety warning
			List<Element> children = sections.getChildren();
			for (Element e:children) {
				if (e.getName().equals("section")) {
					contentList.add(xmlConfigUtil.getContentFromSections(e, dataset)); 
				} else {
					contentList.add(xmlConfigUtil.getContentFromContext(e, dataset));
				}
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
			
			// Set images
 			response.setImages(dataset.getImages());
			
			// Set facets
 			FacetList facets = new FacetList();
 			
 			Element facetsElement = doc.getRootElement().getChild("facets", ns);
 			children.clear();
 			// JDOM doesn't handle generics correctly so it issues a type safety warning
 			children = facetsElement.getChildren();
 			for (Element e:children) {
 				if (e.getName().equals("facet")) {
 					String name = e.getAttributeValue("name");
 					String labelKey = e.getAttributeValue("labelKey");
 					Facet facet = new Facet(name, labelKey);
 					Element child = (Element)e.getChildren().get(0); 
 					if (child != null) {
 						List<String> values = new ArrayList<String>();
 	 					String childName = child.getName();
 						if (childName == "field") {
 							String value = dataset.getField(child.getAttributeValue("datasource"));
 	 						if (value != null) {
 	 							values.add(value);
 	 						}
 	 					} else {
 	 						if (childName == "context") {
 	 							Section section = xmlConfigUtil.getContentFromContext(child, dataset);
 	 							if (section != null) {
 	 								for (Content c:section.getContent()) {
 	 									if (c instanceof FieldList) {
 	 										for (String value: ((FieldList)c).getValue()) {
 	 											if (value != null) {
 	 												values.add(value);
 	 											}
 	 										}
 	 									} else {
 	 										String value = c.toString();
 	 										if (value != null) {
 	 											values.add(value);
 	 										}
 	 									}
 	 								} 	 								
 	 							}
 	 						}
 	 					}
 	 					if (!values.isEmpty()) {
 	 						facet.setValues(values);
 	 					}
 	 					if (!facet.getValues().isEmpty()) {
 	 						facets.add(facet);
 	 					}
 					}
 				}
 			}
 			
 			response.setFacets(facets.getList());
 			
			
			// Set contexts
			/*
			Section contextContent = new Section();
			contextContent.setLabel("Contexts");
			
		    for(ArachneContext aC: dataset.getContext()) { 
		    	
		    	Section specificContext = new Section();
		    	specificContext.setLabel(aC.getContextType());
		    	
		    	for(Link link: aC.getallContexts()) {	    		
		    		if(link.getClass().getSimpleName().equals("ArachneLink")) {
		    			ArachneLink aL = (ArachneLink) link;
		    			Section specificContextContent = new Section();
		    			specificContextContent.setLabel(aL.getEntity2().getArachneId().getInternalKey().toString());
		    			specificContext.add(specificContextContent);
		    		}
		    	}
		    	contextContent.add(specificContext);
		    }
			
		    response.setContext(contextContent);
	    	*/
	    	
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