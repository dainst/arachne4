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

import de.uni_koeln.arachne.util.EntityId;
import de.uni_koeln.arachne.util.StrUtils;
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
    	final Namespace namespace = document.getRootElement().getNamespace();
    	final Element display = document.getRootElement().getChild("display", namespace);
    		    	
    	// set title
    	final String titleStr = getTitleString(dataset, namespace, display);
    	response.setTitle(titleStr);
    	
    	// set subtitle
    	final String subtitleStr = getSubTitle(dataset, namespace, display);
    	response.setSubtitle(subtitleStr);
    	
    	// set datasection
    	setSections(dataset, namespace, display, response);
		
		// Set images
		response.setImages(dataset.getImages());
		
		// Set facets via reflection - not the best way but the least invasive
		// TODO: rewrite faceting so that no reflection is needed
		final Element facets = document.getRootElement().getChild("facets", namespace);
		final List<Facet> facetList = getFacets(dataset, namespace, facets).getList();
		for (final Facet facet: facetList ) {
			try {
				final Class<?> facettedArachneEntityClass = response.getClass().getSuperclass();
				final java.lang.reflect.Field facetField = facettedArachneEntityClass.getDeclaredField("facet_"+facet.getName());
				facetField.set(response, facet.getValues());
			} catch (NoSuchFieldException e) {
				LOGGER.error("Invalid facet definition 'facet_" + facet.getName() + "' in '" + tableName + ".xml'. The facet field is not defined in " +
						"FacettedArachneEntity.java. This facet will be ignored.");
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
				
		//response.setFacets(getFacets(dataset, namespace, facets).getList());
		
		//Set additional Content
		response.setAdditionalContent(dataset.getAdditionalContent());
		
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
	
	/**
	 * Method to construct a response object for a deleted entity.
	 * @param entityId The ID of the entity.
	 * @return A custom response object for the deleted entity. 
	 */
	public BaseArachneEntity createResponseForDeletedEntity(final EntityId entityId) {
		return new DeletedArachneEntity(entityId);
	}

	/**
	 * Retrieves the title for the response.
	 * @param dataset The current dataset.
	 * @param namespace The document namespace.
	 * @param display The display element.
	 * @return A <code>String</code> containing the concatenated values of the <code>title</code> tag.
	 */
	private String getTitleString(final Dataset dataset, final Namespace namespace, final Element display) {
		
		String result = "";
		final Element title = display.getChild("title", namespace);
    	if (title.getChild("field", namespace) == null) {
    		result = contentListToString(getContentList(dataset, namespace, title));
    	} else {
    		result = dataset.getField(title.getChild("field", namespace).getAttributeValue("datasource"));
    	}
    	return result;
	}
	
	/**
	 * Retrieves the subtitle for the response.
	 * @param dataset The current dataset.
	 * @param namespace The document namespace.
	 * @param display The display element.
	 * @return A <code>String</code> containing the concatenated values of the <code>subtitle</code> tag.
	 */
	private String getSubTitle(final Dataset dataset, final Namespace namespace, final Element display) {
		
		String result = "";
		final Element subtitle = display.getChild("subtitle", namespace);
		if (subtitle.getChild("field", namespace) == null) {
			result = contentListToString(getContentList(dataset, namespace, subtitle));
		} else {
			result = dataset.fields.get(subtitle.getChild("field", namespace).getAttributeValue("datasource"));
		}
		return result;
	}
	
	/**
	 * Sets the sections of the response according to the definitions in the corresponing xml config file. 
	 * @param dataset The current dataset.
	 * @param namespace The document namespace.
	 * @param display The display element.
	 * @param response The response object to add the sections to.
	 */
	private void setSections(final Dataset dataset, final Namespace namespace, final Element display
			, final FormattedArachneEntity response) {

		final Element sections = display.getChild("datasections", namespace);
		final List<AbstractContent> contentList = getContentList(dataset, namespace, sections);
		
		if (contentList != null) {
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
	 * Internal function to retrieve the contents of a <code>section</code> or <code>context</code>.
	 * @param dataset The current dataset.
	 * @param namespace The document namespace.
	 * @param element The DOM element to retrieve the content of.
	 * @return A list containing the content of the passed in element.
	 */
	private List<AbstractContent> getContentList(final Dataset dataset, final Namespace namespace, final Element element) {

		final List<AbstractContent> contentList = new ArrayList<AbstractContent>();
		
		final List<Element> children = element.getChildren();
		for (Element e:children) {
			if (e.getName().equals("section")) {
				final Section section = (Section)xmlConfigUtil.getContentFromSections(e, namespace, dataset);
				if (section != null && !section.getContent().isEmpty()) {
					contentList.add(section);
				}
			} else {
				final Section section = (Section)xmlConfigUtil.getContentFromContext(e, dataset, namespace);
				if (section != null && !section.getContent().isEmpty()) {
					contentList.add(section);
				}
			}
		}

		if (!contentList.isEmpty()) {
			return contentList;
		}
		
		return null;
	}
	
	/**
	 * Converts a list of <code>AbstractContent</code> objects to a <code>string</code>.
	 * @param contentList The list to convert.
	 * @return The flattened representation of the list content.
	 */
	private String contentListToString(final List<AbstractContent> contentList) {
		if (contentList != null) {
			String result = contentList.toString();
			if (!result.isEmpty()) {
				result = result.substring(1, result.length() - 1);
			}
			return result;
		}
		return "";
	}
	
	/**
	 * This function retrieves the facets from the current config document and the corresponding values from the dataset.
	 * @param dataset The current dataset.
	 * @param facets The facet element of the current config file.
	 * @return A list of facets.
	 */
	private FacetList getFacets(final Dataset dataset, final Namespace namespace, final Element facets) {
		
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
						String value = dataset.getField(child.getAttributeValue("datasource"));
						if (value == null) {
							final StringBuilder ifEmtpyValue = xmlConfigUtil.getIfEmpty(child, namespace, dataset);
							if (!StrUtils.isEmptyOrNull(ifEmtpyValue)) {
								value = ifEmtpyValue.toString();
							}
						}
						
						if (value != null) {
							values.add(value);
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
		
		final Section section = xmlConfigUtil.getContentFromContext(child, dataset, null);
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