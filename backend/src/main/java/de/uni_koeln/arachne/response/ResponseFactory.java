package de.uni_koeln.arachne.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextResource;

import de.uni_koeln.arachne.util.EntityId;
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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFactory.class);
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	/**
	 * Creates a formatted response object as used by the front-end. The structure of this object is defined in the xml config files.
	 * First the type of the object will be determined from the dataset (e.g. bauwerk). Based on the type the corresponding xml file <code>$(TYPE).xml</code> is read.
	 * The response is then created, according to the xml file, from the dataset.
	 * <br>
	 * The validity of the xml file is not checked!!!
	 * @param dataset The dataset which encapsulates the SQL query results.
	 * @return A <code>FormattedArachneEntity</code> instance which can be jsonized.
	 */
	public FormattedArachneEntity createFormattedArachneEntity(final Dataset dataset, final int groupId) {
		LOGGER.debug("dataset: " + dataset);
		
		final FormattedArachneEntity response = new FormattedArachneEntity();
		
		// set id content
		final EntityId arachneId = dataset.getArachneId(); 
		final String tableName = arachneId.getTableName();
		response.setEntityId(arachneId.getArachneEntityID());
		response.setType(tableName);
		response.setInternalId(arachneId.getInternalKey());
		
		// set thumbnailId
		response.setThumbnailId(dataset.getThumbnailId());
		
		// set dataset group
		// workaround for table marbilder as it does not adhere to the naming conventions
		String datasetGroupFieldName = null;
		if ("marbilder".equals(tableName)) {
			datasetGroupFieldName = "marbilder.DatensatzGruppeMARBilder";
		} else {
			datasetGroupFieldName = tableName+".DatensatzGruppe"+tableName.substring(0,1).toUpperCase()+tableName.substring(1);
		}
		response.setDatasetGroup(dataset.getFieldFromFields(datasetGroupFieldName));		
		
		// set lastModified
		response.setLastModified(dataset.getFieldFromFields(tableName + ".lastModified"));
		
		final String filename = xmlConfigUtil.getFilenameFromType(response.getType());
		
		if ("unknown".equals(filename)) {
			return null;
		}
		
		final ServletContextResource xmlDocument = new ServletContextResource(xmlConfigUtil.getServletContext(), filename);
	    try {
	    	final SAXBuilder saxBuilder = new SAXBuilder();
	    	final Document doc = saxBuilder.build(xmlDocument.getFile());
	    	//TODO Make Nicer XML Parsing is very quick and Dirty solution for my Problems 
	    	final Namespace nameSpace = Namespace.getNamespace("http://arachne.uni-koeln.de/schemas/category");
	    	final Element display = doc.getRootElement().getChild("display",nameSpace);
	    	
	    	// set title
	    	final String titleStr = getTitleString(dataset, nameSpace, display);
	    	response.setTitle(titleStr);
	    	
	    	// set subtitle
	    	final String subtitleStr = getSubTitle(dataset, nameSpace, display);
	    	response.setSubtitle(subtitleStr);
	    	
	    	// set datasections
	    	setSections(dataset, nameSpace, display, response, groupId);
			
			// Set images
 			response.setImages(dataset.getImages());
			
			// Set facets
 			final Element facets = doc.getRootElement().getChild("facets", nameSpace);
 			response.setFacets(getFacets(dataset, facets).getList());
 			
			
			// Set contexts
			/*
			Section contextContent = new Section();
			contextContent.setLabel("Contexts");
			
		    for (Context aC: dataset.getContext()) { 
		    	
		    	Section specificContext = new Section();
		    	specificContext.setLabel(aC.getContextType());
		    	
		    	for(AbstractLink link: aC.getallContexts()) {	    		
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
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		return response;
	}

	private String getTitleString(final Dataset dataset, final Namespace nameSpace, final Element display) {
		
		String result = "";
		final Element title = display.getChild("title", nameSpace);
    	if (title.getChild("field", nameSpace) == null) {
    		result = xmlConfigUtil.getStringFromSections(dataset, nameSpace, title.getChild("section", nameSpace));
    	} else {
    		result = dataset.getField(title.getChild("field", nameSpace).getAttributeValue("datasource"));
    	}
    	return result;
	}
	
	private String getSubTitle(final Dataset dataset, final Namespace nameSpace, final Element display) {
		
		String result = "";
		final Element subtitle = display.getChild("subtitle", nameSpace);
		if (subtitle.getChild("field", nameSpace) == null) {
			result = xmlConfigUtil.getStringFromSections(dataset, nameSpace, subtitle.getChild("section", nameSpace));
		} else {
			result = dataset.fields.get(subtitle.getChild("field", nameSpace).getAttributeValue("datasource", nameSpace));
		}
		return result;
	}
	
	private void setSections(final Dataset dataset, final Namespace nameSpace, final Element display, final FormattedArachneEntity response
			, final int groupId) {

		final Element sections = display.getChild("datasections", nameSpace);
		final List<AbstractContent> contentList = new ArrayList<AbstractContent>();
		// JDOM doesn't handle generics correctly so it issues a type safety warning
		final List<Element> children = sections.getChildren();
		for (Element e:children) {
			if (e.getName().equals("section")) {
				final Section section = (Section)xmlConfigUtil.getContentFromSections(e, dataset, groupId);
				if (!section.getContent().isEmpty()) {
					contentList.add(section);
				}
			} else {
				final Section section = (Section)xmlConfigUtil.getContentFromContext(e, dataset, groupId);
				if (!section.getContent().isEmpty()) {
					contentList.add(section);
				}
			}
		}


		if (!contentList.isEmpty()) {
			if (contentList.size() == 1) {
				response.setSections(contentList.get(0));
			} else {
				final Section sectionContent = new Section();
				sectionContent.setLabel("ContainerSection");
				for (AbstractContent c:contentList) {
					sectionContent.add(c);
				}
				response.setSections(sectionContent);
			}
		}
	}
	
	private FacetList getFacets(final Dataset dataset, final Element facets) {
		
		final FacetList result = new FacetList();
		// JDOM doesn't handle generics correctly so it issues a type safety warning
		final List<Element> children = facets.getChildren();
		for (Element e:children) {
			if ("facet".equals(e.getName())) {
				final String name = e.getAttributeValue("name");
				final String labelKey = e.getAttributeValue("labelKey");
				final Facet facet = new Facet(name, labelKey);
				final Element child = (Element)e.getChildren().get(0); 
				if (child != null) {
					final List<String> values = new ArrayList<String>();
					final String childName = child.getName();
					if ("field".equals(childName)) {
						final String value = dataset.getField(child.getAttributeValue("datasource"));
						if (value != null) {
							values.add(value);
						}
					} else {
						if ("context".equals(childName)) {
							getFacetContext(dataset, child, values);
						}
					}
					if (!values.isEmpty()) {
						facet.setValues(values);
					}
					if (!facet.getValues().isEmpty()) {
						result.add(facet);
					}
				}
			}
		}
		return result;
	}

	private void getFacetContext(final Dataset dataset, final Element child, final List<String> values) {
		
		final Section section = xmlConfigUtil.getContentFromContext(child, dataset);
		if (section != null) {
			for (AbstractContent c:section.getContent()) {
				if (c instanceof FieldList) {
					for (String value: ((FieldList)c).getValue()) {
						if (value != null) {
							values.add(value);
						}
					}
				} else {
					final String value = c.toString();
					if (value != null) {
						values.add(value);
					}
				}
			} 	 								
		}
	}
}