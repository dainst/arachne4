package de.uni_koeln.arachne.response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import de.uni_koeln.arachne.service.IUserRightsService;
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
@Configurable(preConstruction=true)
public class ResponseFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFactory.class);
	
	@Autowired
	private transient XmlConfigUtil xmlConfigUtil;
	
	// needed for testing
	public void setXmlConfigUtil(final XmlConfigUtil xmlConfigUtil) {
		this.xmlConfigUtil = xmlConfigUtil;
	}
	
	@Autowired
	private transient IUserRightsService userRightsService;
	
	/**
	 * Creates a formatted response object as used by the front-end. The structure of this object is defined in the xml config files.
	 * First the type of the object will be determined from the dataset (e.g. bauwerk). Based on the type the corresponding xml file <code>$(TYPE).xml</code> is read.
	 * The response is then created, according to the xml file, from the dataset.
	 * <br>
	 * The validity of the xml file is not checked!!!
	 * @param dataset The dataset which encapsulates the SQL query results.
	 * @return A <code>FormattedArachneEntity</code> instance which can be jsonized.
	 */
	public FormattedArachneEntity createFormattedArachneEntity(final Dataset dataset) {
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
		Date lastModified;
		try {
			lastModified = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.GERMAN).parse(
					dataset.getFieldFromFields(tableName + ".lastModified"));
		} catch (Exception e) {
			lastModified = null;
		}
		response.setLastModified(lastModified);
		
	   	final Document document = xmlConfigUtil.getDocument(tableName);
    	final Namespace nameSpace = document.getRootElement().getNamespace();
    	final Element display = document.getRootElement().getChild("display",nameSpace);
    		    	
    	// set title
    	final String titleStr = getTitleString(dataset, nameSpace, display);
    	response.setTitle(titleStr);
    	
    	// set subtitle
    	final String subtitleStr = getSubTitle(dataset, nameSpace, display);
    	response.setSubtitle(subtitleStr);
    	
    	// set datasection
    	setSections(dataset, nameSpace, display, response);
		
		// Set images
		response.setImages(dataset.getImages());
		
		// Set facets
		final Element facets = document.getRootElement().getChild("facets", nameSpace);
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
	
	private void setSections(final Dataset dataset, final Namespace nameSpace, final Element display
			, final FormattedArachneEntity response) {

		final Element sections = display.getChild("datasections", nameSpace);
		final List<AbstractContent> contentList = new ArrayList<AbstractContent>();
		
		final List<Element> children = sections.getChildren();
		for (Element e:children) {
			if (e.getName().equals("section")) {
				final Section section = (Section)xmlConfigUtil.getContentFromSections(e, dataset);
				if (section != null && !section.getContent().isEmpty()) {
					contentList.add(section);
				}
			} else {
				final Section section = (Section)xmlConfigUtil.getContentFromContext(e, dataset);
				if (section != null && !section.getContent().isEmpty()) {
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
	
	/**
	 * This function retrieves the facets from the current config document and the corresponding values from the dataset.
	 * @param dataset The current dataset.
	 * @param facets The facet element of the current config file.
	 * @return A list of facets.
	 */
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
							if (userRightsService.isUserSolr()) {
								values.add(name + "$" + value);
							} else {
								values.add(value);
							}
						}
					} else {
						if ("context".equals(childName)) {
							getFacetContext(dataset, child, values, name);
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
	
	/**
	 * This function retrieves the facets from a context element and the corresponding values from the dataset.
	 * @param dataset The current dataset.
	 * @param child The context element of the current facet element.
	 * @param values A list of facets to add the new facets to.
	 * @param name The name of the current facet.
	 */
	private void getFacetContext(final Dataset dataset, final Element child, final List<String> values, final String name) {
		
		final Section section = xmlConfigUtil.getContentFromContext(child, dataset);
		if (section != null) {
			for (AbstractContent c:section.getContent()) {
				if (c instanceof FieldList) {
					for (String value: ((FieldList)c).getValue()) {
						if (value != null) {
							if (userRightsService.isUserSolr()) {
								values.add(name + "$" + value);
							} else {
								values.add(value);
							}
						}
					}
				} else {
					final String value = c.toString();
					if (value != null) {
						if (userRightsService.isUserSolr()) {
							values.add(name + "$" + value);
						} else {
							values.add(value);
						}
					}
				}
			} 	 								
		}
	}

	public BaseArachneEntity createResponseForDeletedEntity(final EntityId entityId) {
		return new DeletedArachneEntity(entityId);
	}
}